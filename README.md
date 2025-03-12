# Target
minimalistic project effort estimation and progress tracking and release date interpolation.

# Requirements

## Phase 1
1. Basic functionality
2. Product, Version, project
3. project list.
4. every project has list of sprints.
5. every sprint contains stories and tasks.
6. every task has start, end, effort estimation, effort worked, dependency to other tasks or stories.
7. gantt chart generation with resource conflict visualization.
8. 1. Gantt task only on working days.
   2. User name before gantt task with rounded corners.
   3. X-axis calendar make none working day gray.
9. burn down chart for every sprint.
10. keep number of clicks to minimum for daily work of developer.
11. Close project Release Date.
12. National Holidays
13. data scenario simulation generator
14. 1. Simulator Write the use case as a Story in the project or product

## Phase 2
1. Authentication via oidc
2. Audit logs
3. User availability time-frames
4. User location time-frames
5. User work week time-frames
6. User Work hours time-frames

## Phase 3
1. Authorization, access control using user groups on project level.
2. Admin hub        

## Phase 4
1. Performance
2. Live updates to your inputs
3. Live response to your Input.
4. 4product page.


# Design Philosophy
1. As simple as possible, as complex as nessesary .
2. 1. backup the development with unit tests.
3. create data generators that can be used in unit tests.
4. written in Java + spring boot + Vaadin.
5. minimalistic project status tracking within one single server.
6. simple local database, but keep option to switch to other databases.

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

Ideas
    Take a look how jira is sending fields to the client and replicate if it is good.
    Projects can be locked for change, which will lock start/end dates and all milestones
    project priority can be changed by moving them within the list
    sprint priority can be changed by moving them within the list
    How can we see jpa sql code to determine if it is using too many joins?
        hibernate.show_sql=true
