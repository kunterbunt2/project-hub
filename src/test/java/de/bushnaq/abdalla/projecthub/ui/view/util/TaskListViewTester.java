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

package de.bushnaq.abdalla.projecthub.ui.view.util;

import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class TaskListViewTester {
    private final SeleniumHandler seleniumHandler;

    /**
     * Constructs a new SprintViewTester with the given Selenium handler.
     *
     * @param seleniumHandler the handler for Selenium operations
     */
    public TaskListViewTester(SeleniumHandler seleniumHandler) {
        this.seleniumHandler = seleniumHandler;
    }

    /**
     * Navigates to the TaskListView by clicking on the "Tasks" link in the breadcrumb
     *
     * @param testClassName The name of the test class
     * @param testCaseName The name of the test case
     * @param url Optional URL to navigate to before clicking the Tasks link. If null, assumes we're already on a page with a breadcrumb.
     */
//    public void switchToTaskListView(String testClassName, String testCaseName, String url) {
//        if (url != null) {
//            seleniumHandler.navigateTo(url);
//        }
//
//        // Find and click the Tasks link in the breadcrumb
//        seleniumHandler.click("Tasks");
//
//        // Wait for the task grid to be visible
//        seleniumHandler.waitForElementToBeClickable(TaskListView.TASK_GRID_NAME_PREFIX);
//    }

//    public void selectTask(String name) {
//        seleniumHandler.selectGridRow(TASK_GRID_NAME_PREFIX, TaskView.class, name);
//    }
}
