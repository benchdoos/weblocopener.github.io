/*
 * Copyright 2018 Eugeny Zrazhevsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.benchdoos.weblocopener.commons.utils.browser;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.github.benchdoos.weblocopener.commons.core.ApplicationConstants;
import com.github.benchdoos.weblocopener.commons.core.Translation;
import com.github.benchdoos.weblocopener.commons.utils.Logging;
import com.github.benchdoos.weblocopener.commons.utils.system.SystemUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Eugene Zrazhevsky on 24.08.2017.
 */
public class BrowserManager {
    private static final Logger log = Logger.getLogger(Logging.getCurrentClassName());

    private static final String FIELD_NAME_LIST = "list";
    private static final String FIELD_NAME_BROWSER = "browser";
    private static final String FIELD_NAME_CALL = "call";
    private static final String FIELD_NAME_PRIVATE_CALL = "private-call";
    private static NSArray plist = new NSArray();
    private static ArrayList<Browser> browserList = new ArrayList<>();

    private static ArrayList<Browser> DEFAULT_BROWSERS_LIST = new ArrayList<>();
    private static String defaultBrowserName = "Default";
    private static Translation translation;


    public static void loadBrowserList() {
        initTranslation();
        loadBrowsersFromDefault(generateDefaultBrowserArrayList());
        /*File file = new File(ApplicationConstants.DEFAULT_LIST_LOCATION);
        if (file.exists()) {
            loadBrowsersFromFile(file);
        } else {
            reloadBrowserList(generateDefaultBrowserArrayList());
            if (file.exists()) {
                loadBrowsersFromFile(file);
            }
        }*/
    }

    private static void loadBrowsersFromDefault(ArrayList<Browser> list) {

        browserList = list;
        browserList.add(0, new Browser(defaultBrowserName, ApplicationConstants.BROWSER_DEFAULT_VALUE));
        log.debug("Browsers count: " + browserList.size() + " " + browserList);

    }

    private static void loadBrowsersFromFile(File file) {
        parsePlist(file);
        browserList = plistToArrayList(plist);
        browserList.add(0, new Browser(defaultBrowserName, ApplicationConstants.BROWSER_DEFAULT_VALUE));
        log.debug("count: " + browserList.size() + " " + browserList);
    }

    public static ArrayList<Browser> getBrowserList() {
        return browserList;
    }

    public static boolean isDefaultBrowser(String call) {
        boolean result = false;
        ArrayList<Browser> defaultBrowserList = generateDefaultBrowserArrayList();
        for (Browser browser : defaultBrowserList) {

            if (browser.getCall() != null) {
                if (browser.getCall().equals(call)) {
                    result = true;
                }
            }
            if (browser.getIncognitoCall() != null) {
                if (browser.getIncognitoCall().equals(call)) {
                    result = true;
                }
            }
        }
        return result;
    }

    private static ArrayList<Browser> plistToArrayList(NSArray plist) {
        ArrayList<Browser> result = new ArrayList<>();
        for (int i = 0; i < plist.count(); i++) {
            try {
                NSDictionary dictionary = (NSDictionary) plist.objectAtIndex(i);
                final String name = dictionary.objectForKey(FIELD_NAME_BROWSER).toString();
                final String call = dictionary.objectForKey(FIELD_NAME_CALL).toString();
                Browser browser = new Browser();
                browser.setName(name);
                browser.setCall(call);

                try {
                    final String incognito = dictionary.objectForKey(FIELD_NAME_PRIVATE_CALL).toString();
                    browser.setIncognitoCall(incognito);
                } catch (NullPointerException e) {/*NOP*/}


                result.add(browser);
            } catch (NullPointerException e) {
                log.warn("Can not read browser, index:" + i);
            }
        }
        return result;
    }

    private static void parsePlist(File file) {
        try {
            plist = (NSArray) PropertyListParser.parse(file);
        } catch (IOException e) {
            log.warn("Can not read file to parse: " + file, e);
        } catch (PropertyListFormatException | ParseException | SAXException | ParserConfigurationException e) {
            log.warn("Can not parse file: " + file, e);
        }
    }

    static void reloadBrowserList(ArrayList<Browser> browserList) {

        NSArray root = new NSArray(browserList.size());
        for (int i = 0; i < browserList.size(); i++) {
            NSDictionary browser = new NSDictionary();
            browser.put(FIELD_NAME_BROWSER, browserList.get(i).getName());
            browser.put(FIELD_NAME_CALL, browserList.get(i).getCall());
            browser.put(FIELD_NAME_PRIVATE_CALL, browserList.get(i).getIncognitoCall());
            root.setValue(i, browser);
        }

        try {
            log.debug("Browser list location: " + ApplicationConstants.DEFAULT_LIST_LOCATION);
            File file = new File(ApplicationConstants.DEFAULT_LIST_LOCATION);
            PropertyListParser.saveAsXML(root, file);
        } catch (IOException e) {
            log.warn("Can not create .webloc file", e);
        }
    }

    static ArrayList<Browser> generateDefaultBrowserArrayList() {
        ArrayList<Browser> result = new ArrayList<>();

        if (SystemUtils.isWindows()) {
            result = createBrowserListForWindows(result);
        } else if (SystemUtils.isUnix()) {
            result = createBrowserListForUnix(result);
        }


        return result;
    }

    private static ArrayList<Browser> createBrowserListForUnix(ArrayList<Browser> result) {

        if (commandExists("google-chrome")) {
            final String chromeCall = "google-chrome " + "%site";
            Browser chrome = new Browser("Google Chrome", chromeCall, chromeCall + " --incognito");
            result.add(chrome);
        }

        if (commandExists("chromium-browser")) {
            final String chromiumCall = "chromium-browser " + "%site";
            Browser chromium = new Browser("Chromium", chromiumCall, chromiumCall + " --incognito");
            result.add(chromium);
        }

        if (commandExists("firefox")) {
            Browser firefox = new Browser("Firefox", "firefox " + "%site", "firefox -private-window " + "%site");
            result.add(firefox);
        }

        if (commandExists("opera")) {
            Browser opera = new Browser("Opera", "opera " + "%site", "opera --private " + "%site");
            result.add(opera);
        }

        if (commandExists("yandex-browser")) {
            Browser yandex = new Browser("Yandex Browser", "yandex-browser " + "%site",
                    "yandex-browser -incognito " + "%site");
            result.add(yandex);
        }

        if (commandExists("vivaldi")) {
            Browser vivaldi = new Browser("Vivaldi", "vivaldi " + "%site", "vivaldi -incognito " + "%site");
            result.add(vivaldi);
        }
        return result;
    }

    private static boolean commandExists(String command) { //TODO Make for Windows when ready
        return new File("/usr/bin/" + command).exists();
    }

    private static ArrayList<Browser> createBrowserListForWindows(ArrayList<Browser> result) {
        final String chromeCall = "start chrome " + "\"" + "%site" + "\"";
        Browser chrome = new Browser("Google Chrome", chromeCall, chromeCall + " --incognito");
        result.add(chrome);

        Browser firefox = new Browser("Firefox", "start firefox " + "\"" + "%site" + "\"",
                "start firefox -private-window " + "\"" + "%site" + "\"");
        result.add(firefox);

        Browser edge = new Browser("Microsoft Edge", "start microsoft-edge:" + "\"" + "%site" + "\"");
        result.add(edge);

        Browser iExplorer = new Browser("Internet Explorer", "start iexplore " + "\"" + "%site" + "\"",
                "start iexplore " + "\"" + "%site" + "\"" + " -private");
        result.add(iExplorer);

        Browser opera = new Browser("Opera", "start opera " + "\"" + "%site" + "\"",
                "start opera --private " + "\"" + "%site" + "\"");
        result.add(opera);

        Browser yandex = new Browser("Yandex Browser", "start browser " + "\"" + "%site" + "\"",
                "start browser -incognito " + "\"" + "%site" + "\"");
        result.add(yandex);

        Browser vivaldi = new Browser("Vivaldi", "start vivaldi " + "\"" + "%site" + "\"",
                "start vivaldi -incognito " + "\"" + "%site" + "\"");
        result.add(vivaldi);
        return result;
    }


    private static void initTranslation() {
        translation = new Translation("translations/CommonsBundle") {
            @Override
            public void initTranslations() {
                defaultBrowserName = messages.getString("defaultBrowserName");
            }
        };
        translation.initTranslations();
    }
}
