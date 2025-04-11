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

package de.bushnaq.abdalla.projecthub.gantt;

import de.bushnaq.abdalla.projecthub.dao.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractLegacyGanttTestUtil;
import de.bushnaq.abdalla.util.MpxjUtil;
import de.bushnaq.abdalla.util.date.DateUtil;
import net.sf.mpxj.*;
import net.sf.mpxj.reader.UniversalProjectReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LegacyGanttTest extends AbstractLegacyGanttTestUtil {

    //    @DisplayName("critical path test against actual ms project files")
    @MethodSource("listFilesByExtension")
    @ParameterizedTest
    public void legacyTest(Path mppFileName, TestInfo testInfo) throws Exception {
        File file = new File(String.valueOf(mppFileName.toAbsolutePath()));
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            UniversalProjectReader reader      = new UniversalProjectReader();
            ProjectFile            projectFile = reader.read(inputStream);
            LocalDateTime          date        = projectFile.getProjectProperties().getStartDate();//ensure start date matches
            ParameterOptions.now = DateUtil.localDateTimeToOffsetDateTime(date);
            //populate mpxjTaskMap and resourceMap
            for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
                if (isValidTask(mpxjTask)) {
                    System.out.printf("Task ID: %s, Task Name: %s%n", mpxjTask.getID(), mpxjTask.getName());
                    mpxjTaskMap.put(mpxjTask.getName(), mpxjTask);//store tasks
                    if (!mpxjTask.getResourceAssignments().isEmpty()) {
                        ResourceAssignment resourceAssignment = mpxjTask.getResourceAssignments().get(0);
                        Resource           resource           = resourceAssignment.getResource();
                        String             resourceName       = resource.getName();
                        if (resourceMap.get(resourceName) != null) {
                            resourceMap.put(resourceName, resource);//store resources
                        }
                        if (userMap.get(resourceName) == null) {
                            Number   units        = resourceAssignment.getUnits();
                            Duration work         = resourceAssignment.getWork();
                            double   availability = units.doubleValue() / 100;
                            String   emailAddress = resource.getEmailAddress();
                            if (emailAddress == null) {
                                emailAddress = resourceName.replaceAll(" ", "_") + "@example.com";
                            }
                            User user = addUser(resourceName, emailAddress, "de", "nw", resource.getCreationDate().toLocalDate(), (float) availability);
                            userMap.put(resourceName, user);//store users
                        }
                    }
                }
            }

            //populate taskMap
            for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
                if (isValidTask(mpxjTask)) {
                    String name = mpxjTask.getName();
                    if (!mpxjTask.getResourceAssignments().isEmpty()) {
                        ResourceAssignment resourceAssignment = mpxjTask.getResourceAssignments().get(0);
                        Resource           resource           = resourceAssignment.getResource();
                        String             resourceName       = resource.getName();
                        Duration           work               = resourceAssignment.getWork();
                        net.sf.mpxj.Task   parent             = mpxjTaskMap.get(mpxjTask.getParentTask().getName());
                        User               user               = userMap.get(resourceName);
                        Task               task               = addTask(sprint, null, mpxjTask.getName(), null, MpxjUtil.toJavaDuration(work), user, null);
                        taskMap.put(task.getName(), task);
                    } else {
                        Task task = addTask(sprint, null, mpxjTask.getName(), null, null, null, null);//parent task
                        taskMap.put(task.getName(), task);
                    }
                }
            }
            //add parents and relations
            for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
                if (isValidTask(mpxjTask)) {
                    String name = mpxjTask.getName();
                    Task   task = taskMap.get(name);
                    //set parent
                    if (mpxjTask.getParentTask() != null) {
                        net.sf.mpxj.Task mpxjParent = mpxjTaskMap.get(mpxjTask.getParentTask().getName());
                        if (mpxjParent != null) {
                            //probably not valid task
                            Task parent = taskMap.get(mpxjParent.getName());
                            task.setParentTaskId(parent.getId());
                        }
                    }
                    //set relations
                    for (Relation r : mpxjTask.getPredecessors()) {
                        net.sf.mpxj.Task mpxjPredecessor = r.getPredecessorTask();
                        Task             predecessor     = taskMap.get(mpxjPredecessor.getName());
                        task.addPredecessor(predecessor, true);
                    }
                }
            }
            for (Task value : taskMap.values()) {
                taskApi.persist(value);
            }

            initialize();
            generateGanttChart(testInfo, projectFile);
        }
    }

    private static List<Path> listFilesByExtension() throws IOException {
        return listFilesByExtension("references", ".mpp");
    }

    /**
     * Lists files in the specified directory with an extension filter
     *
     * @param directoryPath The path to the directory
     * @param extension     The file extension to filter by (e.g., ".java")
     * @return List of matching file paths
     * @throws IOException If an I/O error occurs
     */
    private static List<Path> listFilesByExtension(String directoryPath, String extension) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(extension))
                    .collect(Collectors.toList());
        }
    }
}
