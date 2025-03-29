package de.bushnaq.abdalla.projecthub.report.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class User implements Iterable<String> {
    private final String       email;
    private final List<String> loginList = new ArrayList<>();

    public User(String email) {
        this.email = email.toLowerCase();
        loginList.add(this.email);
    }

    @Override
    public Iterator<String> iterator() {
        return loginList.iterator();
    }
}
