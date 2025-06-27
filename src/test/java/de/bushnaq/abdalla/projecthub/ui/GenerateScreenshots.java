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

import de.bushnaq.abdalla.projecthub.ui.dialog.*;
import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.ui.view.*;
import de.bushnaq.abdalla.projecthub.ui.view.util.*;
import de.bushnaq.abdalla.projecthub.util.RandomCase;
import de.bushnaq.abdalla.projecthub.util.TestInfoUtil;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GenerateScreenshots extends AbstractUiTestUtil {
    @Autowired
    private       AvailabilityListViewTester availabilityListViewTester;
    @Autowired
    private       FeatureListViewTester      featureListViewTester;
    private       String                     featureName;
    @Autowired
    private       LocationListViewTester     locationListViewTester;
    @Autowired
    private       ProductListViewTester      productListViewTester;
    private       String                     productName;
    @Autowired
    private       SeleniumHandler            seleniumHandler;
    @Autowired
    private       SprintListViewTester       sprintListViewTester;
    private       String                     sprintName;
    private final LocalDate                  startDate = LocalDate.of(2025, 6, 1);
    @Autowired
    private       UserListViewTester         userListViewTester;
    private       String                     userName;
    @Autowired
    private       VersionListViewTester      versionListViewTester;
    private       String                     versionName;

    private static List<RandomCase> listRandomCases() {
        RandomCase[] randomCases = new RandomCase[]{//
//                new RandomCase(1, 5, 8, 8, 8, 1),//
                new RandomCase(1, LocalDate.parse("2024-12-01"), Duration.ofDays(10), 1, 1, 1, 1, 6, 8, 8, 6, 13)//
//                new RandomCase(2, 10, 3, 2, 3, 1)//
//                new RandomCase(2, 4, 4, 4, 4, 10, 7, 8, 5, 1)//
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
     * Takes screenshots of Availability create, edit and delete dialogs
     */
    private void takeAvailabilityDialogScreenshots() {
        // Create availability dialog
        {
            seleniumHandler.click(AvailabilityListView.CREATE_AVAILABILITY_BUTTON);
            seleniumHandler.waitForElementToBeClickable(AvailabilityDialog.CANCEL_BUTTON); // Wait for dialog
            seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(AvailabilityDialog.AVAILABILITY_DIALOG), AvailabilityDialog.AVAILABILITY_DIALOG, "../project-hub.wiki/screenshots/availability-create-dialog.png");
            seleniumHandler.click(AvailabilityDialog.CANCEL_BUTTON);
        }

        // Edit availability dialog
        // We'll use the current date as a reference to find a record to edit
        {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            seleniumHandler.click(AvailabilityListView.AVAILABILITY_GRID_EDIT_BUTTON_PREFIX + dateStr);
            seleniumHandler.waitForElementToBeClickable(AvailabilityDialog.CANCEL_BUTTON); // Wait for dialog
            seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(AvailabilityDialog.AVAILABILITY_DIALOG), AvailabilityDialog.AVAILABILITY_DIALOG, "../project-hub.wiki/screenshots/availability-edit-dialog.png");
            seleniumHandler.click(AvailabilityDialog.CANCEL_BUTTON);
        }

        // Delete availability dialog
        {
            // create something we can at least try to delete
            availabilityListViewTester.createAvailabilityConfirm(startDate, 50);
            String dateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            seleniumHandler.click(AvailabilityListView.AVAILABILITY_GRID_DELETE_BUTTON_PREFIX + dateStr);
            seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
            seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/availability-delete-dialog.png");
            seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
        }
    }

    /**
     * Takes screenshots of Location create, edit and delete dialogs
     */
    private void takeLocationDialogScreenshots() {
        // Create location dialog
        {
            seleniumHandler.click(LocationListView.CREATE_LOCATION_BUTTON);
            seleniumHandler.waitForElementToBeClickable(LocationDialog.CANCEL_BUTTON); // Wait for dialog
            seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(LocationDialog.LOCATION_DIALOG), LocationDialog.LOCATION_DIALOG, "../project-hub.wiki/screenshots/location-create-dialog.png");
            seleniumHandler.click(LocationDialog.CANCEL_BUTTON);
        }

        // Edit location dialog
        // We'll use the current date as a reference to find a record to edit
        {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            seleniumHandler.click(LocationListView.LOCATION_GRID_EDIT_BUTTON_PREFIX + dateStr);
            seleniumHandler.waitForElementToBeClickable(LocationDialog.CANCEL_BUTTON); // Wait for dialog
            seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(LocationDialog.LOCATION_DIALOG), LocationDialog.LOCATION_DIALOG, "../project-hub.wiki/screenshots/location-edit-dialog.png");
            seleniumHandler.click(LocationDialog.CANCEL_BUTTON);
        }

        // Delete location dialog
        {
            locationListViewTester.createLocationConfirm(startDate, "United States (US)", "California (ca)");
            String dateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            seleniumHandler.click(LocationListView.LOCATION_GRID_DELETE_BUTTON_PREFIX + dateStr);
            seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
            seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/location-delete-dialog.png");
            seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
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
        seleniumHandler.click(ProductListView.PRODUCT_GRID_EDIT_BUTTON_PREFIX + productName);
        seleniumHandler.waitForElementToBeClickable(ProductDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ProductDialog.PRODUCT_DIALOG), ProductDialog.PRODUCT_DIALOG, "../project-hub.wiki/screenshots/product-edit-dialog.png");
        seleniumHandler.click(ProductDialog.CANCEL_BUTTON);

        // Delete product dialog - open action menu first, then delete
        seleniumHandler.click(ProductListView.PRODUCT_GRID_DELETE_BUTTON_PREFIX + productName);
        seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/product-delete-dialog.png");
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
    }

    /**
     * Takes screenshots of Project create, edit and delete dialogs
     */
    private void takeProjectDialogScreenshots() {
        // Create project dialog
        seleniumHandler.click(FeatureListView.CREATE_FEATURE_BUTTON_ID);
        seleniumHandler.waitForElementToBeClickable(FeatureDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(FeatureDialog.FEATURE_DIALOG), FeatureDialog.FEATURE_DIALOG, "../project-hub.wiki/screenshots/feature-create-dialog.png");
        seleniumHandler.click(FeatureDialog.CANCEL_BUTTON);

        // Edit project dialog - open action menu first, then edit
        seleniumHandler.click(FeatureListView.FEATURE_GRID_EDIT_BUTTON_PREFIX + featureName);
        seleniumHandler.waitForElementToBeClickable(FeatureDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(FeatureDialog.FEATURE_DIALOG), FeatureDialog.FEATURE_DIALOG, "../project-hub.wiki/screenshots/feature-edit-dialog.png");
        seleniumHandler.click(FeatureDialog.CANCEL_BUTTON);

        // Delete project dialog - open action menu first, then delete
        seleniumHandler.click(FeatureListView.FEATURE_GRID_DELETE_BUTTON_PREFIX + featureName);
        seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/feature-delete-dialog.png");
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
    }

    @ParameterizedTest
    @MethodSource("listRandomCases")
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void takeScreenshots(RandomCase randomCase, TestInfo testInfo) throws Exception {
        // Set browser window to a fixed size for consistent screenshots
        seleniumHandler.setWindowSize(1800, 1200);

//        printAuthentication();
        TestInfoUtil.setTestMethod(testInfo, testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        TestInfoUtil.setTestCaseIndex(testInfo, randomCase.getTestCaseIndex());
        setTestCaseName(this.getClass().getName(), testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        generateProductsIfNeeded(testInfo, randomCase);
        seleniumHandler.startRecording(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
        userName    = nameGenerator.generateUserName(0);
        productName = nameGenerator.generateProductName(0);
        versionName = nameGenerator.generateVersionName(0);
        featureName = nameGenerator.generateFeatureName(0);
        sprintName  = nameGenerator.generateSprintName(0);

        productListViewTester.switchToProductListView("../project-hub.wiki/screenshots/login.png", testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/product-list-view.png");
        takeProductDialogScreenshots();
        productListViewTester.selectProduct(productName);
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/version-list-view.png");
        takeVersionDialogScreenshots();
        versionListViewTester.selectVersion(versionName);
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/feature-list-view.png");
        takeProjectDialogScreenshots();
        featureListViewTester.selectFeature(featureName);
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/sprint-list-view.png");
        takeSprintDialogScreenshots();
        sprintListViewTester.selectSprint(sprintName);
        seleniumHandler.waitForElementToBeClickable(SprintQualityBoard.GANTT_CHART);
        seleniumHandler.waitForElementToBeClickable(SprintQualityBoard.BURNDOWN_CHART);
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/sprint-quality-board.png");

        userListViewTester.switchToUserListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/user-list-view.png");
        takeUserDialogScreenshots();

        // Navigate to AvailabilityListView for the current user and take screenshots
        availabilityListViewTester.switchToAvailabilityListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo), null);
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/availability-list-view.png");
        takeAvailabilityDialogScreenshots();

        // Navigate to LocationListView for the current user and take screenshots
        locationListViewTester.switchToLocationListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo), null);
        seleniumHandler.takeScreenShot("../project-hub.wiki/screenshots/location-list-view.png");
        takeLocationDialogScreenshots();

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
        seleniumHandler.click(SprintListView.SPRINT_GRID_EDIT_BUTTON_PREFIX + sprintName);
        seleniumHandler.waitForElementToBeClickable(SprintDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(SprintDialog.SPRINT_DIALOG), SprintDialog.SPRINT_DIALOG, "../project-hub.wiki/screenshots/sprint-edit-dialog.png");
        seleniumHandler.click(SprintDialog.CANCEL_BUTTON);

        // Delete sprint dialog - open action menu first, then delete
        seleniumHandler.click(SprintListView.SPRINT_GRID_DELETE_BUTTON_PREFIX + sprintName);
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
        seleniumHandler.click(UserListView.USER_GRID_EDIT_BUTTON_PREFIX + userName);
        seleniumHandler.waitForElementToBeClickable(UserDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(UserDialog.USER_DIALOG), UserDialog.USER_DIALOG, "../project-hub.wiki/screenshots/user-edit-dialog.png");
        seleniumHandler.click(UserDialog.CANCEL_BUTTON);

        // Delete user dialog - open action menu first, then delete
        seleniumHandler.click(UserListView.USER_GRID_DELETE_BUTTON_PREFIX + userName);
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
        seleniumHandler.click(VersionListView.VERSION_GRID_EDIT_BUTTON_PREFIX + versionName);
        seleniumHandler.waitForElementToBeClickable(VersionDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(VersionDialog.VERSION_DIALOG), VersionDialog.VERSION_DIALOG, "../project-hub.wiki/screenshots/version-edit-dialog.png");
        seleniumHandler.click(VersionDialog.CANCEL_BUTTON);

        // Delete version dialog - open action menu first, then delete
        seleniumHandler.click(VersionListView.VERSION_GRID_DELETE_BUTTON_PREFIX + versionName);
        seleniumHandler.waitForElementToBeClickable(ConfirmDialog.CANCEL_BUTTON); // Wait for dialog
        seleniumHandler.takeElementScreenShot(seleniumHandler.findDialogOverlayElement(ConfirmDialog.CONFIRM_DIALOG), ConfirmDialog.CONFIRM_DIALOG, "../project-hub.wiki/screenshots/version-delete-dialog.png");
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
    }
}
