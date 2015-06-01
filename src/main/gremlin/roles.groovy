/**
 * produce information about roles in the community by mining the graph
 * database
 */
import net.wagstrom.research.github.IndexNames
import net.wagstrom.research.github.IdCols
import net.wagstrom.research.github.EdgeType
import net.wagstrom.research.github.VertexType
import net.wagstrom.research.github.PropertyName

ROCKSTAR_THRESHOLD=0.20; // should be in the top 20% of followed links by people in the community
PRODDER_TIME_THRESHOLD=14*86400 // number of seconds that an issue should be idle before a prodder picks it up
                                // we need to validate this by each project 
// amount of time between first and last commit required for inclusion in analysis
CW_MIN_SPAN_TIME = 14*86400
// minimum number of commits to be included in analysis, 3 because we need at least 2 deltas
CW_MIN_COMMITS = 3

PROJECTS_FILE = [System.getenv("HOME"), "Google Drive", "Ecosystem Research", "Data", "rails.db.20120505.coreMemberIntersections.txt"].join(File.separator)
MIN_OVERLAP = 100 // minimum number of overlap users with rails/rails as determined by the above file

def calculateDeltas(Collection xs) {
    return (xs == null || xs.size()<2)?null:[xs, xs[1..xs.size()-1]].transpose().collect{a,b -> b-a}
}

def stdev(Collection xs) { 
    return (xs == null || xs.size()<2)?null:Math.sqrt((xs.collect{it*it}.sum() - xs.sum()*xs.mean()) / (xs.size()-1))
}

/**
 * There are multiple different ways that we can tie a git account to a github
 * account. This iterates through three ways in an attempt to map the commit with the user
 * 
 * 1. direct links via email addresses
 * 2. gravatar links
 * 3. referenced commits from issue events
 * 
 */
def getUserFromGitUser(Vertex gituser) {
    try {
        return gituser.out(EdgeType.EMAIL).in(EdgeType.EMAIL).has(PropertyName.TYPE, VertexType.USER).next()    
    } catch (e) {}
    
    try {
        return gituser.out(EdgeType.EMAIL).out(EdgeType.GRAVATARHASH).in(EdgeType.GRAVATAR).next()
    } catch (e) {}

    try {
        return gituser.in('COMMITTER').out('AUTHOR').filter{it==gituser}.back(2).in("EVENT_COMMIT").in("ISSUE_EVENT_ACTOR").dedup().next()
    } catch (e) {}
   
    return null;
}

def getAllGitAccounts(IndexableGraph g, Vertex user) {
    // getting all of a users git accounts is tricky because they don't make all of their email addresses
    // public. Luckily, using these two methods we do a pretty good job of getting all of a users git_user
    // accounts
    gitAccounts = user.out(EdgeType.EMAIL). \
                       in(EdgeType.EMAIL). \
                       has("type", VertexType.GIT_USER). \
                       dedup().toSet()

    // this code has been superseded as it isn't always that accurate and can
    // grab accounts that don't belong to this user
    // gitAccounts = (gitAccounts as Set) + user.out(EdgeType.ISSUEEVENTACTOR). \
    //                  in(EdgeType.ISSUEEVENT).in(EdgeType.ISSUE). \
    //                  filter{it == repo}.back(3).out(EdgeType.EVENTCOMMIT). \
    //                  out(EdgeType.COMMITTER).dedup().toList()
    
    // here we need to be a little careful with finding additional accounts
    // this pipe takes all of the commits this person has tied to an issue,
    // and filters for those email addresses which are not associated with
    // a user yet. It assumes, and this is a big assumption, that if one of
    // these unparented links shows up in both COMMITTERS and PARENTS then
    // the user probably owns that account
    // traceAccountsCommitter = user.out(EdgeType.ISSUEEVENTACTOR).out(EdgeType.EVENTCOMMIT).out(EdgeType.COMMITTER). \
    //                          filter{it.type=="GIT_USER"}.out("EMAIL").dedup().filter{it.in("EMAIL").filter{it.type == "USER"}.count() == 0}.back(4).toList()
    // traceAccountsAuthor = user.out(EdgeType.ISSUEEVENTACTOR).out(EdgeType.EVENTCOMMIT).out(EdgeType.COMMITAUTHOR). \
    //                          filter{it.type=="GIT_USER"}.out("EMAIL").dedup().filter{it.in("EMAIL").filter{it.type == "USER"}.count() == 0}.back(4).toList()
    // traceAccounts = (traceAccountsCommitter as Set) + traceAccountsAuthor
    // a slightly more complicated but more accurate version of the above commands
    // this version requires that the supposedly unattached commit have the same author
    // and committer.                            
    traceAccounts = user.out(EdgeType.ISSUEEVENTACTOR).out(EdgeType.EVENTCOMMIT). \
         filter{it.out(EdgeType.COMMITTER).filter{it.type==VertexType.GIT_USER}.out(EdgeType.EMAIL).next() == \
                it.out(EdgeType.COMMITAUTHOR).filter{it.type==VertexType.GIT_USER}.out(EdgeType.EMAIL).next()}. \
         out(EdgeType.COMMITTER). \
         filter{it.type==VertexType.GIT_USER}.out(EdgeType.EMAIL).dedup().filter{it.in(EdgeType.EMAIL).filter{it.type == VertexType.USER}.count() == 0}. \
         back(4).dedup().toSet()
    
    gravatars = user.out(EdgeType.GRAVATAR). \
                     in(EdgeType.GRAVATARHASH). \
                     has(PropertyName.TYPE, VertexType.EMAIL). \
                     in(VertexType.EMAIL). \
                     has(PropertyName.TYPE, VertexType.GIT_USER).toSet()
            
    gitAccounts = gitAccounts + traceAccounts + gravatars
     
    allGitAccounts = [] as Set
    for (email in gitAccounts._().out(EdgeType.EMAIL).email.dedup().toSet()) {
        allGitAccounts += g.idx(IndexNames.EMAIL).get(IdCols.EMAIL, email)._().in(EdgeType.EMAIL).has("type", VertexType.GIT_USER).toSet()
    }

    return allGitAccounts
}

def getUserFromVertex(Vertex v) {
    switch (v.type) {
        case VertexType.ISSUE:
            return v.in(EdgeType.ISSUEOWNER).next()
            break
        
        case VertexType.COMMENT:
            return v.in(EdgeType.ISSUECOMMENTOWNER).next()
            break
        
        case VertexType.ISSUE_EVENT:
            return v.in(EdgeType.ISSUEEVENTACTOR).next()
            break
        
        default:
            println "Unable to get User for vertex id=" + v.id + " type: " + v.type
            return null;
            break
    }
}

/**
 * calculate the users who are prodders
 * 
 * question: can users be prodders if they follow their own activities?
 * answer: yes
 * question: can user be prodders on issues they own
 * answer: yes
 */
def getIssueInfo(IndexableGraph g, Vertex issue) {
    prodders = []
    
    // timeline is tuple of (eventDate, event)
    // where event can actually be any of the actual issue, issue comment, or issue event
    timeline = [[Helpers.timestampToDate(issue.createdAt), issue]]
    
    // first sort all of the issue comments
    issue.out(EdgeType.ISSUECOMMENT). \
          sideEffect{timeline.add([Helpers.timestampToDate(it.createdAt), it])}. \
          iterate()
    
    // next add all the issue events
    issue.out(EdgeType.ISSUEEVENT). \
          hasNot("event", "subscribed"). \
          hasNot("event", "unsubscribed"). \
          hasNot("event", "mentioned"). \
          sideEffect{timeline.add([Helpers.timestampToDate(it.createdAt), it])}. \
          iterate()
    
    // next, if this is actually a pull request, we need to add the
    // actions associated with the pull request
    try { 
        pullRequest = g.idx(IndexNames.PULLREQUEST).get(IdCols.PULLREQUEST, issue.issue_id).next() 
        // fixme: add in pullrequest actions
    } catch (java.util.NoSuchElementException e) { }
    
    lastTime = null
    lastAction = null
    for (element in timeline.sort{a,b -> a[0] <=> b[0]}) {
        when = element[0]
        action = element[1]
    
        if (lastTime != null && Helpers.dateDifferenceAbs(when, lastTime) > PRODDER_TIME_THRESHOLD) {
            if (getUserFromVertex(action).login == "name") {
                println "action: " +  action  + " issue: " + issue.issue_id
            }
            prodders.add(getUserFromVertex(action))
        }
        lastTime = when
        lastAction = action
    }
    return prodders as Set
}

/**
 * Provides information about secondary attributes that can be used to calculate
 * other roles
 * 
 * Stewards - top 20% of pull requests merged and top 20% of issues closed
 * Code Warrior -
 * Project Rock Stars - individuals in the top 20% of contributors and also the top 20% of followers
 */
def devInfo(IndexableGraph g, Vertex user, Vertex repo, Map userSet) {
    gitAccounts = getAllGitAccounts(g, user)
                          
    // project rock star: following
    communityFollowing = user.out(EdgeType.FOLLOWING).filter{userSet["allUsers"].contains(it)}.count()
    // project rock star: coding conrtributions
    codingContributions = gitAccounts._().in(EdgeType.COMMITAUTHOR).out(EdgeType.REPOSITORY).filter{it == repo}.count()
    
    // community rock star: coding contributions
    // NOTE: this ONLY covers contributions to the projects we've pulled
    allCodingContributions = gitAccounts._().in(EdgeType.COMMITAUTHOR).count()
    allCodingProjects = gitAccounts._().in(EdgeType.COMMITAUTHOR).out(EdgeType.REPOSITORY).dedup().count()
    // look at people following...
    
    // project merged pull requests
    projectMergedPullRequests = user.in(EdgeType.PULLREQUESTMERGEDBY).in(EdgeType.PULLREQUEST).filter{it==repo}.count()

    // issues closed
    projectIssuesClosed = user.out(EdgeType.ISSUEEVENTACTOR). \
                               has(PropertyName.EVENT,"closed"). \
                               in(EdgeType.ISSUEEVENT). \
                               dedup(). \
                               in(EdgeType.ISSUE). \
                               filter{it == repo}.count()

//    println "User: " + user.login
//    println "  Community Following: " + communityFollowing
//    println "    Community Commits: " + codingContributions
//    println "          All Commits: " + allCodingContributions
//    println "         All Projects: " + allCodingProjects
//    println " Pull Requests Merged: " + projectMergedPullRequests

    return ["communityFollowing": communityFollowing,
            "codingContributions": codingContributions,
            "allCodingContributions": allCodingContributions,
            "allCodingProjects": allCodingProjects,
            "projectPullRequestsMerged": projectMergedPullRequests,
            "projectIssuesClosed": projectIssuesClosed]
}

def devInfoThreshold(Map devInfo, String key, float threshold) {
    int numEntries = devInfo.size() * threshold
    if (numEntries == 0) { 
        return []
    }
    return devInfo.sort{a,b -> a.value[key] <=> b.value[key]}.keySet().asList().reverse()[0..numEntries] as Set
}

/**
 *  Generates an overlap matrix for a given set of projects on a given set of metrics
 *  
 */
def allUserDataCompare(Map allUserData, Collection projects, Collection metrics) {
    println "*************************************"
    println "Overlap for projects: " + projects
    for (metric in metrics) {
        println "Overlap for: " + metric
        overlap = new int[projects.size()][projects.size()]
        for (int i = 0; i < projects.size(); i ++) {
            for (int j = 0; j <= i; j ++) {
                overlap[i][j] = allUserData[projects[i]][metric].intersect(allUserData[projects[j]][metric]).size()
                print sprintf("%6d", overlap[i][j])
            }
            println ""
        }
    }    
}

def allUserRoleOverload(Map allUserData, Collection projects, Collection metrics) {
    println "*************************************"
    println "Overload for roles: " + metrics
    for (project in projects) {
        println "Overlap for: " + project
        overlap = new int[metrics.size()][metrics.size()]
        for (int i = 0; i < metrics.size(); i ++) {
            for (int j = 0; j <= i; j ++) {
                overlap[i][j] = allUserData[project][metrics[i]].intersect(allUserData[project][metrics[j]]).size()
                print sprintf("%6d", overlap[i][j])
            }
            println ""
        }
    }
}

/**
 * read in the list of projects from the file
 *
 * this is a really simple file format that consists of:
 *   reponame, sharedusers
 *
 * where sharedusers is the number of users that are shared in common with rails/rails
 *
 */
def readProjectListFile(String projectFile, int minOverlap) {
    file = new File(projectFile)
    projects = file.readLines()
    // there is a header line that we need to discard
    // otherwise we take all projects with the specified overlap
    return projects[1..projects.size()-1].findAll{Integer.parseInt(it.split(",")[1]) >= minOverlap}.collect{it.split(",")[0]}
    // return projects[1..projects.size-1]
    // return projects[126..129]
}

g = new Neo4jGraph(Defaults.DBPATH)
// projects = ["tinkerpop/gremlin", "tinkerpop/blueprints", "tinkerpop/pipes", "tinkerpop/rexster", "tinkerpop/frames"]
// projects = Defaults.PROJECTS
projects = readProjectListFile(PROJECTS_FILE, MIN_OVERLAP)

allUserData = [:]

for (project in projects) {
    repo = g.idx(IndexNames.REPOSITORY).get(IdCols.REPOSITORY, project).next()
    println "\n\n******************************************"
    println project
                             
    watchers = repo.in(EdgeType.REPOWATCHED).toSet()
        
    // collaborators: have admin rights on projects
    collaborators = repo.out(EdgeType.REPOCOLLABORATOR).toSet()
    
    // contributors: have committed code to project
    contributors = repo.out(EdgeType.REPOCONTRIBUTOR).toSet() + \
                   repo.in(EdgeType.REPOOWNER).dedup().toSet()
    issueOwners = repo.out(EdgeType.ISSUE). \
                       in(EdgeType.ISSUEOWNER). \
                       dedup().toSet()
    issueCommenters = repo.out(EdgeType.ISSUE). \
                           out(EdgeType.ISSUECOMMENT). \
                           in(EdgeType.ISSUECOMMENTOWNER).dedup().toSet()
    issueSubscribers = repo.out(EdgeType.ISSUE). \
                            out(EdgeType.ISSUEEVENT). \
                            has(PropertyName.EVENT, "subscribed"). \
                            in(EdgeType.ISSUEEVENTACTOR).dedup().toSet()
    pullRequestOwners = repo.out(EdgeType.PULLREQUEST). \
                             in(EdgeType.PULLREQUESTOWNER).dedup().toSet()
    openPullRequestOwners = repo.out(EdgeType.PULLREQUEST). \
                                 filter{it.closedAt==null}.in(EdgeType.PULLREQUESTOWNER).dedup().toSet()
    closedPullRequestOwners = repo.out(EdgeType.PULLREQUEST). \
                                   filter{it.closedAt!=null}.in(EdgeType.PULLREQUESTOWNER).dedup().toSet()
    mergedPullRequestOwners = repo.out(EdgeType.PULLREQUEST). \
                                   filter{it.merged_at != null}.in(EdgeType.PULLREQUESTOWNER).dedup().toSet()
    pullRequestCommenters = repo.out(EdgeType.PULLREQUEST). \
                                 out(EdgeType.PULLREQUESTDISCUSSION). \
                                 filter{it.type==VertexType.USER.toString()}.dedup().toSet()
    mergers = repo.out(EdgeType.ISSUE). \
                   out(EdgeType.ISSUEEVENT). \
                   filter{it.event=="merged"}.in(EdgeType.ISSUEEVENTACTOR).dedup().toSet()
    // filter out Organizations because they don't really fork the code. It's an artifact
    // of the way that GitHub handles organizations in their code
    forkOwners = repo.out(EdgeType.REPOFORK). \
                      in(EdgeType.REPOOWNER). \
                      hasNot(PropertyName.USER_TYPE, "Organization").dedup().toSet()

    committers = repo.in(EdgeType.REPOSITORY). \
                      out(EdgeType.COMMITAUTHOR). \
                      filter{it.type==VertexType.GIT_USER}. \
                      out(EdgeType.EMAIL).dedup(). \
                      in(EdgeType.EMAIL). \
                      filter{it.type==VertexType.USER}.toSet()
     
    
    organizationMembers = repo.in(EdgeType.REPOOWNER). \
                               in(EdgeType.ORGANIZATIONMEMBER).toSet()
    organizationCommitters = committers.intersect(organizationMembers + collaborators)
    insideCollaborators = (organizationMembers + collaborators) as Set
    // I have no idea why this works, but it fixes some of the syntax errors
    // we get with the .count() method
    insideCollaborators = Helpers.setDifferenceLeft(insideCollaborators, [])

    allCollaborators = (collaborators as Set) + contributors + insideCollaborators
    
    allActive = (collaborators + contributors + issueOwners + \
        issueCommenters + pullRequestOwners + \
        openPullRequestOwners + closedPullRequestOwners + \
        pullRequestCommenters + mergedPullRequestOwners + \
        mergers + forkOwners + insideCollaborators).toSet()


    allUsers = allActive + watchers + issueSubscribers + insideCollaborators
    
    /*
     * Lurkers - only watching the repo or subscribed to an issue, no
     * forks, issues, etc
     */
    lurkers = Helpers.setDifferenceLeft((watchers + issueSubscribers), allActive)
    
    /*
     * Issues - people active only on issues. This clearly needs a better name.
     */
    issueUsers = Helpers.setDifferenceLeft((issueOwners + issueCommenters + pullRequestCommenters).unique(),
                                           (insideCollaborators +
                                            pullRequestOwners +
                                            openPullRequestOwners + closedPullRequestOwners +
                                            mergedPullRequestOwners +
                                            mergers + forkOwners).unique())
    /*
     * Independent - forked the repo, but no pull requests
     */
    independent = Helpers.setDifferenceLeft(forkOwners, \
                                            (pullRequestOwners + insideCollaborators).unique())

    /*
     * Wannabes - created a pull request, but never accepted
     */
    wannabes = Helpers.setDifferenceLeft(closedPullRequestOwners, (mergedPullRequestOwners + insideCollaborators))

    /*
     * External Contributors - created a pull request, pull request
     * has been merged. No direct access.
     */
    externalContributors =  Helpers.setDifferenceLeft(mergedPullRequestOwners,
                                                      (insideCollaborators).unique())

    /*
     * Lurkers - only watching the repo or subscribed to an issue, no
     * forks, issues, etc
     */
    println "Lurkers [" + lurkers.count() + "]"
    println lurkers.login.sort{a,b -> a <=> b}
    
    /*
     * Issues - people active only on issues. This clearly needs a better name.
     */
    println "Issues [" + issueUsers.count() + "]"
    println issueUsers.login.sort{a,b -> a <=> b}
    
    /*
     * Independent - forked the repo, but no pull requests
     */
    println "Independent [" + independent.count() + "]"
    println independent.login.sort{a,b -> a <=> b}

    /*
     * Wannabes - created a pull request, but never accepted
     */
    println "Wannabes [" + wannabes.count() + "]"
    println wannabes.login.sort{a,b -> a <=> b}
    
    /*
     * External Contributors - created a pull request, pull request
     * has been merged. No direct access.
     */
    println "External Contributors [" + externalContributors.count() + "]"
    println externalContributors.login.sort{a,b -> a <=> b}

    /*
     * Collaborators - have direct access to push to the main repo
     */
    println "Collaborators [" + insideCollaborators.count() + "]"
    println insideCollaborators.login.sort{a,b -> a <=> b}

//    println "Sanity Checks - Overlap"
//    println "Lurkers - Issues: " + lurkers.intersect(issueUsers).count()
//    println "Lurkers - Independent: " + lurkers.intersect(independent).count()
//    println "Lurkers - Wannabes: " + lurkers.intersect(wannabes).count()
//    println "Lurkers - External Contributors: " + lurkers.intersect(externalContributors).count()
//    println "Lurkers - Collaborators: " + lurkers.intersect(insideCollaborators).count()
//    println "Issues - Independent: " + issueUsers.intersect(independent).count()
//    println "Issues - Wannabes: " + issueUsers.intersect(wannabes).count()
//    println "Issues - External Contributors: " + issueUsers.intersect(externalContributors).count()
//    println "Issues - Collaborators: " + issueUsers.intersect(insideCollaborators).count()
//    println "Independent - Wannabes: " + independent.intersect(wannabes).count()
//    println "Independent - External Contributors: " + independent.intersect(externalContributors).count()
//    println "Independent - Collaborators: " + independent.intersect(insideCollaborators).count()
//    println "Wannabes - External Contributors: " + wannabes.intersect(externalContributors).count()
//    println "Wannabes - Collaborators: " + wannabes.intersect(insideCollaborators).count()
//    println "External Contributors - Collaborators: " + externalContributors.intersect(insideCollaborators).count()
//    
//    println "Sanity Checks - Coverage"
    classifiedUsers = (lurkers + issueUsers + independent + wannabes + externalContributors + insideCollaborators)
    println "Total Classified Users: " + classifiedUsers.size()
//    println "All Users: " + allUsers.size()
//    
//    println "Classified but not in All Users: " + Helpers.setDifferenceLeft(classifiedUsers, allUsers).login.sort()
//    println "All Users but not in Classified: " + Helpers.setDifferenceLeft(allUsers, classifiedUsers).login.sort()
//    
    userSet = ["allUsers": allUsers,
               "lurkers": lurkers,
               "issues": issueUsers,
               "independent": independent,
               "wannabes": wannabes,
               "external": externalContributors,
               "internal": insideCollaborators]
    
    devStats = [:]
    for (collab in allCollaborators) {
        devStats[collab] = devInfo(g, collab, repo, userSet)
    }

    topFollowers = devInfoThreshold(devStats, "communityFollowing", ROCKSTAR_THRESHOLD)
    topCoders = devInfoThreshold(devStats, "codingContributions", ROCKSTAR_THRESHOLD)
    println "Project Rock Stars"
    println topFollowers.intersect(topCoders).login.sort()
    userSet["projectRockStars"] = topFollowers.intersect(topCoders)
    
    topIssueClosers = devInfoThreshold(devStats, "projectIssuesClosed", ROCKSTAR_THRESHOLD)
    topPullRequestClosers = devInfoThreshold(devStats, "projectPullRequestsMerged", ROCKSTAR_THRESHOLD)
    println "Project Stewards"
    println topIssueClosers.intersect(topPullRequestClosers).login.sort()
    userSet["projectStewards"] = topIssueClosers.intersect(topPullRequestClosers)
    
    allProdders = [:].withDefault{0}
    for (issue in repo.out(EdgeType.ISSUE)) {
        prodders = getIssueInfo(g, issue)        
        prodders.each{allProdders[it] += 1}
    }
    int numEntries = allProdders.size() * ROCKSTAR_THRESHOLD
    println "Prodders"
    if (allProdders.size > 0) {
        println allProdders.sort{a,b -> a.value <=> b.value}.keySet().asList().reverse()[0..numEntries].login.sort()
        userSet["prodders"] = allProdders.sort{a,b -> a.value <=> b.value}.keySet().asList().reverse()[0..numEntries]
    } else {
        userSet["prodders"] = []
    }
    
    println "Code Warriors"
    // this has been inlined in the following function because groovy fails to type 'correctly'
    //def zip(Collection a, Collection b) { [a, b].transpose() }

    // select all commits
    try {
        committerDeltas = repo.in(EdgeType.REPOSITORY). \
                               // group for each committer & extract from pipe
                               groupBy{ getUserFromGitUser(it.out(EdgeType.COMMITAUTHOR).next()) }{it}. \
                               //groupBy{ it.out(EdgeType.COMMITAUTHOR).next() }{it}. \
                               cap.next(). \
                               // extract commit times and sort by date
                               collect{a, b -> [a, b*.outE("COMMITTER")*.when*.next().sort{it}]}. \
                               // filter users whose commits have not spanned 14 days
                               findAll{a, b -> b[-1]-b[0] > CW_MIN_SPAN_TIME && 
                                                            b.size() > CW_MIN_COMMITS}. \
                               // calculate deltas between times
                               collect{a, b -> [a, calculateDeltas(b)]}. \
                               // filter nil deltas (needed?)
                               //findAll{a, b -> b != null }. \
                               // run mean and sd
                               collect{a, b -> [a, b.mean(), stdev(b)]}
    } catch ( java.util.NoSuchElementException nse ) {
        committerDeltas = []
    }
    //println committerDeltas
    // reverse sort by mean, take top 20%, extract github user
    if (committerDeltas.size > 0) {
        frequentUsers   = committerDeltas.findAll{it!=null}.sort{row -> -row[1]}[0..committerDeltas.size()*ROCKSTAR_THRESHOLD]*.get(0)
        // reverse sort by sd, take top 20%, extract githubuser
        consistentUsers = committerDeltas.findAll{it!=null}.sort{row -> -row[2]}[0..committerDeltas.size()*ROCKSTAR_THRESHOLD]*.get(0)
        println frequentUsers.intersect(consistentUsers).login.sort()
        userSet["codeWarriors"] = frequentUsers.intersect(consistentUsers)
    } else {
        userSet["codeWarriors"] = []
    }

    println "Nomads"

    // select commits
    nomads = repo.in(EdgeType.REPOSITORY). \
                  // group by {githubuser -> count(commits)}
                  groupCount{getUserFromGitUser(it.out(EdgeType.COMMITAUTHOR).next())}. \
                  // extract
                  cap.next(). \
                  // exclude nil keys
                  findAll{it.key != null}. \
                  // select single commit users
                  findAll{it.value == 1}. \
                  // fetch gitusers
                  collect{it.key}. \
                  // convert back to gitusers (take care because this is a set
                  // select gitusers with commits in other projects
                  findAll{getAllGitAccounts(g, it)._(). \
                             in(EdgeType.COMMITAUTHOR). \
                             filter{a -> a.out(EdgeType.REPOSITORY).next() != repo}. \
                             count() > 0}

    println nomads*.login
    userSet["nomads"] = nomads
    
    allUserData[project] = userSet
}

g.shutdown()

println allUserData.keySet()

println "*************************************************"
println "MEGA COMPARE!"
println "*************************************************"
metrics = allUserData[allUserData.keySet().toList()[0]].keySet().toList()
println "Metrics: " + metrics
allUserDataCompare(allUserData, projects, metrics)
allUserRoleOverload(allUserData, projects, metrics)
