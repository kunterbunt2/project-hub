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

import java.io.PrintStream;
import java.util.*;

public class AuthorsContribution extends TreeMap<String, AuthorContribution> {

    MapValueCopmparator mapValueCopmparator = new MapValueCopmparator(this);

    public AuthorsContribution() {
        super();

    }

    public List<String> getSortedKeyList() {
        List<String> authors = Arrays.asList(this.keySet().toArray(new String[0]));
        Collections.sort(authors, mapValueCopmparator);
        return authors;
    }

    public void print(PrintStream out) {
        for (String author : this.getSortedKeyList()) {
            AuthorContribution authorContribution = this.get(author);
            out.printf("%10s ", author);
            authorContribution.print(out);
        }
    }

}

class MapValueCopmparator implements Comparator<String> {
    private static final String NON_CHARGEABLE = "1-Non-chargeable";
    Map<String, AuthorContribution> referenceMap;

    public MapValueCopmparator(Map<String, AuthorContribution> referenceMap) {
        this.referenceMap = referenceMap;
    }

    @Override
    public int compare(String o1, String o2) {
        if (o1.equals(NON_CHARGEABLE)) {
            return 1;
        }
        if (o2.equals(NON_CHARGEABLE)) {
            return -1;
        }
        AuthorContribution ac1 = referenceMap.get(o1);
        AuthorContribution ac2 = referenceMap.get(o2);
        return ac2.worked.plus(ac2.remaining).compareTo(ac1.worked.plus(ac1.remaining));
    }
}
