/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.dao;

import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.rest.debug.DebugUtil;
import de.bushnaq.abdalla.projecthub.util.AbstractGanttTestUtil;
import de.bushnaq.abdalla.util.date.DateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class BurndownTest extends AbstractGanttTestUtil {
    private static final int MAX_DURATION_DAYS      = 10;
    private static final int MAX_NUMBER_OF_FEATURES = 5;
    private static final int MAX_NUMBER_OF_USERS    = 5;
    private static final int MAX_NUMBER_OF_WORK     = 5;
    @Autowired
    DebugUtil debugUtil;

    @Override
    protected void createProductAndUser(TestInfo testInfo) throws Exception {
        //no need to create default product and user
    }

    private static String generateFeatureName(int t) {
        return String.format("Feature-%d", t);
    }

    private void generateTasks(int numberOfUsers, int numberOfFeatures) {
        {
            addRandomUsers(numberOfUsers);
            Product product = addProduct("Product-" + 1);
            Version version = addVersion(product, String.format("1.%d.0", 0));
            Project project = addRandomProject(version);
            sprint = addRandomSprint(project);
        }

        Task startMilestone = addTask(sprint, null, "Start", LocalDateTime.parse("2024-12-15T08:00:00"), Duration.ZERO, null, null, TaskMode.MANUALLY_SCHEDULED, true);
//        List<Task> features = new ArrayList<>();
        for (int f = 0; f < numberOfFeatures; f++) {
            String featureName = generateFeatureName(f);
            Task   feature     = addParentTask(featureName, sprint, null, startMilestone);
//            features.add(feature);
            int numberOfTasks = random.nextInt(MAX_NUMBER_OF_WORK) + 1;
            for (int t = 0; t < numberOfTasks; t++) {
                User   user     = expectedUsers.stream().toList().get(random.nextInt(numberOfUsers));
                String duration = String.format("%dd", random.nextInt(MAX_DURATION_DAYS) + 1);
                String workName = generateWorkName(featureName, t);
                addTask(workName, duration, user, sprint, feature, null);
            }
        }
    }

    private static String generateWorkName(String featureName, int t) {
        String[] workNames = new String[]{"pre-planning", "planning", "analysis", "design", "implementation", "module test", "Functional Test", "System Test", "debugging", "deployment"};
        return String.format("%s-%s", featureName, workNames[t]);
    }

    private void generateWorklogs() {
        final long SECONDS_PER_WORKING_DAY = 75 * 6 * 60;
        final long SECONDS_PER_HOUR        = 60 * 60;
        long       oneDay                  = 75 * SECONDS_PER_HOUR / 10;
        for (LocalDate day = sprint.getStart().toLocalDate(); day.isBefore(sprint.getEnd().toLocalDate().plusDays(1)); day = day.plusDays(1)) {
            for (Task task : sprint.getTasks()) {
                if (task.getChildTasks().isEmpty() && task.getOriginalEstimate() != null && !task.getOriginalEstimate().isZero()) {
                    Number availability = task.getAssignedUser().getAvailabilities().getLast().getAvailability();
                    if (task.getChildTasks().isEmpty()) {
                        if (!day.isBefore(task.getStart().toLocalDate()) && !day.isAfter(task.getFinish().toLocalDate())) {
                            // Day is within task start/finish date range
                            LocalDateTime startOfDay     = day.atStartOfDay().plusHours(8);
                            LocalDateTime endOfDay       = day.atStartOfDay().plusHours(16).plusMinutes(30);
                            LocalDateTime lunchStartTime = DateUtil.calculateLunchStartTime(day.atStartOfDay());
                            LocalDateTime lunchStopTime  = DateUtil.calculateLunchStopTime(day.atStartOfDay());

                            if (task.getEffectiveCalendar().isWorkingDate(day)) {
                                if (task.getStart().isBefore(startOfDay) || task.getStart().isEqual(startOfDay)) {
                                    if (task.getFinish().isAfter(endOfDay) || task.getFinish().isEqual(endOfDay)) {
                                        // we have the whole day
                                        double   fraction = (double) Duration.between(startOfDay, endOfDay).getSeconds() / oneDay;
                                        Duration maxWork  = Duration.ofSeconds((long) ((availability.doubleValue() * SECONDS_PER_WORKING_DAY)));
                                        Duration w        = maxWork;
                                        Worklog  worklog  = addWorklog(task, task.getAssignedUser(), DateUtil.localDateTimeToOffsetDateTime(day.atStartOfDay()), w, task.getName());
//                                    task.setProgress(task.getProgress().intValue() + 1);
                                    }
//                                Worklog worklog = addWorklog(task, task.getAssignedUser(), startOfDay, Duration.ofHours(1), task.getName());
//                                task.setProgress(task.getProgress().intValue() + 1);
                                }
                            }

//                        if (task.getOriginalEstimate() != null) {
//                            long days = task.getOriginalEstimate().toDaysPart();
//                            if (days > 0) {
//
//                                Worklog worklog = addWorklog(task, task.getAssignedUser(), DateUtil.localDateTimeToOffsetDateTime(day.atStartOfDay().plusHours(8)), Duration.ofHours(1), task.getName());
//                                task.setOriginalEstimate(task.getOriginalEstimate().minusDays(1));
//                                Worklog worklog3 = addWorklog(task, task.getAssignedUser(), OffsetDateTime.now(), Duration.ofHours(1), "Implementation 1");
//
////                            task.setProgress(task.getProgress().intValue() + 1);
//                            }
//                        }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void sprint_01(TestInfo testInfo) throws Exception {
        random.setSeed(1);
        int numberOfUsers    = random.nextInt(MAX_NUMBER_OF_USERS) + 2;
        int numberOfFeatures = random.nextInt(MAX_NUMBER_OF_FEATURES) + 1;
        generateTasks(numberOfUsers, numberOfFeatures);
//        ParameterOptions.now = OffsetDateTime.parse("2025-03-01T08:00:00+01:00");
        generateGanttChart(testInfo);
        generateWorklogs();
        generateBurndownChart(testInfo);
    }

}
