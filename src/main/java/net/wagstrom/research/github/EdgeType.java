/*
 * Copyright (c) 2011-2012 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wagstrom.research.github;

public final class EdgeType {
    /**
     * This is static class for constants only
     */
    private EdgeType() {}

    public static final String CHANGED =  "CHANGED";
    public static final String COMMITAUTHOR = "AUTHOR"; // COMMIT->USER also used by RepositoryLoader
    public static final String COMMITCOMMENTOWNER = "COMMIT_COMMENT_OWNER";
    public static final String COMMITCOMMENTREPO = "COMMIT_COMMENT_REPO";
    public static final String COMMITPARENT = "COMMIT_PARENT"; // also used by RepositoryLoader
    public static final String COMMITTER = "COMMITTER"; // COMMIT->USER also used by RepositoryLoader
    public static final String CREATOR = "CREATOR";
    public static final String DISCUSSIONCOMMIT = "DISCUSSION_COMMIT";
    public static final String DISCUSSIONUSER = "DISCUSSION_USER";
    public static final String EMAIL = "EMAIL"; // USER->EMAIL also used by RepositoryLoader
    public static final String EVENTCOMMENT = "EVENT_COMMENT";
    public static final String EVENTCOMMIT = "EVENT_COMMIT";
    public static final String EVENTCOMMITCOMMENT = "EVENT_COMMIT_COMMENT";
    public static final String EVENTDOWNLOAD = "EVENT_DOWNLOAD";
    public static final String EVENTFOLLOWUSER = "EVENT_FOLLOW_USER";
    public static final String EVENTFORKEE = "EVENT_FORKEE";
    public static final String EVENTGIST = "EVENT_GIST";
    public static final String EVENTGOLLUM = "EVENT_GOLLUM";
    public static final String EVENTISSUE = "EVENT_ISSUE";
    public static final String EVENTMEMBER = "EVENT_MEMBER";
    public static final String EVENTPAYLOADREPO = "EVENT_PAYLOAD_REPO";
    public static final String EVENTPULLREQUEST = "EVENT_PULL_REQUEST";
    public static final String EVENTREPO = "EVENT_REPO";
    public static final String EVENTTEAM = "EVENT_TEAM";
    public static final String EVENTUSER = "EVENT_USER";
    public static final String FOLLOWER = "FOLLOWER";
    public static final String FOLLOWING = "FOLLOWING";
    public static final String GISTCOMMENT = "GIST_COMMENT";
    public static final String GISTCOMMENTOWNER = "GIST_COMMENT_OWNER";
    public static final String GISTFILE = "GIST_FILE";
    public static final String GISTOWNER = "GIST_OWNER";
    public static final String GRAVATAR = "GRAVATAR";
    public static final String GRAVATARHASH = "GRAVATAR_HASH";
    public static final String ISSUE = "ISSUE";
    public static final String ISSUEALTEVENT = "ISSUE_ALT_EVENT";
    public static final String ISSUEASSIGNEE = "ISSUE_ASSIGNEE";
    public static final String ISSUELABEL = "ISSUE_LABEL";
    public static final String ISSUEOWNER = "ISSUE_OWNER";
    public static final String ISSUECOMMENT = "ISSUE_COMMENT";
    public static final String ISSUECOMMENTOWNER = "ISSUE_COMMENT_OWNER";
    public static final String ISSUEEVENT = "ISSUE_EVENT";
    public static final String ISSUEEVENTACTOR = "ISSUE_EVENT_ACTOR";
    public static final String MILESTONE = "MILESTONE";
    public static final String NAME = "NAME"; // used by RepositoryLoader
    public static final String ORGANIZATIONOWNER = "ORGANIZATION_OWNER";
    public static final String ORGANIZATIONMEMBER = "ORGANIZATION_MEMBER";
    public static final String ORGANIZATIONTEAM = "ORGANIZATION_TEAM";
    public static final String PULLREQUEST = "PULLREQUEST";
    public static final String PULLREQUESTBASE = "PULLREQUEST_BASE";
    public static final String PULLREQUESTDISCUSSION = "PULLREQUEST_DISCUSSION";
    public static final String PULLREQUESTLABEL = "PULLREQUEST_LABEL";
    public static final String PULLREQUESTOWNER = "PULLREQUEST_OWNER";
    public static final String PULLREQUESTHEAD = "PULLREQUEST_HEAD";
    public static final String PULLREQUESTISSUEUSER = "PULLREQUEST_ISSUE_USER";
    public static final String PULLREQUESTISSUECOMMENT = "PULLREQUEST_ISSUE_COMMENT";
    public static final String PULLREQUESTMARKERUSER = "PULLREQUEST_MARKER_USER";
    public static final String PULLREQUESTMERGEDBY = "PULLREQUEST_MERGED_BY";
    public static final String PULLREQUESTCOMMIT = "PULLREQUEST_COMMIT";
    public static final String PULLREQUESTREVIEWCOMMENT = "PULLREQUEST_REVIEW_COMMENT";
    public static final String PULLREQUESTREVIEWCOMMENTOWNER = "PULLREQUEST_REVIEW_COMMENT_OWNER";
    public static final String PULLREQUESTREVIEWCOMMENTCOMMIT = "PULLREQUEST_REVIEW_COMMENT_COMMIT";
    public static final String PULLREQUESTREVIEWCOMMENTORIGINALCOMMIT = "PULLREQUEST_REVIEW_COMMENT_ORIGINAL_COMMIT";
    public static final String PULLREQUESTCOMMENTOWNER = "PULLREQUEST_COMMENT_OWNER";
    public static final String REPOSITORY = "REPOSITORY"; // used by RepositoryLoader
    public static final String REPOWATCHED = "REPO_WATCHED";
    public static final String REPOOWNER = "REPO_OWNER";
    public static final String REPOPARENT = "REPO_PARENT";
    public static final String REPOCOLLABORATOR = "REPO_COLLABORATOR";
    public static final String REPOCONTRIBUTOR = "REPO_CONTRIBUTOR";
    public static final String REPOFORK = "REPO_FORK";
    public static final String REPOSOURCE = "REPO_SOURCE";
    public static final String TEAMMEMBER = "TEAM_MEMBER";
    public static final String USEREVENT = "USER_EVENT";
}