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

1. name
2. project_id
3. list\<version\>
4. list <AccessGroup>
5. created
6. updated

## Version
1. version_id
2. version
3. list\<sprint\>

## Sprint
1. list\<story\>
1. start
2. end
3. status
4. created
5. updated

## Story
1. list\<task\>
2. start
3. end
4. status
5. created
6. updated

## Task
1. task-id
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
