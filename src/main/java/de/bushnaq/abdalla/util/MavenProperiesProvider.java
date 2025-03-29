package de.bushnaq.abdalla.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class MavenProperiesProvider {

    @SuppressWarnings("unused")
    private static final MavenProperiesProvider INSTANCE = new MavenProperiesProvider();
    static               ResourceBundle         rb;

    private MavenProperiesProvider() {
    }

    public static String getProperty(Class<?> clazz, String name) {
        ResourceBundle bundle = ResourceBundle.getBundle("maven", Locale.getDefault(), clazz.getClassLoader());
        return bundle.getString(name);

    }
}
