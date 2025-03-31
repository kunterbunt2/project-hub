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
