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

package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class SprintTest extends AbstractEntityGenerator {

    @Test
    public void case01() throws Exception {
        User user1 = addRandomUser();
        User user2 = addRandomUser();

        for (int i = 0; i < 1; i++) {
            Product product = addProduct("Product " + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addRandomProject(version);
            Sprint  sprint  = addRandomSprint(project);

            LocalDateTime start = LocalDateTime.now();
            Task          task1 = addTask(sprint, null, "[1] Parent Task", start, Duration.ofDays(0), null, null);
            Task          task2 = addTask(sprint, task1, "[2] Child Task", start, Duration.ofDays(1), user1, null);
            Task          task3 = addTask(sprint, task1, "[3] Child Task", start, Duration.ofDays(1), user2, task2);
        }
        printTables();
        testAll();
    }

//    @Test
//    public void create() throws Exception {
//        Project project = createProject();
//
//        // Create sprint
//        {
//            Sprint sprint = new Sprint();
//            sprint.setName("Sprint 1");
//            sprint.setStart(OffsetDateTime.now());
//            sprint.setEnd(OffsetDateTime.now().plusWeeks(2));
//            sprint.setStatus(Status.OPEN);
//            project.getVersions().getFirst().setSprints(List.of(sprint));
//        }
//
//
//        Project createdProject = productApi.persist(project);
//
//        Project retrievedProject = productApi.getProject(createdProject.getId());
//
//        asserEqual(createdProject, retrievedProject);
//        List<Sprint> sprints = retrievedProject.getVersions().get(0).getSprints();
//        assertFalse(retrievedProject.getVersions().get(0).getSprints().isEmpty());
//        Sprint savedSprint = sprints.get(0);
//        assertEquals("Sprint 1", savedSprint.getName());
//        assertEquals(Status.OPEN, savedSprint.getStatus());
//        assertNotNull(savedSprint.getStart());
//        assertNotNull(savedSprint.getEnd());
//
//        printTables();
//    }


}