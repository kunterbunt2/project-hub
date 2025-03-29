package de.bushnaq.abdalla.projecthub.report.dao;

import java.util.*;

public class Users implements Iterable<User> {
    List<User>        list = new ArrayList<>();
    Map<String, User> map  = new HashMap<>();

    public void add(User user) {
        list.add(user);
        for (String login : user) {
            map.put(login, user);
        }
    }

    @Override
    public Iterator<User> iterator() {
        return list.iterator();
    }

    public int size() {
        return list.size();
    }
}
