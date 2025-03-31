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

import java.util.*;

public class Authors {
    private final List<Author>        list = new ArrayList<>();
    private final Map<String, Author> map  = new HashMap<>();//map author name to author
    public static Users               team = new Users();

    public Author add(String name) {
        Author author = get(name);

        if (author == null) {
            author      = new Author();
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
            for (User user : team) {
                for (String login : user) {
                    Author author = get(login);
                    if (author != null && author.color == null) {
                        author.color = graphicsTheme.getAuthorColor(i);
                        break;
                    }
                }
                i++;
            }
        }

        List<Author> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList, Author.comparator);
        int start = team.size();
        if (authorsArePeople) {
            start = 0;
        }
        int index = 0;
        for (Author author : sortedList) {
            if (author.color == null) {
                author.color = graphicsTheme.getAuthorColor(start + index);
                index++;
            }
        }
    }

    public Author get(String resourceName) {
        return map.get(resourceName.toLowerCase());
    }

    public List<Author> getList() {
        return list;
    }

    public void remove(Author author) {
        list.remove(author);
        map.remove(author.name.toLowerCase());
    }

    public void sort() {
        Collections.sort(list, Author.comparator);
    }

}
