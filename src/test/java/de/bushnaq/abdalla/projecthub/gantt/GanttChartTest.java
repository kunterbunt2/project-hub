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

package de.bushnaq.abdalla.projecthub.gantt;

import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.util.AbstractGanttUtil;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
public class GanttChartTest extends AbstractGanttUtil {

    @Test
    public void gantt_01(TestInfo testInfo) throws Exception {

        //create tasks
        Task task1 = addParentTask("[1] Parent Task", sprint, null, null);
        Task task2 = addTask("[2] Child Task", Duration.ofDays(5), resource1, sprint, task1, null);
        Task task3 = addTask("[3] Child Task", Duration.ofDays(5), resource2, sprint, task1, task2);
        generateGanttChart(testInfo);
    }

    @Test
    public void gantt_02(TestInfo testInfo) throws Exception {

        //create tasks
        Task task1 = addParentTask("[1] Parent Task", sprint, null, null);
        Task task2 = addTask("[2] Child Task ", Duration.ofDays(5), resource1, sprint, task1, null);
        Task task3 = addTask("[3] Child Task ", Duration.ofDays(5), resource2, sprint, task1, task2);

        Task task4 = addParentTask("[4] Parent Task", sprint, null, task1);
        Task task5 = addTask("[5] Child Task ", Duration.ofDays(5), resource1, sprint, task4, null);
        Task task6 = addTask("[6] Child Task ", Duration.ofDays(5), resource2, sprint, task4, task5);

        generateGanttChart(testInfo);
    }

    @Test
    public void gantt_03(TestInfo testInfo) throws Exception {

        //create tasks
        Task task1 = addParentTask("[1] Parent Task", sprint, null, null);
        Task task2 = addTask("[2] Child Task ", Duration.ofDays(5), resource1, sprint, task1, null);
        Task task3 = addTask("[3] Child Task ", Duration.ofDays(5), resource2, sprint, task1, task2);

        Task task4 = addParentTask("[4] Parent Task", sprint, null, task1);
        Task task5 = addTask("[5] Child Task ", Duration.ofDays(5), resource1, sprint, task4, null);
        Task task6 = addTask("[6] Child Task ", Duration.ofDays(5), resource2, sprint, task4, task5);

        Task task7 = addParentTask("[7] Parent Task", sprint, null, task4);
        Task task8 = addTask("[8] Child Task ", Duration.ofDays(5), resource1, sprint, task7, null);
        Task task9 = addTask("[9] Child Task ", Duration.ofDays(5), resource2, sprint, task7, null);

        generateGanttChart(testInfo);
    }

}
