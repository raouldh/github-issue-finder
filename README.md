# github-issue-finder
TLDR; 
- Start application.kt with -Dgithub.api.token={YOUR-API-TOKEN}
- GET http://localhost:8080/contribution-issues/spring-projects

## Summary
Little "pet project" for finding GitHub issues that are labelled as open-for-contribution.
Why? It is hard to find issues that are open for contribution because labels in repositories are different.
And there are many GitHub repositories within an organization to search in.

The hardcoded list of "open-for-contribution" labels are based on the spring-cloud and spring-projects organizations

## GET /contribution-issues/{org-name}
Return a list of issues that have a label that is in the list of hardcoded open-for-contribution labels
Note: loading a "big" organization might take some time ;)
- Example url: http://localhost:8080/contribution-issues/spring-cloud

## GET /labels/{org-name}
Returns a list of all issue-labels for given organization. 
This list was used determine the hardcoded list op open-for-contribution labels
- Example url: http://localhost:8080/labels/spring-cloud