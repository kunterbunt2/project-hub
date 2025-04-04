# Target

minimalistic project effort estimation and progress tracking and release date interpolation open source server.

[Requirements](https://github.com/kunterbunt2/project-hub/wiki/Requirements)

[Design](https://github.com/kunterbunt2/project-hub/wiki/Design)

[ER Diagram](https://github.com/kunterbunt2/project-hub/wiki/ER-Diagram)

# Status

## Locations

1. we can **create**, **update**, **add**, **delete** `locations`.

## User

1. we can **create** `users`.

# Next Steps

1. handle users leaving.
2. add user availability & vacations.
3. introduce sprint, story and task as a preliminary structure to allow unit tests.
4. add unit tests that generate project progress data.

# Design Philosophy

1. As simple as possible, as complex as necessary.
2. backup the development with unit tests.
3. create data generators that can be used in unit tests.
4. written in Java + spring boot + Vaadin.
5. minimalistic project status tracking within one single server.
6. simple local database, but keep option to switch to other databases.

# Ideas

- Take a look how jira is sending fields to the client and replicate if it is good.
- Projects can be locked for change, which will lock start/end dates and all milestones
- project priority can be changed by moving them within the list
- sprint priority can be changed by moving them within the list

# Issues

1. task update will not always persist the sprint_id.
1. rest client still sends null and empty fields as json.

# Limitations

 
