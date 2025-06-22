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

import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.util.UserViewTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Integration test for the UserListView UI component.
 * Tests CRUD (Create, Read, Update, Delete) operations for users in the UI.
 * <p>
 * These tests verify that:
 * - Users can be created with appropriate details
 * - Created users appear correctly in the list
 * - Users can be edited and changes are reflected in the UI
 * - Users can be deleted from the system
 * - Cancellation of operations works as expected
 * <p>
 * The tests use {@link UserViewTester} to interact with the UI elements
 * and verify the expected behavior.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserListViewTest extends AbstractUiTestUtil {
    private final String          color              = "#ff0000"; // Red
    private final String          email              = "user.test@example.com";
    private final LocalDate       firstWorkingDay    = LocalDate.of(2025, 1, 15);
    private final LocalDate       lastWorkingDay     = LocalDate.of(2026, 12, 31);
    private final String          name               = "User-Test";
    private final String          newColor           = "#0000ff"; // Blue
    private final String          newEmail           = "newuser.test@example.com";
    private final LocalDate       newFirstWorkingDay = LocalDate.of(2025, 2, 1);
    private final LocalDate       newLastWorkingDay  = LocalDate.of(2027, 6, 30);
    private final String          newName            = "NewUser-Test";
    @Autowired
    private       SeleniumHandler seleniumHandler;
    @Autowired
    private       UserViewTester  userViewTester;

    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        userViewTester.switchToUserListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
//        seleniumHandler.startRecording(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
    }

    /**
     * Tests the behavior when creating a user but canceling the operation.
     * <p>
     * Verifies that when a user clicks the create user button, enters a name, and then
     * cancels the operation, no user is created in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateCancel() throws Exception {
        userViewTester.createUserCancel(name, email, color, firstWorkingDay, lastWorkingDay);
        userViewTester.verifyFormIsReset();
    }

    /**
     * Tests the behavior when successfully creating a user.
     * <p>
     * Verifies that when a user clicks the create user button, enters all user fields,
     * and confirms the creation, the user appears in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateConfirm() throws Exception {
        userViewTester.createUserConfirm(name, email, color, firstWorkingDay, lastWorkingDay);
        userViewTester.verifyUserDialogFields(name, email, color, firstWorkingDay, lastWorkingDay);
    }

    /**
     * Tests the behavior when attempting to delete a user but canceling the operation.
     * <p>
     * Creates a user, then attempts to delete it but cancels the confirmation dialog.
     * Verifies that the user remains in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testDeleteCancel() throws Exception {
        userViewTester.createUserConfirm(name, email, color, firstWorkingDay, lastWorkingDay);
        userViewTester.deleteUserCancel(name);
    }

    /**
     * Tests the behavior when successfully deleting a user.
     * <p>
     * Creates a user, then deletes it by confirming the deletion in the confirmation dialog.
     * Verifies that the user is removed from the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testDeleteConfirm() throws Exception {
        userViewTester.createUserConfirm(name, email, color, firstWorkingDay, lastWorkingDay);
        userViewTester.deleteUserConfirm(name);
    }

    /**
     * Tests the behavior when attempting to edit a user but canceling the operation.
     * <p>
     * Creates a user, attempts to edit all its fields (name, email, color, first working day,
     * and last working day), but cancels the edit dialog.
     * Verifies that the original user details remain unchanged and the new values are not applied.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditCancel() throws Exception {
        userViewTester.createUserConfirm(name, email, color, firstWorkingDay, lastWorkingDay);
        userViewTester.editUserCancel(name, newName, email, newEmail, color, newColor, firstWorkingDay, newFirstWorkingDay, lastWorkingDay, newLastWorkingDay);
        userViewTester.verifyUserDialogFields(name, email, color, firstWorkingDay, lastWorkingDay);
    }

    /**
     * Tests the behavior when successfully editing a user.
     * <p>
     * Creates a user, edits all user fields, and confirms the edit.
     * Verifies that the user with the new name appears in the list and the old name is removed.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditConfirm() throws Exception {
        userViewTester.createUserConfirm(name, email, color, firstWorkingDay, lastWorkingDay);
        userViewTester.editUserConfirm(name, newName, newEmail, newColor, newFirstWorkingDay, newLastWorkingDay);
        userViewTester.verifyUserDialogFields(newName, newEmail, newColor, newFirstWorkingDay, newLastWorkingDay);
    }
}
