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

import de.bushnaq.abdalla.projecthub.dto.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sprints")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
//@JsonIdentityInfo(
//        scope = SprintDAO.class,
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
public class SprintDAO extends AbstractTimeAwareDAO {
    @Column(name = "end_date", nullable = true)  // renamed from 'end' as it is reserved in H2 databases
    private OffsetDateTime end;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;
    //    @ManyToOne(fetch = FetchType.LAZY)
//    @ToString.Exclude//help intellij debugger not to go into a loop
//    @JsonBackReference(value = "project-sprint")

    @Column(nullable = false)
    private Long projectId;

    @Column(name = "start_date", nullable = true)  // renamed from 'start'
    private OffsetDateTime start;

    @Column(nullable = false)
    private Status status;

//    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "sprint_id", referencedColumnName = "id")
//    @JsonManagedReference(value = "sprint-task")
//    private List<TaskDAO> tasks = new ArrayList<>();
}
