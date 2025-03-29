package de.bushnaq.abdalla.projecthub.report.dao;

import lombok.Getter;

import java.awt.*;
import java.util.Comparator;

import static java.util.Comparator.comparing;

@Getter
public class Author {
    public        Color              color      = null;
    public static Comparator<Author> comparator = comparing(Author::getName).reversed();
    public        String             name       = null;

}
