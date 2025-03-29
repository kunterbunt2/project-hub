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
