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
import de.bushnaq.abdalla.projecthub.ui.dialog.AvailabilityDialog;
import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.HumanizedSeleniumHandler;
import de.bushnaq.abdalla.projecthub.ui.view.AvailabilityListView;
import de.bushnaq.abdalla.projecthub.ui.view.LoginView;
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
public class UserAvailabilityInstructionVideo extends AbstractUiTestUtil {
    public static final  NarratorAttribute          INTENSE  = new NarratorAttribute().withExaggeration(.7f).withCfgWeight(.3f).withTemperature(1f)/*.withVoice("chatterbox")*/;
    public static final  NarratorAttribute          NORMAL   = new NarratorAttribute().withExaggeration(.5f).withCfgWeight(.5f).withTemperature(1f)/*.withVoice("chatterbox")*/;
    // Start Keycloak container with realm configuration
    @Container
    private static final KeycloakContainer          keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.1")
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
    @Autowired
    private              LocationListViewTester     locationListViewTester;
    @Autowired
    private              OffDayListViewTester       offDayListViewTester;
    @Autowired
    private              ProductListViewTester      productListViewTester;
    @Autowired
    private              HumanizedSeleniumHandler   seleniumHandler;
    @Autowired
    private              SprintListViewTester       sprintListViewTester;
    @Autowired
    private              TaskListViewTester         taskListViewTester;
    @Autowired
    private              UserListViewTester         userListViewTester;
    @Autowired
    private              VersionListViewTester      versionListViewTester;

    @ParameterizedTest
    @MethodSource("listRandomCases")
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void createVideo(RandomCase randomCase, TestInfo testInfo) throws Exception {

        TestInfoUtil.setTestMethod(testInfo, testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        TestInfoUtil.setTestCaseIndex(testInfo, randomCase.getTestCaseIndex());
        setTestCaseName(this.getClass().getName(), testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        generateProductsIfNeeded(testInfo, randomCase);
        Narrator paul = Narrator.withChatterboxTTS("tts/" + testInfo.getTestClass().get().getSimpleName());
        seleniumHandler.getAndCheck("http://localhost:" + "8080" + "/ui/" + LoginView.ROUTE);
        seleniumHandler.showOverlay("Kassandra User Availability", "Introduction Video");
        seleniumHandler.startRecording(testInfo.getTestClass().get().getSimpleName(), "User Availability Introduction Video");
        seleniumHandler.wait(3000);
        paul.narrateAsync(NORMAL, "Hi everyone, Christopher Paul here from kassandra.org. Today we're going to learn about User Availability management in Kassandra. User availability defines what percentage of your time you can dedicate to project work. This is essential for accurate sprint planning and capacity calculations.");
        seleniumHandler.hideOverlay();
        productListViewTester.switchToProductListViewWithOidc("christopher.paul@kassandra.org", "password", "../project-hub.wiki/screenshots/login-view.png", testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));

        //---------------------------------------------------------------------------------------
        // Navigate to Availability Page
        //---------------------------------------------------------------------------------------

        seleniumHandler.setHighlightEnabled(true);//highlight elements starting now
        paul.narrateAsync(NORMAL, "Let's open the user menu and navigate to the User Availability page.");
        seleniumHandler.click(MainLayout.ID_USER_MENU);
        seleniumHandler.click(MainLayout.ID_USER_MENU_AVAILABILITY);

        //---------------------------------------------------------------------------------------
        // Explain Availability Page Purpose
        //---------------------------------------------------------------------------------------

        seleniumHandler.highlight(AvailabilityListView.AVAILABILITY_LIST_PAGE_TITLE);
        paul.narrate(NORMAL, "This page shows your availability history. Each availability record defines what percentage of your working time is dedicated to project tasks during a specific time period.");
        seleniumHandler.highlight(AvailabilityListView.AVAILABILITY_GRID);
        paul.narrate(NORMAL, "For example, one hundred percent means you're fully available, fifty percent means you're working half time, and values over one hundred percent represent overtime work.");

        //---------------------------------------------------------------------------------------
        // Explain Existing Records
        //---------------------------------------------------------------------------------------

        paul.narrate(NORMAL, "I have one availability record starting from my first working day, set to fifty percent. This means Kassandra assumes I'm working half time on project work.");
        paul.narrate(NORMAL, "However, my capacity might change over time.");

        //---------------------------------------------------------------------------------------
        // Why Multiple Availability Records
        //---------------------------------------------------------------------------------------

        paul.narrate(NORMAL, "Why do we need an availability history? Well, your capacity can change over time.");
        paul.narrate(NORMAL, "You might switch to part-time work, take on management responsibilities that reduce your project time, or work overtime during critical project phases.");
        paul.narrate(NORMAL, "Kassandra uses this information to accurately calculate team capacity, plan realistic sprints, and estimate project release dates.");

        //---------------------------------------------------------------------------------------
        // Create New Availability - Intro
        //---------------------------------------------------------------------------------------

        seleniumHandler.highlight(AvailabilityListView.CREATE_AVAILABILITY_BUTTON);
        paul.narrate(NORMAL, "Let me show you how to add a new availability record. I've been working at fifty percent project availability because I was handling third level support tasks.");
        paul.narrate(NORMAL, "Good news - those support responsibilities are being reassigned, so I can increase my availability to eighty percent starting next month.");

        //---------------------------------------------------------------------------------------
        // Create New Availability - Dialog
        //---------------------------------------------------------------------------------------

        paul.narrateAsync(NORMAL, "Click the create button.");
        seleniumHandler.click(AvailabilityListView.CREATE_AVAILABILITY_BUTTON);

        paul.narrateAsync(NORMAL, "First, we select the start date - this is when the new availability becomes effective. Let's set it to June first, twenty twenty-five.");
        final LocalDate newAvailabilityStartDate = LocalDate.of(2025, 6, 1);
        seleniumHandler.setDatePickerValue(AvailabilityDialog.AVAILABILITY_START_DATE_FIELD, newAvailabilityStartDate);

        paul.narrateAsync(NORMAL, "Next, we enter the availability percentage. I'll enter eighty to represent eighty percent availability.");
        seleniumHandler.setTextField(AvailabilityDialog.AVAILABILITY_PERCENTAGE_FIELD, "80");

        paul.narrateAsync(NORMAL, "Now click Save to create the availability record.");
        seleniumHandler.click(AvailabilityDialog.CONFIRM_BUTTON);

        //---------------------------------------------------------------------------------------
        // Verify Creation & Explain Impact
        //---------------------------------------------------------------------------------------

        seleniumHandler.wait(1000);
        seleniumHandler.highlight(AvailabilityListView.AVAILABILITY_GRID_START_DATE_PREFIX + "2025-06-01");
        paul.narrate(NORMAL, "Perfect! The new availability record is now visible in the grid.");
        paul.narrate(NORMAL, "Starting June first, Kassandra will use this new availability to calculate the number of days I need to finish my estimated tasks.");
        paul.narrate(NORMAL, "For example, a task that would take me ten days at one hundred percent availability will now take twelve and a half days.");
        paul.narrate(NORMAL, "My old availability record automatically ends the day before the new one begins, ensuring accurate capacity tracking throughout the year.");

        //---------------------------------------------------------------------------------------
        // Mention Edit/Delete Capabilities
        //---------------------------------------------------------------------------------------

        paul.narrate(NORMAL, "With the little notepad and trashcan icons, on the right side, you can edit or delete any existing availability record.");

        //---------------------------------------------------------------------------------------
        // Mention Minimum Availability Requirement
        //---------------------------------------------------------------------------------------

        seleniumHandler.highlight(AvailabilityListView.AVAILABILITY_GRID);
        paul.narrate(NORMAL, "However, you cannot delete your only availability record - Kassandra requires at least one availability record to calculate capacity properly.");

        //---------------------------------------------------------------------------------------
        // Closing
        //---------------------------------------------------------------------------------------

        paul.narrate(NORMAL, "That's all there is to managing your availability in Kassandra. Remember, keeping your availability up to date ensures accurate capacity planning and realistic sprint commitments. Thanks for watching!");

        seleniumHandler.waitUntilBrowserClosed(5000);
    }

    // Method to get the public-facing URL, fixing potential redirect issues
    private static String getPublicFacingUrl(KeycloakContainer container) {
        return String.format("http://%s:%s",
                container.getHost(),
                container.getMappedPort(8080));
    }

    private static List<RandomCase> listRandomCases() {
        RandomCase[] randomCases = new RandomCase[]{//
                new RandomCase(1, LocalDate.parse("2025-05-01"), Duration.ofDays(10), 0, 0, 0, 0, 0, 6, 8, 12, 6, 13)//
        };
        return Arrays.stream(randomCases).toList();
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

