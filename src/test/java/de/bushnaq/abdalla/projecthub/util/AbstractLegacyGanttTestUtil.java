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

package de.bushnaq.abdalla.projecthub.util;

import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.report.renderer.gantt.GanttContext;
import de.bushnaq.abdalla.projecthub.report.renderer.gantt.GanttUtil;
import de.bushnaq.abdalla.util.MpxjUtil;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
public class AbstractLegacyGanttTestUtil extends AbstractGanttTestUtil {
    protected Map<String, net.sf.mpxj.Task>     mpxjTaskMap = new HashMap<>();
    protected Map<String, net.sf.mpxj.Resource> resourceMap = new HashMap<>();
    protected Map<String, Task>                 taskMap     = new HashMap<>();
    protected Map<String, User>                 userMap     = new HashMap<>();

    @Override
    protected void compareResults(ProjectFile projectFile) throws IOException {
        GanttContext gc              = new GanttContext();
        Sprint       referenceSprint = new Sprint();//fake sprint
        referenceSprint.setId(1L);//fake sprint id
        gc.allSprints.add(referenceSprint);
        long taskIdIndex = 1;//fake task id
        for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
            if (isValidTask(mpxjTask)) {
                Task referenceTask = new Task();//fake task
                referenceTask.setId(taskIdIndex++);
                referenceTask.setName(mpxjTask.getName());
                referenceTask.setStart(mpxjTask.getStart());
                referenceTask.setFinish(mpxjTask.getFinish());
                referenceTask.setDuration(MpxjUtil.toJavaDuration(mpxjTask.getDuration()));
                referenceTask.setCritical(mpxjTask.getCritical());
                referenceTask.setSprintId(gc.allSprints.getFirst().getId());
                gc.allTasks.add(referenceTask);
            }
        }
        gc.initialize();

//        Sprint sprint = expectedSprints.getFirst();
        logTasks(sprint, gc.allSprints.getFirst());
        for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
            if (isValidTask(mpxjTask)) {
                Task            task     = taskMap.get(mpxjTask.getName());
                ProjectCalendar calendar = task.getEffectiveCalendar();
                assertTrue(GanttUtil.equals(calendar, task.getStart(), mpxjTask.getStart()), String.format("unexpected task: %s start", task.getName()));
                assertTrue(GanttUtil.equals(calendar, task.getFinish(), mpxjTask.getFinish()), String.format("unexpected task: %s finish", task.getName()));
                assertEquals(MpxjUtil.toJavaDuration(mpxjTask.getDuration()), task.getDuration(), String.format("unexpected task: %s duration", task.getName()));
                boolean c = mpxjTask.getCritical();
                if (mpxjTask.getChildTasks().isEmpty()) {
                    //TODO fix discrepancy between mpxj and java
                    assertEquals(mpxjTask.getCritical(), task.isCritical(), String.format("unexpected task: %s critical", task.getName()));
                }
            }
        }
    }

    @Override
    protected void initialize() throws Exception {
        GanttContext gc = new GanttContext();
        gc.allUsers    = userApi.getAllUsers();
        gc.allProducts = productApi.getAll();
        gc.allVersions = versionApi.getAll();
        gc.allProjects = projectApi.getAll();
        gc.allSprints  = sprintApi.getAll();
        gc.allTasks    = taskApi.getAll();
        gc.initialize();

        taskMap.clear();
        for (Task allTask : gc.allTasks) {
            taskMap.put(allTask.getName(), allTask);
        }
        userMap.clear();
        for (User allUser : gc.allUsers) {
            userMap.put(allUser.getName(), allUser);
        }
        sprint = gc.allSprints.getFirst();
    }

}
