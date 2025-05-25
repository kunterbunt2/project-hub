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

package de.bushnaq.abdalla.projecthub.report.calendar;

import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.report.burndown.TestInfoUtil;
import de.bushnaq.abdalla.projecthub.report.gantt.GanttContext;
import de.bushnaq.abdalla.projecthub.util.AbstractGanttTestUtil;
import de.bushnaq.abdalla.util.Util;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CalendarTest extends AbstractGanttTestUtil {

    /**
     * generates a calendar for every user
     * there is per se no test, just successfully rendering the calendar without exceptions.
     */
    @Test
    public void calendar_01(TestInfo testInfo) throws Exception {
        int testCaseIndex = 1;
        TestInfoUtil.setTestCaseIndex(testInfo, testCaseIndex);
        TestInfoUtil.setTestMethod(testInfo, testInfo.getTestMethod().get().getName() + "-" + testCaseIndex);
        setTestCaseName(this.getClass().getName(), testInfo.getTestMethod().get().getName() + "-" + testCaseIndex);
        generateOneProduct(testInfo);
        addRandomUser();
        addRandomUser();
        addRandomUser();
        addRandomUser();
        addRandomUser();
        addRandomUser();

        GanttContext gc = new GanttContext();
        gc.allUsers    = userApi.getAll();
        gc.allProducts = productApi.getAll();
        gc.allVersions = versionApi.getAll();
        gc.allProjects = projectApi.getAll();
        gc.allSprints  = sprintApi.getAll();
        gc.allTasks    = taskApi.getAll();
        gc.allWorklogs = worklogApi.getAll();
        gc.initialize();

        for (User user : gc.allUsers) {
            CalendarChart chart = new CalendarChart(context, ParameterOptions.getLocalNow(), user, "scheduleWithMargin", context.parameters.graphicsTheme);
            chart.generateImage(Util.generateCopyrightString(ParameterOptions.getLocalNow()), "", testResultFolder);
        }
    }

}
