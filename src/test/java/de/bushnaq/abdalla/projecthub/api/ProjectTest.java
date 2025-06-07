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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ProjectTest extends AbstractEntityGenerator {
    private static final long   FAKE_ID     = 999999L;
    private static final String SECOND_NAME = "SECOND_NAME";

    @Test
    public void anonymousSecurity() {
        {
            setUser("admin-user", "ROLE_ADMIN");
            addRandomProducts(1);
            SecurityContextHolder.clearContext();
        }

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            addRandomProject(expectedVersions.getFirst());
        });

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            List<Project> allProjects = projectApi.getAll();
        });

        {
            Project project = expectedProjects.getFirst();
            Long    id      = project.getId();
            String  name    = project.getName();
            project.setName(SECOND_NAME);
            try {
                updateProject(project);
                fail("should not be able to update");
            } catch (AuthenticationCredentialsNotFoundException e) {
                //restore fields to match db for later tests in @AfterEach
                project.setName(name);
            }
        }

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            removeProject(expectedProjects.get(0).getId());
        });

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            Project project = projectApi.getById(expectedProjects.getFirst().getId());
        });
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void create() throws Exception {
        addRandomProducts(1);
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void delete() throws Exception {
        //create the users
        addRandomProducts(2);
        removeProject(expectedProjects.getFirst().getId());
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
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
        {
            setUser("admin-user", "ROLE_ADMIN");
            addRandomProducts(3);
            setUser("user", "ROLE_USER");
        }
        List<Project> allProjects = projectApi.getAll();
        assertEquals(3, allProjects.size());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllEmpty() throws Exception {
        List<Project> allProjects = projectApi.getAll();
        assertEquals(0, allProjects.size());
    }

    @Test
    public void getByFakeId() throws Exception {
        {
            setUser("admin-user", "ROLE_ADMIN");
            addRandomProducts(1);
            setUser("user", "ROLE_USER");
        }
        try {
            projectApi.getById(FAKE_ID);
            fail("Project should not exist");
        } catch (ServerErrorException e) {
            //expected
        }
    }

    @Test
    public void getById() throws Exception {
        {
            setUser("admin-user", "ROLE_ADMIN");
            addRandomProducts(1);
            setUser("user", "ROLE_USER");
        }
        Project project = projectApi.getById(expectedProjects.getFirst().getId());
        assertProjectEquals(expectedProjects.getFirst(), project, true); // shallow test
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void update() throws Exception {
        addRandomProducts(2);
        Project project = expectedProjects.getFirst();
        project.setName(SECOND_NAME);
        updateProject(project);
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
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

    @Test
    public void userSecurity() {
        {
            setUser("admin-user", "ROLE_ADMIN");
            addRandomProducts(1);
            setUser("user", "ROLE_USER");
        }

        assertThrows(AccessDeniedException.class, () -> {
            addRandomProject(expectedVersions.getFirst());
        });

        {
            Project project = expectedProjects.getFirst();
            String  name    = project.getName();
            project.setName(SECOND_NAME);
            try {
                updateProject(project);
                fail("should not be able to update");
            } catch (AccessDeniedException e) {
                //restore fields to match db for later tests in @AfterEach
                project.setName(name);
            }
        }

        assertThrows(AccessDeniedException.class, () -> {
            removeProject(expectedProjects.get(0).getId());
        });
    }
}
