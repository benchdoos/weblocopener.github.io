/*
 * (C) Copyright 2019.  Eugene Zrazhevsky and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Contributors:
 * Eugene Zrazhevsky <eugene.zrazhevsky@gmail.com>
 */

package com.github.benchdoos.weblocopener.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class InternetShortcut {
    /**
     * Create an Internet shortcut
     *
     * @param file location of the shortcut
     * @param url  URL
     * @param icon URL (ex. http://www.server.com/favicon.ico)
     * @throws IOException if can not write a file
     */
    public static void createInternetShortcut(File file, String url, String icon) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("[InternetShortcut]\n");
        fileWriter.write("URL=" + url + "\n");
        if (icon != null && !icon.equals("")) {
            fileWriter.write("IconFile=" + icon + "\n");
        }
        fileWriter.flush();
        fileWriter.close();
    }

    public static URL getInternetShortcut(File file) throws IOException {
        try (FileReader fileReader = new FileReader(file)) {
            Scanner scan = new Scanner(fileReader);

            while (scan.hasNext()) {
                final String next = scan.next();
                if (next.startsWith("URL=")) {
                    char[] buffer = new char[next.length()];
                    next.getChars(4, next.length(), buffer, 0);
                    final String url = new String(buffer);
                    return new URL(url);
                }
            }
        }
        return null;
    }
}
