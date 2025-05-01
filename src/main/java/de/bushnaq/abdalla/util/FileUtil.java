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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * changes extension to new extension example: x = changeExtension("data.txt",
     * ".java") will assign "data.java" to x.
     *
     * @param originalName
     * @param newExtension
     * @return
     */
    public static String changeExtension(String originalName, String newExtension) {
        int lastDot = originalName.lastIndexOf(".");
        if (lastDot != -1) {
            return originalName.substring(0, lastDot) + newExtension;
        } else {
            return originalName + newExtension;
        }
    }

    public static String concatPath(String p1, String p2) {
        if (p1.endsWith("/")) {
            return p1 + p2;
        } else {
            return p1 + "/" + p2;
        }
    }

    /**
     * Copy a file from source to destination.
     *
     * @param source      the source
     * @param destination the destination
     * @return True if succeeded , False if not
     * @throws IOException
     */
    public static void copyFile(InputStream source, String destination) throws IOException {
        Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void copyFilesFromExploded(Class<?> clazz, String sourceDir, String destinationDir) throws IOException {
        logger.info(String.format("Copying files from clazz=%s sourceFolder=%s destinationFolder=%s", clazz.getCanonicalName(), sourceDir, destinationDir));
        for (File f : FileUtil.getResourceFolderFiles(clazz, sourceDir)) {
            if (f.isFile()) {
                InputStream stream = clazz.getResourceAsStream(sourceDir + "/" + f.getName());
                if (stream != null) {
                    try {
                        logger.info("Copying ->" + sourceDir + "/" + f.getName() + " to ->" + destinationDir);
                        FileUtil.copyFile(stream, destinationDir + "/" + f.getName());
                    } finally {
                        stream.close();
                    }
                }
            }
        }
    }

    public static void copyFilesFromJar(Class<?> callingClass, String jarName, String sourceDir, String destinationDir) throws IOException {
        logger.info(String.format("Copying files from warFile=%s clazz=%s sourceFolder=%s destinationFolder=%s", jarName, callingClass.getCanonicalName(),
                sourceDir, destinationDir));
        JarFile jarfile = new JarFile(new File(jarName));
        try {
            java.util.Enumeration<JarEntry> enu = jarfile.entries();
            while (enu.hasMoreElements()) {
                //            String destdir = reportFolder + "/report";     //abc is my destination directory
                JarEntry je = enu.nextElement();
                if (je.getName().startsWith(sourceDir)) {
                    //                System.out.println(je.getName());
                    String destinationFileName = je.getName().substring(sourceDir.length());
                    File   fl                  = new File(destinationDir, destinationFileName);

                    if (!fl.exists()) {
                        fl.getParentFile().mkdirs();
                        fl = new File(destinationDir, destinationFileName);
                    }
                    if (je.isDirectory()) {
                        continue;
                    }
                    InputStream is = jarfile.getInputStream(je);
                    try {
                        java.io.FileOutputStream fo = new java.io.FileOutputStream(fl);
                        try {
                            while (is.available() > 0) {
                                fo.write(is.read());
                            }
                        } finally {
                            fo.close();
                        }
                    } finally {
                        is.close();
                    }
                }
            }
        } finally {
            jarfile.close();
        }
    }

    public static File[] getResourceFolderFiles(Class<?> clazz, String location) {
        URL    url  = clazz.getResource(location);
        String path = url.getPath();
        return new File(path).listFiles();
    }

    public static String loadFile(final Object aCallingObject, final String fileName) throws IOException {
        if (aCallingObject == null) {
            StringBuilder     _command     = new StringBuilder();
            final InputStream _inputStream = new FileInputStream(new File(fileName));
            //                if (_inputStream == null) {
            //                    throw new IOException(String.format("File '%s' not found", fileName));
            //                }
            final BufferedInputStream _bufferedInputStream = new BufferedInputStream(_inputStream);
            final byte[]              _array               = new byte[1000 * 10];
            int                       _number              = 0;
            do {
                _number = _bufferedInputStream.read(_array, 0, 10000);
                if (_number > 0) {
                    _command.append(new String(_array, 0, _number));
                }
            } while (_number != -1);
            _bufferedInputStream.close();
            return _command.toString();
        } else {
            try {
                String            _command     = "";
                final InputStream _inputStream = aCallingObject.getClass().getResourceAsStream(fileName);
                if (_inputStream == null) {
                    throw new IOException(String.format("File '%s' not found", fileName));
                }
                final BufferedInputStream _bufferedInputStream = new BufferedInputStream(_inputStream);
                final byte[]              _array               = new byte[1000 * 10];
                int                       _number              = 0;
                do {
                    _number = _bufferedInputStream.read(_array, 0, 10000);
                    if (_number > 0) {
                        _command += new String(_array, 0, _number);
                    }
                } while (_number != -1);
                _bufferedInputStream.close();
                return _command;
            } catch (IOException e) {
                logger.error("load " + aCallingObject.getClass().getCanonicalName() + ", " + fileName);
                throw e;
            }
        }
    }

    public static String removeExtension(String originalName) {
        if (originalName == null) {
            return originalName;
        }
        int lastDot = originalName.lastIndexOf(".");
        if (lastDot != -1) {
            return originalName.substring(0, lastDot);
        } else {
            return originalName;
        }
    }

}
