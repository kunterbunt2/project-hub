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

package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.UserDAO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends ListCrudRepository<UserDAO, Long> {

    Optional<UserDAO> findByName(String name);

    @Query("SELECT DISTINCT u FROM UserDAO u WHERE u.id IN " +
            "(SELECT t.resourceId FROM TaskDAO t WHERE t.sprintId = :sprintId AND t.resourceId IS NOT NULL)")
    List<UserDAO> findBySprintId(@Param("sprintId") Long sprintId);
}