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

import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ProjectTest extends AbstractTestUtil {
    Logger logger = LoggerFactory.getLogger(ProjectTest.class);

//    @Test
//    public void create() throws Exception {
//
//        for (int i = 0; i < 1; i++) {
//            Project project        = createProject();
//            Project createdProject = productApi.persist(project);
//            Project retrievedProject = productApi.getProject(createdProject.getId());
//            asserEqual(createdProject, retrievedProject);
//        }
//        printTables();
//    }
//
//
//    @Test
//    public void getAll() throws Exception {
//        List<Project> allProjects = productApi.getAllProjects();
//        printTables();
//    }
//
//    @Test
//    public void getById() throws Exception {
//        Project project        = createProject();
//        Project createdProject = productApi.persist(project);
//
//        Project retrievedProject = productApi.getProject(createdProject.getId());
//        asserEqual(createdProject, retrievedProject);
//        printTables();
//    }

}