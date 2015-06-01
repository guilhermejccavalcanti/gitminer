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

public final class PropertyName {
    /**
     * This is static class for constants only
     */
    private PropertyName() {};
    
    public static final String ACTION = "action";
    public static final String ACTIONS = "actions";
    public static final String ADDITIONS = "additions";
    public static final String AFTER = "after";
    public static final String AUTHORED_DATE = "authoredDate";
    public static final String ASSIGNEE = "assignee";
    public static final String BEFORE = "before";
    public static final String BILLING_EMAIL = "billingEmail";
    public static final String BIOGRAPHY = "biography";
    public static final String BLOG = "blog";
    public static final String BODY = "body";
    public static final String BODY_HTML = "bodyHtml";
    public static final String BODY_TEXT = "bodyText";
    public static final String CLONE_URL = "cloneUrl";
    public static final String CLOSED_AT = "closedAt";
    public static final String CLOSED_ISSUES = "closedIssues";
    public static final String COLLABORATORS = "collaborators";
    public static final String COMMENTS = "comments";
    public static final String COMMITTED_DATE = "committedDate";
    public static final String COMMIT_ID = "commitId";
    public static final String COMMITS = "commits";
    public static final String COMPANY = "company";
    public static final String CONTENT_TYPE = "contentType";
    public static final String CONTRIBUTIONS = "contributions";
    public static final String CREATED_AT = "createdAt";
    public static final String DATE = "date";
    public static final String DELETIONS = "deletions";
    public static final String DESCRIPTION = "description";
    public static final String DIFF_HUNK = "diffHunk";
    public static final String DIFF_URL = "diffUrl";
    public static final String DISK_USAGE = "diskUsage";
    public static final String DOWNLOAD_COUNT = "downloadCount";
    public static final String DUE_DATE = "dueDate";
    public static final String EMAIL = "email";
    public static final String EVENT = "event";
    public static final String EVENT_ACTION = "eventAction";
    public static final String EVENT_TYPE = "eventType";
    public static final String FOLLOWERS = "followers";
    public static final String FOLLOWING = "following";
    public static final String FORKS = "forks";
    public static final String FULLNAME = "fullname";
    public static final String GITHUB_ID = "gitHubId";
    public static final String GIT_PULL_URL = "gitPullUrl";
    public static final String GIT_PUSH_URL = "gitPushUrl";
    public static final String GIT_URL = "gitUrl";
    public static final String GRAVATAR_ID = "gravatarId";
    public static final String HASH = "hash";
    public static final String HAS_DOWNLOADS = "hasDownloads";
    public static final String HAS_ISSUES = "hasIssues";
    public static final String HAS_WIKI = "hasWiki";
    public static final String HEAD = "head";
    public static final String HOMEPAGE = "homepage";
    public static final String HTML_URL = "htmlUrl";
    public static final String ID_NUM = "id_num";
    public static final String ISSUE_CREATED_AT = "issueCreatedAt";
    public static final String ISSUE_UPDATED_AT = "issueUpdatedAt";
    public static final String ISSUE_URL = "issueUrl";
    public static final String IS_FORK = "isFork";
    public static final String IS_MERGE = "isMerge";
    public static final String IS_PRIVATE = "isPrivate";
    public static final String LABEL = "label";
    public static final String LANGUAGE = "language";
    public static final String LINE = "line";
    public static final String LOCATION = "location";
    public static final String LOGIN = "login";
    public static final String MASTER_BRANCH = "masterBranch";
    public static final String MEMBERS = "members";
    public static final String MESSAGE = "message";
    public static final String MERGED_AT = "merged_at";
    public static final String MIRROR_URL = "mirrorUrl";
    public static final String NAME = "name";
    public static final String NUMBER = "number";
    public static final String OPEN_ISSUES = "openIssues";
    public static final String ORGANIZATION = "organization";
    public static final String ORG_TYPE = "orgType";
    public static final String ORIGINAL_COMMIT_ID = "originalCommitId";
    public static final String OWNED_PRIVATE_REPO_COUNT = "ownedPrivateRepoCount";
    public static final String OWNER = "owner";
    public static final String PARENT = "parent";
    public static final String PATCH_URL = "patchUrl";
    public static final String PATH = "path";
    public static final String PERMISSION = "permission";
    public static final String POSITION = "position";
    public static final String PRIVATE_GIST_COUNT = "private_gist_count";
    public static final String PUBLIC_GIST_COUNT = "public_gist_count";
    public static final String PUBLIC_REPO_COUNT = "public_repo_count";
    public static final String PUSHED_AT = "pushedAt";
    public static final String REF = "ref";
    public static final String REF_TYPE = "ref_type";
    public static final String REPO = "repo";
    public static final String REPOSITORIES = "repositories";
    public static final String REPO_TYPE = "repoType";
    public static final String SCORE = "score";
    public static final String SHA = "sha";
    public static final String SIZE = "size";
    public static final String SOURCE = "source";
    public static final String SPACE = "space";
    public static final String SSH_URL = "sshUrl";
    public static final String SVN_URL = "svnUrl";
    public static final String STATE = "state";
    public static final String SYS_COMMENTS_ADDED = "sys_comments_added";
    public static final String SYS_CREATED_AT = "sys_created_at";
    public static final String SYS_EVENTS_ADDED = "sys_events_added";
    public static final String SYS_DISCUSSIONS_ADDED = "sys_discussions_added";
    public static final String SYS_GISTS_ADDED = "sys_gists_added";
    public static final String SYS_LAST_FULL_UPDATE = "sys_last_full_update";
    public static final String SYS_LAST_UPDATED = "sys_last_updated";
    public static final String SYS_UPDATE_COMPLETE = "sys_update_complete";
    public static final String TOTAL_PRIVATE_REPO_COUNT = "totalPrivateRepoCount";
    public static final String TIME = "time";
    public static final String TITLE = "title";
    public static final String TIMEZONE = "timezone";
    public static final String TIMEZONE_OFFSET = "timezoneOffset";
    public static final String TREE = "tree";
    public static final String TYPE = "type";
    public static final String UPDATED_AT = "updatedAt";
    public static final String URL = "url";
    public static final String USER = "user";
    public static final String USERNAME = "username"; // FIXME: is this the same as USER?
    public static final String USER_TYPE = "user_type"; // GitHub normally returns a "type", to indicate if organization or not. "type" is reserved, so this is remapped to "user_type"
    public static final String VOTES = "votes";
    public static final String WATCHERS = "watchers";
    public static final String WHEN = "when";
}
