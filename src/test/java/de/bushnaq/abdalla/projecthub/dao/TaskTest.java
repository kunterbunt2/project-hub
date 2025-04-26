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

package de.bushnaq.abdalla.projecthub.dao;

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
public class TaskTest extends AbstractEntityGenerator {

    @Test
    public void create() throws Exception {
        User user1 = addRandomUser();

        for (int i = 0; i < 1; i++) {
            Product product = addProduct("Product " + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addRandomProject(version);
            Sprint  sprint  = addRandomSprint(project);

            Task task1 = addTask(sprint, null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
            Task task2 = addTask(sprint, task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
            Task task3 = addTask(sprint, task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);
        }

        printTables();
        testAll();
    }


    @Test
    public void update() throws Exception {
        User user1 = addRandomUser();

        for (int i = 0; i < 1; i++) {
            Product product = addProduct("Product " + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addRandomProject(version);
            Sprint  sprint  = addRandomSprint(project);
            Task    task1   = addTask(sprint, null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
            Task    task2   = addTask(sprint, task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
            Task    task3   = addTask(sprint, task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);
        }

        testAll();

        //update
        {
            move(expectedSprints.getFirst(), expectedTasks.get(2), expectedTasks.get(1));
        }

        printTables();
        testAll();

    }

}