package nl.rdh.github.api.v1

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ControllerIT {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `get labels for a repo`() {
        val response = restTemplate.getForEntity(
            "/api/v1/labels/testorg/repo-one",
            Array<String>::class.java
        )
        assertThat(response.statusCode.value()).isEqualTo(200)
        val body = response.body!!.toList()
        assertThat(body).containsExactly("good first issue", "help wanted")
    }

    @Test
    fun `get all labels for an org`() {
        val response = restTemplate.getForEntity(
            "/api/v1/labels/testorg",
            Array<String>::class.java
        )
        assertThat(response.statusCode.value()).isEqualTo(200)
        val body = response.body!!.toList()
        // Combined unique labels across repos, sorted
        assertThat(body).containsExactly("good first issue", "help wanted", "maintenance")
    }

    @Test
    fun `get contribution issues for an org`() {
        val response = restTemplate.exchange(
            "/api/v1/contribution-issues/testorg",
            org.springframework.http.HttpMethod.GET,
            null,
            object : org.springframework.core.ParameterizedTypeReference<List<Map<String, Any>>>() {}
        )
        assertThat(response.statusCode.value()).isEqualTo(200)
        val body = response.body!!
        assertThat(body).hasSize(1)
        val issue = body[0]
        assertThat(issue["url"]).isEqualTo("https://api.github.com/repos/testorg/repo-one/issues/1")
        assertThat(issue["htmlUrl"]).isEqualTo("https://github.com/testorg/repo-one/issues/1")
        assertThat(issue["title"]).isEqualTo("An easy first contribution")
        assertThat(issue["state"]).isEqualTo("open")
        assertThat((issue["comments"] as Number).toInt()).isEqualTo(2)
        val labels = issue["labels"] as List<*>
        assertThat(labels).contains("help wanted")
    }

    companion object {
        private var mockServer: MockWebServer = MockWebServer()
        init {
            mockServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
                    "/orgs/testorg/repos" -> MockResponse()
                        .addHeader("Content-Type", "application/json")
                        .setBody(REPOS_JSON)

                    "/repos/testorg/repo-one/labels" -> MockResponse()
                        .addHeader("Content-Type", "application/json")
                        .setBody(LABELS_REPO_ONE_JSON)

                    "/repos/testorg/repo-two/labels" -> MockResponse()
                        .addHeader("Content-Type", "application/json")
                        .setBody(LABELS_REPO_TWO_JSON)

                    "/repos/testorg/repo-one/issues" -> MockResponse()
                        .addHeader("Content-Type", "application/json")
                        .setBody(ISSUES_REPO_ONE_JSON)

                    else -> MockResponse().setResponseCode(404)
                }
            }
            mockServer.start()
        }

        @JvmStatic
        @AfterAll
        fun tearDownMockServer() {
            mockServer.shutdown()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("github.api.url") { mockServer.url("/").toString().removeSuffix("/") }
        }

        private const val REPOS_JSON = """
            [
              {
                "allow_forking": true,
                "archive_url": "https://api.github.com/repos/testorg/repo-one/{archive_format}{/ref}",
                "archived": false,
                "assignees_url": "https://api.github.com/repos/testorg/repo-one/assignees{/user}",
                "blobs_url": "https://api.github.com/repos/testorg/repo-one/git/blobs{/sha}",
                "branches_url": "https://api.github.com/repos/testorg/repo-one/branches{/branch}",
                "clone_url": "https://github.com/testorg/repo-one.git",
                "collaborators_url": "https://api.github.com/repos/testorg/repo-one/collaborators{/collaborator}",
                "comments_url": "https://api.github.com/repos/testorg/repo-one/comments{/number}",
                "commits_url": "https://api.github.com/repos/testorg/repo-one/commits{/sha}",
                "compare_url": "https://api.github.com/repos/testorg/repo-one/compare/{base}...{head}",
                "contents_url": "https://api.github.com/repos/testorg/repo-one/contents/{+path}",
                "contributors_url": "https://api.github.com/repos/testorg/repo-one/contributors",
                "created_at": "2020-01-01T00:00:00Z",
                "default_branch": "main",
                "deployments_url": "https://api.github.com/repos/testorg/repo-one/deployments",
                "description": "Repo one",
                "disabled": false,
                "downloads_url": "https://api.github.com/repos/testorg/repo-one/downloads",
                "events_url": "https://api.github.com/repos/testorg/repo-one/events",
                "fork": false,
                "forks": 0,
                "forks_count": 0,
                "forks_url": "https://api.github.com/repos/testorg/repo-one/forks",
                "full_name": "testorg/repo-one",
                "git_commits_url": "https://api.github.com/repos/testorg/repo-one/git/commits{/sha}",
                "git_refs_url": "https://api.github.com/repos/testorg/repo-one/git/refs{/sha}",
                "git_tags_url": "https://api.github.com/repos/testorg/repo-one/git/tags{/sha}",
                "git_url": "git://github.com/testorg/repo-one.git",
                "has_discussions": false,
                "has_downloads": true,
                "has_issues": true,
                "has_pages": false,
                "has_projects": false,
                "has_wiki": false,
                "homepage": null,
                "hooks_url": "https://api.github.com/repos/testorg/repo-one/hooks",
                "html_url": "https://github.com/testorg/repo-one",
                "id": 1,
                "is_template": false,
                "issue_comment_url": "https://api.github.com/repos/testorg/repo-one/issues/comments{/number}",
                "issue_events_url": "https://api.github.com/repos/testorg/repo-one/issues/events{/number}",
                "issues_url": "https://api.github.com/repos/testorg/repo-one/issues{/number}",
                "keys_url": "https://api.github.com/repos/testorg/repo-one/keys{/key_id}",
                "labels_url": "https://api.github.com/repos/testorg/repo-one/labels{/name}",
                "language": null,
                "languages_url": "https://api.github.com/repos/testorg/repo-one/languages",
                "license": null,
                "merges_url": "https://api.github.com/repos/testorg/repo-one/merges",
                "milestones_url": "https://api.github.com/repos/testorg/repo-one/milestones{/number}",
                "mirror_url": null,
                "name": "repo-one",
                "node_id": "R_kgDOrepoone",
                "notifications_url": "https://api.github.com/repos/testorg/repo-one/notifications{?since,all,participating}",
                "open_issues": 1,
                "open_issues_count": 1,
                "owner": {
                  "avatar_url": "https://avatars.githubusercontent.com/u/1?v=4",
                  "events_url": "https://api.github.com/users/testorg/events{/privacy}",
                  "followers_url": "https://api.github.com/users/testorg/followers",
                  "following_url": "https://api.github.com/users/testorg/following{/other_user}",
                  "gists_url": "https://api.github.com/users/testorg/gists{/gist_id}",
                  "gravatar_id": "",
                  "html_url": "https://github.com/testorg",
                  "id": 1,
                  "login": "testorg",
                  "node_id": "MDQ6VXNlcjE=",
                  "organizations_url": "https://api.github.com/users/testorg/orgs",
                  "received_events_url": "https://api.github.com/users/testorg/received_events",
                  "repos_url": "https://api.github.com/users/testorg/repos",
                  "site_admin": false,
                  "starred_url": "https://api.github.com/users/testorg/starred{/owner}{/repo}",
                  "subscriptions_url": "https://api.github.com/users/testorg/subscriptions",
                  "type": "Organization",
                  "url": "https://api.github.com/users/testorg",
                  "user_view_type": "public"
                },
                "permissions": {
                  "admin": false,
                  "maintain": false,
                  "pull": true,
                  "push": false,
                  "triage": false
                },
                "private": false,
                "pulls_url": "https://api.github.com/repos/testorg/repo-one/pulls{/number}",
                "pushed_at": "2020-01-02T00:00:00Z",
                "releases_url": "https://api.github.com/repos/testorg/repo-one/releases{/id}",
                "size": 1,
                "ssh_url": "git@github.com:testorg/repo-one.git",
                "stargazers_count": 0,
                "stargazers_url": "https://api.github.com/repos/testorg/repo-one/stargazers",
                "statuses_url": "https://api.github.com/repos/testorg/repo-one/statuses/{sha}",
                "subscribers_url": "https://api.github.com/repos/testorg/repo-one/subscribers",
                "subscription_url": "https://api.github.com/repos/testorg/repo-one/subscription",
                "svn_url": "https://github.com/testorg/repo-one",
                "tags_url": "https://api.github.com/repos/testorg/repo-one/tags",
                "teams_url": "https://api.github.com/repos/testorg/repo-one/teams",
                "topics": [],
                "trees_url": "https://api.github.com/repos/testorg/repo-one/git/trees{/sha}",
                "updated_at": "2020-01-03T00:00:00Z",
                "url": "https://api.github.com/repos/testorg/repo-one",
                "visibility": "public",
                "watchers": 0,
                "watchers_count": 0,
                "web_commit_signoff_required": false
              },
              {
                "allow_forking": true,
                "archive_url": "https://api.github.com/repos/testorg/repo-two/{archive_format}{/ref}",
                "archived": false,
                "assignees_url": "https://api.github.com/repos/testorg/repo-two/assignees{/user}",
                "blobs_url": "https://api.github.com/repos/testorg/repo-two/git/blobs{/sha}",
                "branches_url": "https://api.github.com/repos/testorg/repo-two/branches{/branch}",
                "clone_url": "https://github.com/testorg/repo-two.git",
                "collaborators_url": "https://api.github.com/repos/testorg/repo-two/collaborators{/collaborator}",
                "comments_url": "https://api.github.com/repos/testorg/repo-two/comments{/number}",
                "commits_url": "https://api.github.com/repos/testorg/repo-two/commits{/sha}",
                "compare_url": "https://api.github.com/repos/testorg/repo-two/compare/{base}...{head}",
                "contents_url": "https://api.github.com/repos/testorg/repo-two/contents/{+path}",
                "contributors_url": "https://api.github.com/repos/testorg/repo-two/contributors",
                "created_at": "2020-02-01T00:00:00Z",
                "default_branch": "main",
                "deployments_url": "https://api.github.com/repos/testorg/repo-two/deployments",
                "description": "Repo two",
                "disabled": false,
                "downloads_url": "https://api.github.com/repos/testorg/repo-two/downloads",
                "events_url": "https://api.github.com/repos/testorg/repo-two/events",
                "fork": false,
                "forks": 0,
                "forks_count": 0,
                "forks_url": "https://api.github.com/repos/testorg/repo-two/forks",
                "full_name": "testorg/repo-two",
                "git_commits_url": "https://api.github.com/repos/testorg/repo-two/git/commits{/sha}",
                "git_refs_url": "https://api.github.com/repos/testorg/repo-two/git/refs{/sha}",
                "git_tags_url": "https://api.github.com/repos/testorg/repo-two/git/tags{/sha}",
                "git_url": "git://github.com/testorg/repo-two.git",
                "has_discussions": false,
                "has_downloads": true,
                "has_issues": false,
                "has_pages": false,
                "has_projects": false,
                "has_wiki": false,
                "homepage": null,
                "hooks_url": "https://api.github.com/repos/testorg/repo-two/hooks",
                "html_url": "https://github.com/testorg/repo-two",
                "id": 2,
                "is_template": false,
                "issue_comment_url": "https://api.github.com/repos/testorg/repo-two/issues/comments{/number}",
                "issue_events_url": "https://api.github.com/repos/testorg/repo-two/issues/events{/number}",
                "issues_url": "https://api.github.com/repos/testorg/repo-two/issues{/number}",
                "keys_url": "https://api.github.com/repos/testorg/repo-two/keys{/key_id}",
                "labels_url": "https://api.github.com/repos/testorg/repo-two/labels{/name}",
                "language": null,
                "languages_url": "https://api.github.com/repos/testorg/repo-two/languages",
                "license": null,
                "merges_url": "https://api.github.com/repos/testorg/repo-two/merges",
                "milestones_url": "https://api.github.com/repos/testorg/repo-two/milestones{/number}",
                "mirror_url": null,
                "name": "repo-two",
                "node_id": "R_kgDOrepotwo",
                "notifications_url": "https://api.github.com/repos/testorg/repo-two/notifications{?since,all,participating}",
                "open_issues": 0,
                "open_issues_count": 0,
                "owner": {
                  "avatar_url": "https://avatars.githubusercontent.com/u/1?v=4",
                  "events_url": "https://api.github.com/users/testorg/events{/privacy}",
                  "followers_url": "https://api.github.com/users/testorg/followers",
                  "following_url": "https://api.github.com/users/testorg/following{/other_user}",
                  "gists_url": "https://api.github.com/users/testorg/gists{/gist_id}",
                  "gravatar_id": "",
                  "html_url": "https://github.com/testorg",
                  "id": 1,
                  "login": "testorg",
                  "node_id": "MDQ6VXNlcjE=",
                  "organizations_url": "https://api.github.com/users/testorg/orgs",
                  "received_events_url": "https://api.github.com/users/testorg/received_events",
                  "repos_url": "https://api.github.com/users/testorg/repos",
                  "site_admin": false,
                  "starred_url": "https://api.github.com/users/testorg/starred{/owner}{/repo}",
                  "subscriptions_url": "https://api.github.com/users/testorg/subscriptions",
                  "type": "Organization",
                  "url": "https://api.github.com/users/testorg",
                  "user_view_type": "public"
                },
                "permissions": {
                  "admin": false,
                  "maintain": false,
                  "pull": true,
                  "push": false,
                  "triage": false
                },
                "private": false,
                "pulls_url": "https://api.github.com/repos/testorg/repo-two/pulls{/number}",
                "pushed_at": "2020-02-02T00:00:00Z",
                "releases_url": "https://api.github.com/repos/testorg/repo-two/releases{/id}",
                "size": 1,
                "ssh_url": "git@github.com:testorg/repo-two.git",
                "stargazers_count": 0,
                "stargazers_url": "https://api.github.com/repos/testorg/repo-two/stargazers",
                "statuses_url": "https://api.github.com/repos/testorg/repo-two/statuses/{sha}",
                "subscribers_url": "https://api.github.com/repos/testorg/repo-two/subscribers",
                "subscription_url": "https://api.github.com/repos/testorg/repo-two/subscription",
                "svn_url": "https://github.com/testorg/repo-two",
                "tags_url": "https://api.github.com/repos/testorg/repo-two/tags",
                "teams_url": "https://api.github.com/repos/testorg/repo-two/teams",
                "topics": [],
                "trees_url": "https://api.github.com/repos/testorg/repo-two/git/trees{/sha}",
                "updated_at": "2020-02-03T00:00:00Z",
                "url": "https://api.github.com/repos/testorg/repo-two",
                "visibility": "public",
                "watchers": 0,
                "watchers_count": 0,
                "web_commit_signoff_required": false
              }
            ]
        """

        private const val LABELS_REPO_ONE_JSON = """
            [
              {"id": 101, "node_id": null, "url": "https://api.github.com/labels/101", "name": "help wanted", "color": "eee", "default": false, "description": ""},
              {"id": 102, "node_id": null, "url": "https://api.github.com/labels/102", "name": "good first issue", "color": "ccc", "default": false, "description": ""}
            ]
        """

        private const val LABELS_REPO_TWO_JSON = """
            [
              {"id": 201, "node_id": null, "url": "https://api.github.com/labels/201", "name": "maintenance", "color": "fff", "default": false, "description": ""}
            ]
        """

        private const val ISSUES_REPO_ONE_JSON = """
            [
              {
                "active_lock_reason": null,
                "assignee": null,
                "assignees": [],
                "author_association": "CONTRIBUTOR",
                "body": "Some description",
                "closed_at": null,
                "closed_by": null,
                "comments": 2,
                "comments_url": "https://api.github.com/repos/testorg/repo-one/issues/1/comments",
                "created_at": "2020-03-01T00:00:00Z",
                "draft": false,
                "events_url": "https://api.github.com/repos/testorg/repo-one/issues/1/events",
                "html_url": "https://github.com/testorg/repo-one/issues/1",
                "id": 1001,
                "labels": [
                  {"id": 101, "node_id": null, "url": "https://api.github.com/labels/101", "name": "help wanted", "color": "eee", "default": false, "description": ""}
                ],
                "labels_url": "https://api.github.com/repos/testorg/repo-one/issues/1/labels{/name}",
                "locked": false,
                "milestone": null,
                "node_id": "I_kwDOissue1",
                "number": 1,
                "performed_via_github_app": null,
                "pull_request": null,
                "reactions": null,
                "repository_url": "https://api.github.com/repos/testorg/repo-one",
                "state": "open",
                "state_reason": null,
                "timeline_url": "https://api.github.com/repos/testorg/repo-one/issues/1/timeline",
                "title": "An easy first contribution",
                "updated_at": "2020-03-02T00:00:00Z",
                "url": "https://api.github.com/repos/testorg/repo-one/issues/1",
                "user": null
              }
            ]
        """
    }

}
