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

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.bushnaq.abdalla.projecthub.ai.narrator.Narrator;
import de.bushnaq.abdalla.projecthub.ai.narrator.NarratorAttribute;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
import de.bushnaq.abdalla.projecthub.ui.dialog.FeatureDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.ProductDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.SprintDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.VersionDialog;
import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.RenderUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.ui.view.*;
import de.bushnaq.abdalla.projecthub.ui.view.util.*;
import de.bushnaq.abdalla.projecthub.util.RandomCase;
import de.bushnaq.abdalla.projecthub.util.TestInfoUtil;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=8080",
                "spring.profiles.active=test",
                // Disable basic authentication for these tests
                "spring.security.basic.enabled=false"
        }
)
@AutoConfigureMockMvc
@Transactional
@Testcontainers
@Disabled
public class GenerateOneSprint extends AbstractUiTestUtil {
    //    public static final  float                      EXAGGERATE_LOW    = 0.25f;
//    public static final  float                      EXAGGERATE_NORMAL = 0.3f;
    public static final  NarratorAttribute          INTENSE     = new NarratorAttribute().withExaggeration(.7f).withCfgWeight(.3f).withTemperature(1f)/*.withVoice("chatterbox")*/;
    public static final  NarratorAttribute          NORMAL      = new NarratorAttribute().withExaggeration(.5f).withCfgWeight(.5f).withTemperature(1f)/*.withVoice("chatterbox")*/;
    // Start Keycloak container with realm configuration
    @Container
    private static final KeycloakContainer          keycloak    = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.1")
            .withRealmImportFile("keycloak/project-hub-realm.json")
            .withAdminUsername("admin")
            .withAdminPassword("admin")
            // Expose on a fixed port for more reliable testing
            .withExposedPorts(8080, 8443)
            // Add debugging to see container output
            .withLogConsumer(outputFrame -> System.out.println("Keycloak: " + outputFrame.getUtf8String()))
            // Make Keycloak accessible from outside the container
            .withEnv("KC_HOSTNAME_STRICT", "false")
            .withEnv("KC_HOSTNAME_STRICT_HTTPS", "false");
    @Autowired
    private              AvailabilityListViewTester availabilityListViewTester;
    @Autowired
    private              FeatureListViewTester      featureListViewTester;
    private              String                     featureName;
    //    private final        LocalDate                  firstDay        = LocalDate.of(2025, 6, 1);
//    private final        LocalDate                  firstDayRecord1 = LocalDate.of(2025, 8, 1);
//    private final        LocalDate                  lastDay         = LocalDate.of(2025, 6, 1);
//    private final        LocalDate                  lastDayRecord1  = LocalDate.of(2025, 8, 5);
    @Autowired
    private              LocationListViewTester     locationListViewTester;
    @Autowired
    private              OffDayListViewTester       offDayListViewTester;
    @Autowired
    private              ProductListViewTester      productListViewTester;
    private              String                     productName;
    @Autowired
    private              SeleniumHandler            seleniumHandler;
    @Autowired
    private              SprintListViewTester       sprintListViewTester;
    private              String                     sprintName;
    @Autowired
    private              TaskListViewTester         taskListViewTester;
    private              String                     taskName;
    private final        OffDayType                 typeRecord1 = OffDayType.VACATION;
    @Autowired
    private              UserListViewTester         userListViewTester;
    private              String                     userName;
    @Autowired
    private              VersionListViewTester      versionListViewTester;
    private              String                     versionName;

    @ParameterizedTest
    @MethodSource("listRandomCases")
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void createASprint(RandomCase randomCase, TestInfo testInfo) throws Exception {
        // Set browser window to a fixed size for consistent screenshots
//        seleniumHandler.setWindowSize(1024, 800);
        seleniumHandler.setWindowSize(1800, 1300);
//        seleniumHandler.setTypingDelayMillis(50);

//        printAuthentication();
        TestInfoUtil.setTestMethod(testInfo, testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        TestInfoUtil.setTestCaseIndex(testInfo, randomCase.getTestCaseIndex());
        setTestCaseName(this.getClass().getName(), testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        generateProductsIfNeeded(testInfo, randomCase);
        seleniumHandler.startRecording(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
        Narrator narrator = Narrator.withChatterboxTTS("tts/" + testInfo.getTestClass().get().getSimpleName());
        productName = "Jupiter";
        versionName = "1.0.0";
        featureName = "Property request api";
        sprintName  = "Minimum Viable Product";
        taskName    = nameGenerator.generateSprintName(0);


        narrator.narrateAsync(NORMAL, "Good morning, my name is Christopher Paul. I am the product manager of Kassandra and I will be demonstrating the latest alpha version of the Kassandra project server to you today.");
        productListViewTester.switchToProductListViewWithOidc("christopher.paul@kassandra.org", "password", "../project-hub.wiki/screenshots/login-view.png", testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));

        //---------------------------------------------------------------------------------------..
        // Products Page
        //---------------------------------------------------------------------------------------..
        narrator.narrate(NORMAL, "Kassandra is a project planning and progress tracking server targeting small to medium team sizes. It is a open source project and has an Apachee two dot zero license.").pause();
        narrator.narrate(NORMAL, "Kassandra supports OIDC authentication and authorization. I just logged into the server using my kassandra dot org ID.").pause();
        narrator.narrate(NORMAL, "The first page you see when you log into the server is the Products page where all Products are listed.").pause();
        //---------------------------------------------------------------------------------------..
        // Create a Product
        //---------------------------------------------------------------------------------------..
        narrator.narrate(NORMAL, "Lets start by adding a new product by selecting the Create button.");
        seleniumHandler.click(ProductListView.CREATE_PRODUCT_BUTTON);
        narrator.narrate(INTENSE, "Lets call it Jupiter!").pause();
        seleniumHandler.setTextField(ProductDialog.PRODUCT_NAME_FIELD, productName);
        narrator.narrate(NORMAL, "Select Save to close the dialog and persist our product.").pause();
        seleniumHandler.click(ProductDialog.CONFIRM_BUTTON);
        narrator.narrate(INTENSE, "And we got ourself a new product!").pause();


        narrator.narrate(NORMAL, "With the little notepad and trashcan icons, on the right side, you can edit or delete your product.").pause();
        narrator.narrate(NORMAL, "Lets select our product...");
        productListViewTester.selectProduct(productName);

        //---------------------------------------------------------------------------------------..
        // Versions Page
        //---------------------------------------------------------------------------------------..
        narrator.narrate(NORMAL, "This takes us to the Versions Page.").pause();
        narrator.narrate(NORMAL, "Every Product can have any number of versions.").pause();
        narrator.narrate(NORMAL, "Jupiter is a totally new product, so lets create a first version for it.");
        narrator.narrate(NORMAL, "Select the Create button...");
        //---------------------------------------------------------------------------------------..
        // Create a Version
        //---------------------------------------------------------------------------------------..
        seleniumHandler.click(VersionListView.CREATE_VERSION_BUTTON);
        narrator.narrate(NORMAL, "Lets use the obvious. One, dot, zero, dot, zero.");
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, versionName);
        narrator.narrateAsync(NORMAL, "Select Save to close the dialog and persist our version.");
        seleniumHandler.click(VersionDialog.CONFIRM_BUTTON);
        narrator.narrate(INTENSE, "And we got ourself a new version!").longPause();
        narrator.narrate(NORMAL, "The little notepad and trashcan icons, on the right side, can be used to edit or delete your version.").pause();
        narrator.narrate(NORMAL, "Lets select our version.");
        versionListViewTester.selectVersion(versionName);

        //---------------------------------------------------------------------------------------..
        // Features Page
        //---------------------------------------------------------------------------------------..
        narrator.narrate(NORMAL, "This takes us to the Features Page. Features are what we actually want to plan and track, although they are split into one or more sprints.").pause();
        narrator.narrate(NORMAL, "Every product version can have any number of features.").pause();
        narrator.narrate(NORMAL, "Lets assume Jupiter is a server that keeps track of micro services configurations.").pause();
        narrator.narrate(NORMAL, "So the first feature would be a rest API that supports retrieving configurations.").pause();
        narrator.narrate(NORMAL, "Select the Create button...");
        //---------------------------------------------------------------------------------------..
        // Create Feature
        //---------------------------------------------------------------------------------------..
        seleniumHandler.click(FeatureListView.CREATE_FEATURE_BUTTON_ID);
        narrator.narrate(NORMAL, "Lets call the feature 'Property request API'.");
        seleniumHandler.setTextField(FeatureDialog.FEATURE_NAME_FIELD, featureName);
        narrator.narrateAsync(NORMAL, "Select Save to close the dialog and persist our feature.");
        seleniumHandler.click(FeatureDialog.CONFIRM_BUTTON);
        narrator.narrate(INTENSE, "Jupiter has its first feature!").longPause();
        narrator.narrate(NORMAL, "Again, as in the other pages, the little notepad and trashcan icons, on the right side, can be used to edit or delete your feature.").pause();
        narrator.narrate(NORMAL, "Lets select our feature...");
        featureListViewTester.selectFeature(featureName);

        //---------------------------------------------------------------------------------------..
        // Sprints Page
        //---------------------------------------------------------------------------------------..
        narrator.narrate(NORMAL, "We are now on the Sprints page of our product. On this page we however only see sprints related to the Feature we just selected.").pause();
        narrator.narrate(NORMAL, "Lets create a sprint for our feature and just call it: Minimum Viable Product.").pause();
        narrator.narrate(NORMAL, "Select the Create button.");
        //---------------------------------------------------------------------------------------..
        // Create a Sprint
        //---------------------------------------------------------------------------------------..
        seleniumHandler.click(SprintListView.CREATE_SPRINT_BUTTON);
        seleniumHandler.setTextField(SprintDialog.SPRINT_NAME_FIELD, sprintName);
        narrator.narrateAsync(NORMAL, "Select Save to close the dialog and persist our sprint.");
        seleniumHandler.click(SprintDialog.CONFIRM_BUTTON);
        narrator.narrate(INTENSE, "That was easy!").longPause();
        narrator.narrate(NORMAL, "Now we need to start planning our sprint. We do this in the Tasks page. Not by selecting the sprint, but configuring it with the small crog icon on the right side.");
        seleniumHandler.click(SprintListView.SPRINT_GRID_CONFIG_BUTTON_PREFIX + sprintName);

        //---------------------------------------------------------------------------------------..
        // Tasks Page
        //---------------------------------------------------------------------------------------..
        narrator.narrate(NORMAL, "This is the page where you plan your sprint including the gantt chart.").pause();
        narrator.narrate(NORMAL, "Lets start by adding a milestone that will fix the starting point of our sprint.").pause();
        narrator.narrate(NORMAL, "Select the Create Milestone button...");
        seleniumHandler.click(TaskListView.CREATE_MILESTONE_BUTTON_ID);
        String milestoneName = "New Milestone-1";
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, milestoneName);
        narrator.narrate(NORMAL, "Lets also create a story. We use stories as containers for the actual work items called tasks.");
        seleniumHandler.click(TaskListView.CREATE_STORY_BUTTON_ID);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, "New Story-2");
        narrator.narrate(NORMAL, "You can see that all the new created items are always added to the end of our table.").pause();
        narrator.narrate(NORMAL, "Lets create 3 additional tasks as work units for our first sprint.");
        seleniumHandler.click(TaskListView.CREATE_TASK_BUTTON_ID);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, "New Task-3");
        seleniumHandler.click(TaskListView.CREATE_TASK_BUTTON_ID);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, "New Task-4");
        seleniumHandler.click(TaskListView.CREATE_TASK_BUTTON_ID);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, "New Task-5");
        narrator.narrate(INTENSE, "Good!").longPause();
        narrator.narrate(NORMAL, "Select the edit button to change to whole table into edit mode...").pause();
        seleniumHandler.click(TaskListView.EDIT_BUTTON_ID);
        narrator.narrate(NORMAL, "We can now edit all valid milestone, story or task cells.").pause();
        narrator.narrate(NORMAL, "Lets give the milestone a fixed start date and time. We want our developers to start working on this Monday first thing in the morning.");
        seleniumHandler.click(TaskListView.TASK_GRID_NAME_PREFIX + milestoneName);


        narrator.narrate(NORMAL, "If you look carefully, you will notice that all three tasks have been assigned to the story.").pause();
        narrator.narrate(NORMAL, "The story is the parent of these tasks.").pause();
        narrator.narrate(NORMAL, "Kassandra does that automatically. All three tasks also are automatically assigned to myself.").pause();
        narrator.narrate(NORMAL, "But, as i am not a developer, we will assign these tasks to a developer.").pause();
        narrator.narrate(NORMAL, "We want our story to depend on our milestone. The story can only start after the milestone.").pause();
        narrator.narrate(NORMAL, "Defining such a dependency between a task or story to other tasks or stories can be done in 3 different ways...");


        sprintListViewTester.selectSprint(sprintName);
        seleniumHandler.waitForElementToBeClickable(RenderUtil.GANTT_CHART);
        seleniumHandler.waitForElementToBeClickable(RenderUtil.BURNDOWN_CHART);

        // After visiting the SprintQualityBoard, go back to SprintListView and use the column config button
        seleniumHandler.click("Sprints (" + sprintName + ")"); // Go back to SprintListView using breadcrumb
        // Find and click the column configuration button
        seleniumHandler.click(SprintListView.SPRINT_GRID_CONFIG_BUTTON_PREFIX + sprintName);
        seleniumHandler.waitForElementToBeClickable(RenderUtil.GANTT_CHART);
        if (false) {


//        userListViewTester.switchToUserListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
//        takeUserDialogScreenshots();

            // Navigate to AvailabilityListView for the current user and take screenshots
//        availabilityListViewTester.switchToAvailabilityListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo), null);
//        takeAvailabilityDialogScreenshots();

            // Navigate to LocationListView for the current user and take screenshots
//        locationListViewTester.switchToLocationListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo), null);
//        takeLocationDialogScreenshots();

            // Navigate to OffDayListView for the current user and take screenshots
//        offDayListViewTester.switchToOffDayListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo), null);
//        takeOffDayDialogScreenshots();
        }
        seleniumHandler.waitUntilBrowserClosed(1000);

    }

    // Method to get the public-facing URL, fixing potential redirect issues
    private static String getPublicFacingUrl(KeycloakContainer container) {
        return String.format("http://%s:%s",
                container.getHost(),
                container.getMappedPort(8080));
    }

    private static List<RandomCase> listRandomCases() {
        RandomCase[] randomCases = new RandomCase[]{//
                new RandomCase(1, LocalDate.parse("2025-10-23"), Duration.ofDays(10), 0, 0, 0, 0, 0, 6, 8, 12, 6, 13)//
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

    // Configure Spring Security to use the Keycloak container
    @DynamicPropertySource
    static void registerKeycloakProperties(DynamicPropertyRegistry registry) {
        // Start the container
        keycloak.start();

        // Get the actual URL that's accessible from outside the container
        String externalUrl = getPublicFacingUrl(keycloak);
        System.out.println("Keycloak External URL: " + externalUrl);

        // Log all container environment information for debugging
        System.out.println("Keycloak Container:");
        System.out.println("  Auth Server URL: " + keycloak.getAuthServerUrl());
        System.out.println("  Container IP: " + keycloak.getHost());
        System.out.println("  HTTP Port Mapping: " + keycloak.getMappedPort(8080));
        System.out.println("  HTTPS Port Mapping: " + keycloak.getMappedPort(8443));

        // Override the authServerUrl with our public-facing URL
        String publicAuthServerUrl = externalUrl + "/";

        // Create properties with the public URL
        Map<String, String> props = new HashMap<>();
        props.put("spring.security.oauth2.client.provider.keycloak.issuer-uri", publicAuthServerUrl + "realms/project-hub-realm");
        props.put("spring.security.oauth2.client.provider.keycloak.authorization-uri", publicAuthServerUrl + "realms/project-hub-realm/protocol/openid-connect/auth");
        props.put("spring.security.oauth2.client.provider.keycloak.token-uri", publicAuthServerUrl + "realms/project-hub-realm/protocol/openid-connect/token");
        props.put("spring.security.oauth2.client.provider.keycloak.user-info-uri", publicAuthServerUrl + "realms/project-hub-realm/protocol/openid-connect/userinfo");
        props.put("spring.security.oauth2.client.provider.keycloak.jwk-set-uri", publicAuthServerUrl + "realms/project-hub-realm/protocol/openid-connect/certs");

        props.put("spring.security.oauth2.client.registration.keycloak.client-id", "project-hub-client");
        props.put("spring.security.oauth2.client.registration.keycloak.client-secret", "test-client-secret");
        props.put("spring.security.oauth2.client.registration.keycloak.scope", "openid,profile,email");
        props.put("spring.security.oauth2.client.registration.keycloak.authorization-grant-type", "authorization_code");
        props.put("spring.security.oauth2.client.registration.keycloak.redirect-uri", "{baseUrl}/login/oauth2/code/{registrationId}");

        props.put("spring.security.oauth2.resourceserver.jwt.issuer-uri", publicAuthServerUrl + "realms/project-hub-realm");

        // Register all properties
        props.forEach((key, value) -> registry.add(key, () -> value));
    }

}
