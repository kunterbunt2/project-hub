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

package de.bushnaq.abdalla.projecthub.report.dao;

import de.bushnaq.abdalla.projecthub.dto.User;

import java.util.*;

public class Authors1 {
    private final List<Author1>        list = new ArrayList<>();
    private final Map<String, Author1> map  = new HashMap<>();//map author name to author
    public static Users                team = new Users();

    public Author1 add(String name) {
        Author1 author = get(name);

        if (author == null) {
            author      = new Author1();
            author.name = name;
            list.add(author);
            map.put(name.toLowerCase(), author);
            return author;

        }
        return author;
    }

    public void calculateColors(BurnDownGraphicsTheme graphicsTheme, boolean authorsArePeople) {
        //distribute fixed resource colors
        int i = 0;
        if (authorsArePeople) {
            for (User user : team.getList()) {
//                for (String login : user)
                {
                    Author1 author = get(user.getEmail());
                    if (author != null && author.color == null) {
                        author.color = graphicsTheme.getAuthorColor(i);
                        break;
                    }
                }
                i++;
            }
        }

        List<Author1> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList, Author1.comparator);
        int start = team.getList().size();
        if (authorsArePeople) {
            start = 0;
        }
        int index = 0;
        for (Author1 author : sortedList) {
            if (author.color == null) {
                author.color = graphicsTheme.getAuthorColor(start + index);
                index++;
            }
        }
    }

    public Author1 get(String resourceName) {
        return map.get(resourceName.toLowerCase());
    }

    public List<Author1> getList() {
        return list;
    }

    public void remove(Author1 author) {
        list.remove(author);
        map.remove(author.name.toLowerCase());
    }

    public void sort() {
        Collections.sort(list, Author1.comparator);
    }

}
