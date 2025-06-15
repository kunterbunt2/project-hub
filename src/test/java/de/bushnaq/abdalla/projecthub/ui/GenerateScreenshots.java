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

package de.bushnaq.abdalla.projecthub.ui;

import de.bushnaq.abdalla.projecthub.ui.common.*;
import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.util.*;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GenerateScreenshots extends AbstractUiTestUtil {
    @Autowired
    private ProductViewTester productViewTester;
    @Autowired
    private ProjectViewTester projectViewTester;
    @Autowired
    private SeleniumHandler   seleniumHandler;
    @Autowired
    private SprintViewTester  sprintViewTester;
    @Autowired
    private UserViewTester    userViewTester;
    @Autowired
    private VersionViewTester versionViewTester;


    private static List<RandomCase> listRandomCases() {
        RandomCase[] randomCases = new RandomCase[]{//
//                new RandomCase(1, 10, 2, 1, 2, 1),//
//                new RandomCase(2, 10, 3, 2, 3, 1)//
                new RandomCase(2, 4, 4, 4, 4, 10, 3, 4, 3, 1)//
        };
        return Arrays.stream(randomCases).toList();
    }

    private void printAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String password = "test-password";
            logger.info("Running demo with user: {} and password: {}", username, password);
        } else {
            logger.warn("No authenticated user found. Running demo without authentication.");
        }
    }

    /**
     * Takes screenshots of Product create, edit and delete dialogs
     */
    private void takeProductDialogScreenshots() {
        // Create product dialog
        seleniumHandler.click(ProductListView.CREATE_PRODUCT_BUTTON);
        seleniumHandler.waitForElementToBeClickable(ProductDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ProductDialog.PRODUCT_DIALOG), ProductDialog.PRODUCT_DIALOG, "../project-hub.wiki/screenshots/product-create-dialog.png");
        seleniumHandler.click(ProductDialog.CANCEL_BUTTON);

        // Edit product dialog - open action menu first, then edit
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + "Product-1");
        seleniumHandler.click(ProductListView.PRODUCT_GRID_EDIT_BUTTON_PREFIX + "Product-1");
        seleniumHandler.waitForElementToBeClickable(ProductDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ProductDialog.PRODUCT_DIALOG), ProductDialog.PRODUCT_DIALOG, "../project-hub.wiki/screenshots/product-edit-dialog.png");
        seleniumHandler.click(ProductDialog.CANCEL_BUTTON);

        // Delete product dialog - open action menu first, then delete
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + "Product-1");
        seleniumHandler.click(ProductListView.PRODUCT_GRID_DELETE_BUTTON_PREFIX + "Product-1");
        seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/product-delete-dialog.png");
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
    }

    /**
     * Takes screenshots of Project create, edit and delete dialogs
     */
    private void takeProjectDialogScreenshots() {
        // Create project dialog
        seleniumHandler.click(ProjectListView.CREATE_PROJECT_BUTTON_ID);
        seleniumHandler.waitForElementToBeClickable(ProjectDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ProjectDialog.PROJECT_DIALOG), ProjectDialog.PROJECT_DIALOG, "../project-hub.wiki/screenshots/project-create-dialog.png");
        seleniumHandler.click(ProjectDialog.CANCEL_BUTTON);

        // Edit project dialog - open action menu first, then edit
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + "Project-0");
        seleniumHandler.click(ProjectListView.PROJECT_GRID_EDIT_BUTTON_PREFIX + "Project-0");
        seleniumHandler.waitForElementToBeClickable(ProjectDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ProjectDialog.PROJECT_DIALOG), ProjectDialog.PROJECT_DIALOG, "../project-hub.wiki/screenshots/project-edit-dialog.png");
        seleniumHandler.click(ProjectDialog.CANCEL_BUTTON);

        // Delete project dialog - open action menu first, then delete
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + "Project-0");
        seleniumHandler.click(ProjectListView.PROJECT_GRID_DELETE_BUTTON_PREFIX + "Project-0");
        seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/project-delete-dialog.png");
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
    }

    @ParameterizedTest
    @MethodSource("listRandomCases")
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void takeScreenshots(RandomCase randomCase, TestInfo testInfo) throws Exception {
        // Set browser window to a fixed size for consistent screenshots
        seleniumHandler.setWindowSize(1280, 1024);

//        printAuthentication();
        TestInfoUtil.setTestMethod(testInfo, testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        TestInfoUtil.setTestCaseIndex(testInfo, randomCase.getTestCaseIndex());
        setTestCaseName(this.getClass().getName(), testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        generateProducts(testInfo, randomCase);

        productViewTester.switchToProductListView("../project-hub.wiki/screenshots/login.png");
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/product-list-view.png");
        takeProductDialogScreenshots();
        productViewTester.selectProduct("Product-0");
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/version-list-view.png");
        takeVersionDialogScreenshots();
        versionViewTester.selectVersion("1.0.0");
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/project-list-view.png");
        takeProjectDialogScreenshots();
        projectViewTester.selectProject("Project-0");
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/sprint-list-view.png");
        takeSprintDialogScreenshots();
        sprintViewTester.selectSprint("sprint-0");
        seleniumHandler.waitForElementToBeClickable(SprintQualityBoard.GANTT_CHART);
        seleniumHandler.waitForElementToBeClickable(SprintQualityBoard.BURNDOWN_CHART);
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/sprint-view.png");

        userViewTester.switchToUserListView();
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/user-list-view.png");
        takeUserDialogScreenshots();

        seleniumHandler.waitUntilBrowserClosed(5000);
    }

    /**
     * Takes screenshots of Sprint create, edit and delete dialogs
     */
    private void takeSprintDialogScreenshots() {
        // Create sprint dialog
        seleniumHandler.click(SprintListView.CREATE_SPRINT_BUTTON);
        seleniumHandler.waitForElementToBeClickable(SprintDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(SprintDialog.SPRINT_DIALOG), SprintDialog.SPRINT_DIALOG, "../project-hub.wiki/screenshots/sprint-create-dialog.png");
        seleniumHandler.click(SprintDialog.CANCEL_BUTTON);

        // Edit sprint dialog - open action menu first, then edit
        seleniumHandler.click(SprintListView.SPRINT_GRID_ACTION_BUTTON_PREFIX + "sprint-0");
        seleniumHandler.click(SprintListView.SPRINT_GRID_EDIT_BUTTON_PREFIX + "sprint-0");
        seleniumHandler.waitForElementToBeClickable(SprintDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(SprintDialog.SPRINT_DIALOG), SprintDialog.SPRINT_DIALOG, "../project-hub.wiki/screenshots/sprint-edit-dialog.png");
        seleniumHandler.click(SprintDialog.CANCEL_BUTTON);

        // Delete sprint dialog - open action menu first, then delete
        seleniumHandler.click(SprintListView.SPRINT_GRID_ACTION_BUTTON_PREFIX + "sprint-0");
        seleniumHandler.click(SprintListView.SPRINT_GRID_DELETE_BUTTON_PREFIX + "sprint-0");
        seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/sprint-delete-dialog.png");
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
    }

    /**
     * Takes screenshots of User create, edit and delete dialogs
     */
    private void takeUserDialogScreenshots() {
        // Create user dialog
        seleniumHandler.click(UserListView.CREATE_USER_BUTTON);
        seleniumHandler.waitForElementToBeClickable(UserDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(UserDialog.USER_DIALOG), UserDialog.USER_DIALOG, "../project-hub.wiki/screenshots/user-create-dialog.png");
        seleniumHandler.click(UserDialog.CANCEL_BUTTON);

        // Edit user dialog - open action menu first, then edit
        // Get first user in the list
        String firstUser = "Christopher Paul"; // Assuming Christopher Paul exists from the test data
        seleniumHandler.click(UserListView.USER_GRID_ACTION_BUTTON_PREFIX + firstUser);
        seleniumHandler.click(UserListView.USER_GRID_EDIT_BUTTON_PREFIX + firstUser);
        seleniumHandler.waitForElementToBeClickable(UserDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(UserDialog.USER_DIALOG), UserDialog.USER_DIALOG, "../project-hub.wiki/screenshots/user-edit-dialog.png");
        seleniumHandler.click(UserDialog.CANCEL_BUTTON);

        // Delete user dialog - open action menu first, then delete
        seleniumHandler.click(UserListView.USER_GRID_ACTION_BUTTON_PREFIX + firstUser);
        seleniumHandler.click(UserListView.USER_GRID_DELETE_BUTTON_PREFIX + firstUser);
        seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/user-delete-dialog.png");
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
    }

    /**
     * Takes screenshots of Version create, edit and delete dialogs
     */
    private void takeVersionDialogScreenshots() {
        // Create version dialog
        seleniumHandler.click(VersionListView.CREATE_VERSION_BUTTON);
        seleniumHandler.waitForElementToBeClickable(VersionDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(VersionDialog.VERSION_DIALOG), VersionDialog.VERSION_DIALOG, "../project-hub.wiki/screenshots/version-create-dialog.png");
        seleniumHandler.click(VersionDialog.CANCEL_BUTTON);

        // Edit version dialog - open action menu first, then edit
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + "1.0.0");
        seleniumHandler.click(VersionListView.VERSION_GRID_EDIT_BUTTON_PREFIX + "1.0.0");
        seleniumHandler.waitForElementToBeClickable(VersionDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(VersionDialog.VERSION_DIALOG), VersionDialog.VERSION_DIALOG, "../project-hub.wiki/screenshots/version-edit-dialog.png");
        seleniumHandler.click(VersionDialog.CANCEL_BUTTON);

        // Delete version dialog - open action menu first, then delete
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + "1.0.0");
        seleniumHandler.click(VersionListView.VERSION_GRID_DELETE_BUTTON_PREFIX + "1.0.0");
        seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/version-delete-dialog.png");
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
    }
}
