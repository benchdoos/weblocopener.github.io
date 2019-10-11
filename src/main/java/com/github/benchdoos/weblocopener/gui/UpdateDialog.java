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

package com.github.benchdoos.weblocopener.gui;

import com.github.benchdoos.jcolorful.core.JColorful;
import com.github.benchdoos.weblocopener.core.Translation;
import com.github.benchdoos.weblocopener.core.constants.ApplicationConstants;
import com.github.benchdoos.weblocopener.core.constants.PathConstants;
import com.github.benchdoos.weblocopener.core.constants.StringConstants;
import com.github.benchdoos.weblocopener.update.Updater;
import com.github.benchdoos.weblocopener.update.UpdaterManager;
import com.github.benchdoos.weblocopener.utils.CoreUtils;
import com.github.benchdoos.weblocopener.utils.FrameUtils;
import com.github.benchdoos.weblocopener.utils.Internal;
import com.github.benchdoos.weblocopener.utils.notification.NotificationManager;
import com.github.benchdoos.weblocopener.utils.version.ApplicationVersion;
import com.github.benchdoos.weblocopener.utils.version.Beta;
import com.github.benchdoos.weblocopenercore.preferences.PreferencesManager;
import com.github.benchdoos.weblocopenercore.service.UrlsProceed;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.validator.routines.UrlValidator;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.github.benchdoos.weblocopener.utils.system.SystemUtils.IS_WINDOWS_XP;

@SuppressWarnings({"ALL", "ResultOfMethodCallIgnored"})
@Log4j2
public class UpdateDialog extends JFrame implements Translatable {
    private static volatile UpdateDialog instance = null;
    private JProgressBar progressBar;
    private JButton buttonOK;
    private JButton buttonCancel;
    private Timer messageTimer;
    private Updater updater = null;
    private ApplicationVersion serverApplicationVersion;
    private JPanel contentPane;
    private JLabel currentVersionLabel;
    private JLabel availableVersionLabel;
    private JLabel newVersionSizeLabel;
    private JLabel unitLabel;
    private JLabel currentVersionStringLabel;
    private JLabel availableVersionStringLabel;
    private JButton updateInfoButton;
    private Thread updateThread;
    private JButton manualDownloadButton;
    private JLabel serverBetaLabel;
    private JLabel currentBetaLabel;

    private UpdateDialog() {
        iniGui();
        loadProperties();

    }

    public static UpdateDialog getInstance() {
        UpdateDialog localInstance = instance;
        if (localInstance == null) {
            synchronized (UpdateDialog.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new UpdateDialog();
                }
            }
        }
        return localInstance;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 10, 10, 10), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setActionCommand(ResourceBundle.getBundle("translations/UpdateDialogBundle_en_EN").getString("buttonOk"));
        buttonOK.setEnabled(false);
        Font buttonOKFont = this.$$$getFont$$$(null, Font.BOLD, -1, buttonOK.getFont());
        if (buttonOKFont != null) buttonOK.setFont(buttonOKFont);
        this.$$$loadButtonText$$$(buttonOK, ResourceBundle.getBundle("translations/UpdateDialogBundle").getString("buttonOk"));
        panel2.add(buttonOK, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setActionCommand(ResourceBundle.getBundle("translations/UpdateDialogBundle_en_EN").getString("buttonCancel"));
        this.$$$loadButtonText$$$(buttonCancel, ResourceBundle.getBundle("translations/UpdateDialogBundle").getString("buttonCancel"));
        panel2.add(buttonCancel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        manualDownloadButton = new JButton();
        manualDownloadButton.setIcon(new ImageIcon(getClass().getResource("/images/downloadsIcon16.png")));
        manualDownloadButton.setInheritsPopupMenu(false);
        manualDownloadButton.setMargin(new Insets(2, 2, 2, 8));
        manualDownloadButton.setOpaque(true);
        manualDownloadButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(manualDownloadButton, ResourceBundle.getBundle("translations/UpdateDialogBundle").getString("manualDownloadButtonText"));
        manualDownloadButton.setToolTipText(ResourceBundle.getBundle("translations/UpdateDialogBundle").getString("manualDownloadButtonToolTip"));
        panel2.add(manualDownloadButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 7, new Insets(10, 10, 0, 10), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(false);
        panel3.add(progressBar, new GridConstraints(2, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        currentVersionStringLabel = new JLabel();
        this.$$$loadLabelText$$$(currentVersionStringLabel, ResourceBundle.getBundle("translations/UpdateDialogBundle").getString("currentVersionStringLabel"));
        panel3.add(currentVersionStringLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        currentVersionLabel = new JLabel();
        Font currentVersionLabelFont = this.$$$getFont$$$(null, Font.BOLD, -1, currentVersionLabel.getFont());
        if (currentVersionLabelFont != null) currentVersionLabel.setFont(currentVersionLabelFont);
        currentVersionLabel.setText("");
        panel3.add(currentVersionLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        availableVersionStringLabel = new JLabel();
        this.$$$loadLabelText$$$(availableVersionStringLabel, ResourceBundle.getBundle("translations/UpdateDialogBundle").getString("availableVersionStringLabel"));
        panel3.add(availableVersionStringLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        availableVersionLabel = new JLabel();
        Font availableVersionLabelFont = this.$$$getFont$$$(null, Font.BOLD, -1, availableVersionLabel.getFont());
        if (availableVersionLabelFont != null) availableVersionLabel.setFont(availableVersionLabelFont);
        availableVersionLabel.setText("");
        panel3.add(availableVersionLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        newVersionSizeLabel = new JLabel();
        newVersionSizeLabel.setText("");
        panel3.add(newVersionSizeLabel, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unitLabel = new JLabel();
        unitLabel.setText("");
        panel3.add(unitLabel, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateInfoButton = new JButton();
        updateInfoButton.setBorderPainted(false);
        updateInfoButton.setContentAreaFilled(false);
        updateInfoButton.setDefaultCapable(true);
        updateInfoButton.setDoubleBuffered(false);
        updateInfoButton.setEnabled(false);
        updateInfoButton.setIcon(new ImageIcon(getClass().getResource("/images/infoIcon16.png")));
        updateInfoButton.setMargin(new Insets(2, 2, 2, 2));
        updateInfoButton.setOpaque(false);
        updateInfoButton.setRequestFocusEnabled(false);
        updateInfoButton.setText("");
        updateInfoButton.setToolTipText(ResourceBundle.getBundle("translations/UpdateDialogBundle").getString("infoAboutUpdate"));
        panel3.add(updateInfoButton, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverBetaLabel = new JLabel();
        serverBetaLabel.setText("(beta)");
        panel3.add(serverBetaLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        currentBetaLabel = new JLabel();
        currentBetaLabel.setText("(beta)");
        panel3.add(currentBetaLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    public void checkForUpdates() {
        progressBar.setIndeterminate(true);
        updater = UpdaterManager.getUpdaterForCurrentOperatingSystem();
        log.debug("Provided updater: {}", updater);
        if (updater != null) {
            createDefaultActionListeners();

            serverApplicationVersion = updater.getLatestAppVersion();

            initBetaLabel();

            progressBar.setIndeterminate(false);
            availableVersionLabel.setText(serverApplicationVersion.getVersion());
            setNewVersionSizeInfo();

            updateInfoButton.setEnabled(true);


            compareVersions();
        } else {
            removeAllListeners(buttonOK);

            progressBar.setIndeterminate(false);
            buttonOK.setEnabled(true);
            buttonOK.setText(Translation.getTranslatedString("UpdateDialogBundle", "retryButton"));
            buttonOK.addActionListener(e1 -> {
                progressBar.setIndeterminate(true);
                checkForUpdates();
            });
        }
    }

    private void initBetaLabel() {
        serverBetaLabel.setVisible(serverApplicationVersion.isBeta());
        final Beta beta = serverApplicationVersion.getBeta();
        serverBetaLabel.setText("(beta." + beta.getVersion() + ")");
    }

    private void compareVersions() {
        switch (Internal.versionCompare(serverApplicationVersion)) {
            case SERVER_VERSION_IS_NEWER:
                buttonOK.setEnabled(true);
                buttonOK.setText(Translation.getTranslatedString("UpdateDialogBundle", "buttonOk"));
                break;
            case CURRENT_VERSION_IS_NEWER:
                buttonOK.setText(Translation.getTranslatedString("UpdateDialogBundle", "buttonOkDev"));
                buttonOK.setEnabled(PreferencesManager.isDevMode());
                break;
            case VERSIONS_ARE_EQUAL:
                buttonOK.setText(Translation.getTranslatedString("UpdateDialogBundle", "buttonOkUp2Date"));
                break;
        }
    }

    private void createDefaultActionListeners() {
        for (ActionListener actionListener : buttonOK.getActionListeners()) {
            buttonOK.removeActionListener(actionListener);
        }

        for (ActionListener actionListener : buttonCancel.getActionListeners()) {
            buttonCancel.removeActionListener(actionListener);
        }

        buttonOK.addActionListener(e -> {
            updateThread = new Thread(() -> onOK());
            updateThread.start();
        });

        buttonCancel.addActionListener(e -> onCancel());
    }

    public ApplicationVersion getAppVersion() {
        return serverApplicationVersion;
    }

    public JButton getButtonCancel() {
        return buttonCancel;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    private void iniGui() {
        setTitle(Translation.getTranslatedString("UpdateDialogBundle", "windowTitle"));
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        if (IS_WINDOWS_XP) {
            setIconImage(Toolkit.getDefaultToolkit().getImage(UpdateDialog.class.getResource("/images/updateIconWhite256.png")));
        } else {
            setIconImage(Toolkit.getDefaultToolkit().getImage(UpdateDialog.class.getResource("/images/updateIconBlue256.png")));

        }

        serverBetaLabel.setVisible(false);

        createDefaultActionListeners();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initListeners();

        initWindowListeners();


        translate();

        pack();
        setLocation(FrameUtils.getFrameOnCenterLocationPoint(this));
        setSize(new Dimension(400, 170));
        setResizable(false);
    }

    private void initWindowListeners() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void initListeners() {
        initUpdateInfoButton();

        manualDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSetupUrl();
            }

            private void openSetupUrl() {
                log.debug("Calling to download setup manually");
                URL url = null;
                if (updater != null) {
                    if (updater.getLatestAppVersion() != null) {
                        try {
                            log.debug("Trying to open [" + updater.getLatestAppVersion().getDownloadUrl() + "]");
                            url = new URL(updater.getLatestAppVersion().getDownloadUrl());
                            UrlValidator urlValidator = new UrlValidator();
                            UrlsProceed.openUrl(url);
                        } catch (MalformedURLException e1) {
                            openWebsite(url);
                        }
                    } else UrlsProceed.openUrl(StringConstants.UPDATE_WEB_URL);

                } else UrlsProceed.openUrl(StringConstants.UPDATE_WEB_URL);
            }

            private void openWebsite(URL url) {
                log.warn("Could not open setup url: [" + url + "]\n" +
                        "Opening " + StringConstants.UPDATE_WEB_URL);
                UrlsProceed.openUrl(StringConstants.UPDATE_WEB_URL);
            }
        });
    }

    private void initUpdateInfoButton() {
        updateInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onUpdateInfoButton();
            }
        });

        updateInfoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadProperties() {
        initCurrentVersionInfo();

        availableVersionLabel.setText(
                Translation.getTranslatedString("UpdateDialogBundle", "availableVersionLabelUnknown"));
    }

    private void initCurrentVersionInfo() {
        final ApplicationVersion currentApplicationVersion = CoreUtils.getCurrentApplicationVersion();

        currentVersionLabel.setText(currentApplicationVersion.getVersion());

        currentBetaLabel.setVisible(currentApplicationVersion.isBeta());
        final Beta beta = currentApplicationVersion.getBeta();

        currentBetaLabel.setText("(beta." + beta.getVersion() + ")");
    }

    private void onCancel() {
        if (updateThread != null) {
            if (!updateThread.isInterrupted()) {
                updateThread.interrupt();
                log.info("Installation was interrupted: " + updateThread.isInterrupted());
                updateThread = null;
                buttonOK.setEnabled(true);
                progressBar.setValue(0);
                runCleanInstallerFile();
            } else {
                runCleanInstallerFile();
                dispose();
            }
        } else {
            runCleanInstallerFile();
            dispose();
        }
    }

    private void onOK() {
        buttonOK.setEnabled(false);
        if (!Thread.currentThread().isInterrupted()) {
            try {
                updater.startUpdate(serverApplicationVersion);
            } catch (IOException e) {
                if (serverApplicationVersion.getDownloadUrl() != null) {
                    log.warn("Could not start update", e);

                    Translation translation = new Translation("UpdateDialogBundle");

                    NotificationManager.getForcedNotification(this).showErrorNotification(
                            translation.getTranslatedString("unableToUpdateTitle"),
                            translation.getTranslatedString("lostConnectionMessage"));
                } else {
                    log.warn("Could not start update, there is no available version for this system", e);
                    Translation translation = new Translation("UpdateDialogBundle");
                    NotificationManager.getForcedNotification(this).showErrorNotification(
                            translation.getTranslatedString("unableToUpdateTitle"),
                            translation.getTranslatedString("noAvailableVersion"));
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
                dispose();
            }
        } else {
            buttonOK.setEnabled(true);
            buttonCancel.setEnabled(true);
        }

    }

    private void onUpdateInfoButton() {
        if (serverApplicationVersion != null) {
            if (!serverApplicationVersion.getUpdateInfo().isEmpty()) {
                UpdateInfoDialog dialog;
                if (PreferencesManager.isDarkModeEnabledNow()) {
                    JColorful colorful = new JColorful(ApplicationConstants.DARK_MODE_THEME);
                    colorful.colorizeGlobal();
                    dialog = new UpdateInfoDialog(serverApplicationVersion);
                    colorful.colorize(dialog);
                } else {
                    dialog = new UpdateInfoDialog(serverApplicationVersion);
                }
                dialog.setVisible(true);
            }
        }
    }


    private void removeAllListeners(JButton button) {
        for (ActionListener al :
                button.getActionListeners()) {
            button.removeActionListener(al);
        }
    }

    private void runCleanInstallerFile() {
        final String installerFilePath = PathConstants.UPDATE_PATH_FILE + StringConstants.WINDOWS_WEBLOCOPENER_SETUP_NAME
                + serverApplicationVersion.getVersion() + ".exe";
        log.info("Deleting file: " + installerFilePath);
        File installer = new File(installerFilePath);
        installer.deleteOnExit();
    }


    private void setNewVersionSizeInfo() {
        if (serverApplicationVersion.getSize() > 1024 * 1024) {
            double size = serverApplicationVersion.getSize() / 1024 / (double) 1024;
            size = size * 100;
            int i = (int) Math.round(size);
            size = (double) i / 100;
            newVersionSizeLabel.setText(Double.toString(size));
            unitLabel.setText("MB");
        } else {
            newVersionSizeLabel.setText(serverApplicationVersion.getSize() / 1024 + "");
            unitLabel.setText("KB");
        }
    }

    @Override
    public void translate() {
        Translation translation = new Translation("UpdateDialogBundle");
        currentVersionStringLabel.setText(translation.getTranslatedString("currentVersionStringLabel"));
        availableVersionStringLabel.setText(translation.getTranslatedString("availableVersionStringLabel"));
        updateInfoButton.setToolTipText(translation.getTranslatedString("infoAboutUpdate"));
        buttonOK.setText(translation.getTranslatedString("buttonOk"));
        manualDownloadButton.setText(translation.getTranslatedString("manualDownloadButtonText"));
        manualDownloadButton.setToolTipText(translation.getTranslatedString("manualDownloadButtonToolTip"));
        buttonCancel.setText(translation.getTranslatedString("buttonCancel"));
    }
}
