# Target
minimalistic project effort estimation and progress tracking and release date interpolation.

# Requirements
1. minimalistic project status tracking within one single server.
2. written in Java + spring boot.
3. project list.
4. every project has list of sprints.
5. every sprint contains stories and tasks.
6. every task has start, end, effort estimation, effort worked, dependency to other tasks or stories.
7. gantt chart generation with resource conflict visualization.
8. burn down chart for every sprint.
9. keep number of clicks to minimum for daily work of developer.
10. access control using user groups and oidc authentication on project level.
11. simple local database, but keep option to switch to other databases.

# Design
## Modules
1. data layer and rest service
2. logical layer and rest interface
3. UI layer

## Project

1. project_id
2. name
3. requester
4. list\<version\>
5. list <AccessGroup>
6. created
7. updated

## Version
1. version_id
2. version
3. list\<sprint\>

## Sprint
1. sprint_id
2. list\<story\>
3. start
4. end
5. status
6. created
7. updated

## Story
1. story_id
2. list\<task\>
3. start
4. end
5. status
6. created
7. updated

## Task
1. task_id
2. summary
3. effort estimated
4. effort worked
1. start
2. end
5. status
6. created
7. updated

# Design Philosophy
1. backup the development with unit tests.
2. create data generators that can be used in unit tests.
