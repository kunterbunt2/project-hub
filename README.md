[![Tests](https://img.shields.io/github/actions/workflow/status/kunterbunt2/project-hub/maven-build.yml?label=tests)](https://github.com/kunterbunt2/project-hub/actions/workflows/maven-build.yml)
[![License](https://img.shields.io/github/license/kunterbunt2/project-hub)](https://github.com/kunterbunt2/project-hub/blob/main/LICENSE)

# kassandra

tiny project management server.<br>
project effort estimation and progress tracking and release date interpolation open source server.

## features

tbd

[Requirements](https://github.com/kunterbunt2/project-hub/wiki/Requirements)

[Limitations](https://github.com/kunterbunt2/project-hub/wiki/Limitations)

[Design](https://github.com/kunterbunt2/project-hub/wiki/Design)

[ER Diagram](https://github.com/kunterbunt2/project-hub/wiki/ER-Diagram)

# Screenshots

![gantt-03](https://raw.githubusercontent.com/wiki/kunterbunt2/project-hub/gantt/gantt_03-3-gant-chart.svg)

![burn-down-03](https://raw.githubusercontent.com/wiki/kunterbunt2/project-hub/burn-down/gantt_03-3-burn-down.svg)

![Christopher Paul.de.nw](https://raw.githubusercontent.com/wiki/kunterbunt2/project-hub/calendar/Christopher%20Paul.de.nw.svg)

# Status

## Locations

1. we can **create**, **update**, **add**, **delete** `locations`.

## User

1. we can **create** `users`.

# Next Steps

1. handle users leaving.
2. add user availability & vacations.
3. introduce sprint, story and task1 as a preliminary structure to allow unit tests.
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

1. users are retired by their name instead of their email address.
2. user email addresses must be unique.
1. LocationDialog not showing errors in dialog.
2. AvailabilityTest.userSecurity() generates several exceptions on server side that the test does not catch.
3. gantt calendar too light.
4. gantt calendar should be using sprint calendar.

# License

[Apache License, version 2.0](https://github.com/kunterbunt2/project-hub/blob/main/LICENSE)
