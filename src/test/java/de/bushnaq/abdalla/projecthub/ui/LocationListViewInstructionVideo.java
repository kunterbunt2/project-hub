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
import de.bushnaq.abdalla.projecthub.ui.dialog.LocationDialog;
import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.HumanizedSeleniumHandler;
import de.bushnaq.abdalla.projecthub.ui.view.LocationListView;
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
public class LocationListViewInstructionVideo extends AbstractUiTestUtil {
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
        seleniumHandler.showOverlay("Kassandra User Location", "Introduction Video");
        seleniumHandler.startRecording(testInfo.getTestClass().get().getSimpleName(), "User Location Introduction Video");
        seleniumHandler.wait(3000);
        paul.narrateAsync(NORMAL, "Hi everyone, Christopher Paul here from kassandra.org. Today we're going to learn about User Location management in Kassandra. User locations are essential for accurate project planning because they determine which public holidays apply to each team member.");
        seleniumHandler.hideOverlay();
        productListViewTester.switchToProductListViewWithOidc("christopher.paul@kassandra.org", "password", "../project-hub.wiki/screenshots/login-view.png", testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));

        //---------------------------------------------------------------------------------------
        // Navigate to Location Page
        //---------------------------------------------------------------------------------------

        seleniumHandler.setHighlightEnabled(true);//highlight elements starting now
        paul.narrateAsync(NORMAL, "Let's open the user menu and navigate to the User Location page.");
        seleniumHandler.click(MainLayout.ID_USER_MENU);
        seleniumHandler.click(MainLayout.ID_USER_MENU_LOCATION);

        //---------------------------------------------------------------------------------------
        // Explain Location Page Purpose
        //---------------------------------------------------------------------------------------

        seleniumHandler.highlight(LocationListView.LOCATION_LIST_PAGE_TITLE);
        paul.narrate(NORMAL, "The User Location page shows your location history. Each location record defines where you were working during a specific time period.");
        seleniumHandler.highlight(LocationListView.LOCATION_GRID);
        paul.narrate(NORMAL, "This is important because Kassandra uses this information to automatically populate the correct public holidays for your region when planning sprints and calculating availability.");

        //---------------------------------------------------------------------------------------
        // Explain Existing Records
        //---------------------------------------------------------------------------------------

        paul.narrate(NORMAL, "You can see that I have a location record starting from my first working day. I'm currently located in Germany, North Rhine-Westphalia.");
        paul.narrate(NORMAL, "This means that all German national holidays and North Rhine-Westphalia regional holidays are automatically included in my calendar and affect my availability calculations.");

        //---------------------------------------------------------------------------------------
        // Why Multiple Locations
        //---------------------------------------------------------------------------------------

        seleniumHandler.highlight(LocationListView.LOCATION_GRID);
        paul.narrate(NORMAL, "Why do we need a location history? Well, if you relocate to a different country or region, the public holidays will change.");
        paul.narrate(NORMAL, "For example, if you move from Germany to the United States, your Thanksgiving holiday would now count as a day off, but your German unity day would not.");

        //---------------------------------------------------------------------------------------
        // Create New Location - Intro
        //---------------------------------------------------------------------------------------

        seleniumHandler.highlight(LocationListView.CREATE_LOCATION_BUTTON);
        paul.narrate(NORMAL, "Let me show you how to add a new location. Imagine I'm planning to relocate to California in September. Let's create that location record now.");

        //---------------------------------------------------------------------------------------
        // Create New Location - Dialog
        //---------------------------------------------------------------------------------------

        paul.narrateAsync(NORMAL, "Click the create button.");
        seleniumHandler.click(LocationListView.CREATE_LOCATION_BUTTON);

        paul.narrateAsync(NORMAL, "First, we select the start date - this is when the new location becomes effective. Let's set it to September first, twenty twenty-five.");
        final LocalDate californiaStartDate = LocalDate.of(2025, 9, 1);
        seleniumHandler.setDatePickerValue(LocationDialog.LOCATION_START_DATE_FIELD, californiaStartDate);

        paul.narrateAsync(NORMAL, "Next, we choose the country - United States.");
        seleniumHandler.setComboBoxValue(LocationDialog.LOCATION_COUNTRY_FIELD, "United States (US)");

        paul.narrateAsync(NORMAL, "And finally, the state or region - California.");
        seleniumHandler.setComboBoxValue(LocationDialog.LOCATION_STATE_FIELD, "California (ca)");

        paul.narrateAsync(NORMAL, "Now click Save to create the location record.");
        seleniumHandler.click(LocationDialog.CONFIRM_BUTTON);

        //---------------------------------------------------------------------------------------
        // Verify Creation & Explain Impact
        //---------------------------------------------------------------------------------------

        seleniumHandler.wait(1000);
        seleniumHandler.highlight(LocationListView.LOCATION_GRID_START_DATE_PREFIX + "2025-09-01");
        paul.narrate(NORMAL, "Perfect! The new location record is now visible in the grid.");
        paul.narrate(NORMAL, "Starting September first, Kassandra will use California holidays instead of North Rhine-Westphalia holidays when calculating my availability and planning sprints.");
        paul.narrate(NORMAL, "My old location record automatically ends the day before the new one begins, ensuring there are no gaps or overlaps.");

        //---------------------------------------------------------------------------------------
        // Mention Edit/Delete Capabilities
        //---------------------------------------------------------------------------------------

        paul.narrate(NORMAL, "With the little notepad and trashcan icons, on the right side, you can edit or delete any existing location.");

        //---------------------------------------------------------------------------------------
        // Mention Minimum Location Requirement
        //---------------------------------------------------------------------------------------

        seleniumHandler.highlight(LocationListView.LOCATION_GRID);
        paul.narrate(NORMAL, "However, you cannot delete your only location record - Kassandra requires at least one location to calculate holidays properly.");

        //---------------------------------------------------------------------------------------
        // Closing
        //---------------------------------------------------------------------------------------

        paul.narrate(NORMAL, "That's all there is to managing your location in Kassandra. Remember, keeping your location up to date ensures accurate holiday calculations and better project planning. Thanks for watching!");

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

