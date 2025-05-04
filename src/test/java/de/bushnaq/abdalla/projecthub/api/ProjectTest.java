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

package de.bushnaq.abdalla.projecthub.api;

import de.bushnaq.abdalla.projecthub.dto.Project;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ProjectTest extends AbstractEntityGenerator {
    private static final long   FAKE_ID     = 999999L;
    private static final String SECOND_NAME = "SECOND_NAME";
    Logger logger = LoggerFactory.getLogger(ProjectTest.class);

    @Test
    public void create() throws Exception {
        addRandomProducts(1);
    }

    @Test
    public void delete() throws Exception {
        //create the users
        addRandomProducts(2);
        removeProject(expectedProjects.getFirst().getId());
    }

    @Test
    public void deleteUsingFakeId() throws Exception {
        addRandomProducts(2);
        try {
            removeProject(FAKE_ID);
        } catch (ServerErrorException e) {
            //expected
        }
    }

    @Test
    public void getAll() throws Exception {
        addRandomProducts(3);
        List<Project> allProjects = projectApi.getAll();
        assertEquals(3, allProjects.size());
    }

    @Test
    public void getAllEmpty() throws Exception {
        List<Project> allProjects = projectApi.getAll();

    }

    @Test
    public void getByFakeId() throws Exception {
        addRandomProducts(1);
        try {
            projectApi.getById(FAKE_ID);
            fail("Project should not exist");
        } catch (ServerErrorException e) {
            //expected
        }
    }

    @Test
    public void getById() throws Exception {
        addRandomProducts(1);
        Project project = projectApi.getById(expectedProjects.getFirst().getId());
        assertProjectEquals(expectedProjects.getFirst(), project, true); //shallow test
    }

    @Test
    public void update() throws Exception {
        addRandomProducts(2);
        Project project = expectedProjects.getFirst();
        project.setName(SECOND_NAME);
        updateProject(project);
    }

    @Test
    public void updateUsingFakeId() throws Exception {
        addRandomProducts(2);
        Project project = expectedProjects.getFirst();
        Long    id      = project.getId();
        String  name    = project.getName();
        project.setId(FAKE_ID);
        project.setName(SECOND_NAME);
        try {
            updateProject(project);
            fail("should not be able to update");
        } catch (ServerErrorException e) {
            //expected
            project.setId(id);
            project.setName(name);
        }
    }
}
