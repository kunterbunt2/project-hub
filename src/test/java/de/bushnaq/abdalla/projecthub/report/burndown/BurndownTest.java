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

package de.bushnaq.abdalla.projecthub.report.burndown;

import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.rest.debug.DebugUtil;
import de.bushnaq.abdalla.projecthub.util.AbstractGanttTestUtil;
import de.bushnaq.abdalla.util.date.DateUtil;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class BurndownTest extends AbstractGanttTestUtil {
    @Autowired
    DebugUtil debugUtil;

    @Override
    protected void createProductAndUser(TestInfo testInfo) throws Exception {
        //no need to create default product and user
    }

    private static String generateFeatureName(int t) {
        return String.format("Feature-%d", t);
    }

    private void generateTasks(RandomCase randomCase) {
        random.setSeed(1);
        int numberOfUsers    = random.nextInt(randomCase.maxNumberOfUsers) + 2;
        int numberOfFeatures = random.nextInt(randomCase.maxNumberOfFeatures) + 1;
        int numberOfTasks    = random.nextInt(randomCase.maxNumberOfWork) + 1;
        {
            addRandomUsers(numberOfUsers);
            Product product = addProduct("Product-" + 1);
            Version version = addVersion(product, String.format("1.%d.0", 0));
            Project project = addRandomProject(version);
            sprint = addRandomSprint(project);
        }

        Task startMilestone = addTask(sprint, null, "Start", LocalDateTime.parse("2024-12-15T08:00:00"), Duration.ZERO, null, null, TaskMode.MANUALLY_SCHEDULED, true);
        for (int f = 0; f < numberOfFeatures; f++) {
            String featureName = generateFeatureName(f);
            Task   feature     = addParentTask(featureName, sprint, null, startMilestone);
            for (int t = 0; t < numberOfTasks; t++) {
                User   user     = expectedUsers.stream().toList().get(random.nextInt(numberOfUsers));
                String duration = String.format("%dd", random.nextInt(randomCase.maxDurationDays) + 1);
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
        Duration   rest                    = Duration.ofSeconds(1);
//        for (LocalDate day = sprint.getStart().toLocalDate(); day.isBefore(sprint.getEnd().toLocalDate().plusDays(1)); day = day.plusDays(1)) {
        for (LocalDate day = sprint.getStart().toLocalDate(); !rest.equals(Duration.ZERO); day = day.plusDays(1)) {
            LocalDateTime startOfDay     = day.atStartOfDay().plusHours(8);
            LocalDateTime endOfDay       = day.atStartOfDay().plusHours(16).plusMinutes(30);
            LocalDateTime lunchStartTime = DateUtil.calculateLunchStartTime(day.atStartOfDay());
            LocalDateTime lunchStopTime  = DateUtil.calculateLunchStopTime(day.atStartOfDay());
            rest = Duration.ZERO;
            for (Task task : sprint.getTasks()) {
                if (task.getChildTasks().isEmpty() && task.getOriginalEstimate() != null && !task.getOriginalEstimate().isZero()) {
                    Number availability = task.getAssignedUser().getAvailabilities().getLast().getAvailability();
                    if (task.getChildTasks().isEmpty()) {
                        if (!day.isBefore(task.getStart().toLocalDate()) /*&& !day.isAfter(task.getFinish().toLocalDate())*/) {
                            // Day is within task start/finish date range

                            if (task.getEffectiveCalendar().isWorkingDate(day)) {
                                if (task.getStart().isBefore(startOfDay) || task.getStart().isEqual(startOfDay)) {
//                                    if (task.getFinish().isAfter(endOfDay) || task.getFinish().isEqual(endOfDay))
                                    {
                                        if (!task.getRemainingEstimate().isZero()) {
                                            // we have the whole day
//                                        double fraction = (double) Duration.between(startOfDay, endOfDay).getSeconds() / oneDay;
                                            double   fraction = 0.8f + random.nextFloat() / 5;
                                            Duration maxWork  = Duration.ofSeconds((long) ((fraction * availability.doubleValue() * SECONDS_PER_WORKING_DAY)));
                                            Duration w        = maxWork;
                                            Duration delta    = task.getRemainingEstimate().minus(w);
                                            if (delta.isZero() || delta.isPositive()) {
                                            } else {
                                                w = task.getRemainingEstimate();
                                            }
                                            Worklog worklog = addWorklog(task, task.getAssignedUser(), DateUtil.localDateTimeToOffsetDateTime(day.atStartOfDay()), w, task.getName());
                                            task.addTimeSpent(w);
                                            task.removeRemainingEstimate(w);
//                                    task.setProgress(task.getProgress().intValue() + 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                rest = rest.plus(task.getRemainingEstimate());//accumulate the rest
            }
        }
        sprint.getTasks().forEach(task -> {
            taskApi.update(task);
        });
        sprintApi.update(sprint);
    }

    private static List<RandomCase> listRandomCases() {
        RandomCase[] randomCases = new RandomCase[]{//
                new RandomCase(10, 2, 1, 2),//
                new RandomCase(10, 3, 2, 3)//
        };
        return Arrays.stream(randomCases).toList();
    }

    @ParameterizedTest
    @MethodSource("listRandomCases")
    public void sprint_01(RandomCase randomCase, TestInfo testInfo) throws Exception {
        generateTasks(randomCase);
        generateGanttChart(testInfo);
        generateWorklogs();
        generateBurndownChart(testInfo);
    }

}
