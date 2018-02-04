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

package com.github.benchdoos.weblocopener.weblocOpener.gui;

import com.github.benchdoos.weblocopener.commons.core.Translation;
import com.github.benchdoos.weblocopener.commons.registry.RegistryCanNotReadInfoException;
import com.github.benchdoos.weblocopener.commons.registry.RegistryCanNotWriteInfoException;
import com.github.benchdoos.weblocopener.commons.registry.RegistryException;
import com.github.benchdoos.weblocopener.commons.registry.RegistryManager;
import com.github.benchdoos.weblocopener.commons.registry.fixer.RegistryFixer;
import com.github.benchdoos.weblocopener.commons.registry.fixer.RegistryFixerAppVersionKeyFailException;
import com.github.benchdoos.weblocopener.commons.registry.fixer.RegistryFixerAutoUpdateKeyFailException;
import com.github.benchdoos.weblocopener.commons.registry.fixer.RegistryFixerInstallPathKeyFailException;
import com.github.benchdoos.weblocopener.commons.utils.FrameUtils;
import com.github.benchdoos.weblocopener.commons.utils.MessagePushable;
import com.github.benchdoos.weblocopener.commons.utils.UserUtils;
import com.github.benchdoos.weblocopener.commons.utils.browser.Browser;
import com.github.benchdoos.weblocopener.commons.utils.browser.BrowserManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.MinimalBalloonStyle;
import net.java.balloontip.utils.TimingUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.github.benchdoos.weblocopener.commons.utils.Logging.getCurrentClassName;

public class SettingsDialog extends JFrame implements MessagePushable {
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    boolean onInit = true;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox autoUpdateEnabledCheckBox;
    private JButton updateNowButton;
    private JLabel versionLabel;
    private JLabel versionStringLabel;
    private JButton aboutButton;
    private JTextPane errorTextPane;
    private JPanel errorPanel;
    private JComboBox<Object> comboBox;
    private JButton updateListButton;
    private JTextField callTextField;
    private JLabel callLabel;
    private JLabel syntaxInfoLabel;
    private JCheckBox incognitoCheckBox;
    private String errorMessageTitle = "Error";
    private String canNotSaveSettingsToRegistryMessage = "Can not save settings to registry.";
    private Timer messageTimer;
    private String toolTipText = "" +
            "<html>" +
            "  <body style=\"font-size:10px;\">Syntax: <b><u>file path</u></b> <b style=\"color:red;\">%site</b>, don't forget to add <b>%site</b>" +
            "  <br>Example for Google Chrome: <b style=\"color:green;\">start chrome \"%site\"</b>" +
            "  </body>" +
            "</html>";

    private String chooseAFile = "Choose a file:";

    private String customBrowserName = "Custom...";

    public SettingsDialog() {
        log.debug("Creating settings dialog.");
        translateDialog();
        initGui();
        log.debug("Settings dialog created.");
    }

    public static void runUpdater() {
        String run;
        try {
            run = "java -jar \"" + RegistryManager.getInstallLocationValue() + "Updater.jar\"";
        } catch (RegistryCanNotReadInfoException e) {
            run = new File(SettingsDialog.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath()).getAbsolutePath().replace("%20", " ");
            log.warn("Can not read registry, using alternate path: [" + run + "]", e);
        }
        log.info("Running: " + run);
        try {
            Runtime.getRuntime().exec(run);
        } catch (IOException e) {
            log.warn("Can not execute command: " + run, e);
        }
    }

    private int findBrowser(String browserValue) {
        int result = 0;
        for (int i = 0; i < BrowserManager.getBrowserList().size(); i++) {
            Browser browser = BrowserManager.getBrowserList().get(i);
            log.debug("Selected value in comboBox: " + browser);

            if (browser.getCall() != null) {
                if (browser.getCall().equals(browserValue)) {
                    result = i;
                    return result;
                } else if (browser.getIncognitoCall() != null) {
                    if (browser.getIncognitoCall().equals(browserValue)) {
                        result = i;
                        return result;
                    }
                }
            }
        }

        if (browserValue.equals("default") || browserValue.isEmpty()) {
            return 0;
        } else return BrowserManager.getBrowserList().size() - 1;
    }

    private BalloonTip generateBalloonTip(String toolTipText) {
        BalloonTip balloonTip = new BalloonTip(syntaxInfoLabel, toolTipText);
        balloonTip.setStyle(new MinimalBalloonStyle(Color.white, 5));
        balloonTip.setCloseButton(null);
        balloonTip.setVisible(false);
        balloonTip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                balloonTip.setVisible(false);
            }
        });
        return balloonTip;
    }

    private void initComboBox() {
        ArrayList<Browser> browsers = BrowserManager.getBrowserList();

        Browser others = new Browser(customBrowserName, null);
        browsers.add(others);

        comboBox.setModel(new DefaultComboBoxModel<>(browsers.toArray()));

        comboBox.addActionListener(e -> {
            if (comboBox.getSelectedIndex() == comboBox.getItemCount() - 1) {
                if (!onInit) {
                    log.info("Opening file browser for custom browser search");
                    String path = openFileBrowser();
                    if (path != null) {
                        callLabel.setVisible(true);
                        callTextField.setVisible(true);
                        callTextField.setText(path);
                        incognitoCheckBox.setEnabled(false);
                    }
                } else {
                    callLabel.setVisible(true);
                    callTextField.setText(RegistryManager.getBrowserValue());
                    callTextField.setVisible(true);
                    syntaxInfoLabel.setVisible(true);
                }
            } else {
                if (comboBox.getSelectedIndex() == 0) {
                    incognitoCheckBox.setEnabled(false);
                    incognitoCheckBox.setSelected(false);
                } else {
                    if (browsers.get(comboBox.getSelectedIndex()).getIncognitoCall() != null) {
                        incognitoCheckBox.setEnabled(true);
                    } else {
                        incognitoCheckBox.setSelected(false);
                        incognitoCheckBox.setEnabled(false);
                    }
                }
                callLabel.setVisible(false);
                callTextField.setVisible(false);
            }
        });
    }

    private void initGui() {
        setContentPane(contentPane);


        getRootPane().setDefaultButton(buttonOK);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/balloonIcon64.png")));

        syntaxInfoLabel.setVisible(false);
        updateListButton.setVisible(false);
        callTextField.setVisible(false);
        callLabel.setVisible(false);

        initComboBox();

        loadSettings();

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        updateNowButton.addActionListener(e -> onUpdateNow());

        aboutButton.addActionListener(e -> onAbout());

        callTextField.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                syntaxInfoLabel.setVisible(false);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                syntaxInfoLabel.setVisible(true);
            }
        });

        setSyntaxInfoButtonToolTip();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        try {
            versionLabel.setText(RegistryManager.getAppVersionValue());
        } catch (RegistryCanNotReadInfoException e) {
            versionLabel.setText("Unknown");
        }

        onInit = false;

        pack();
        setSize(400, 250);
        setLocation(FrameUtils.getFrameOnCenterLocationPoint(this));
        setResizable(false);
    }

    private void loadSettings() {
        try {
            autoUpdateEnabledCheckBox.setSelected(RegistryManager.isAutoUpdateActive());
            comboBox.setSelectedIndex(findBrowser(RegistryManager.getBrowserValue()));
            final Browser browser = (Browser) comboBox.getSelectedItem();

            if (browser != null) {
                if (browser.getIncognitoCall() != null) {
                    incognitoCheckBox.setSelected(RegistryManager.getBrowserValue().equals(browser.getIncognitoCall()));
                } else {
                    incognitoCheckBox.setSelected(false);
                    incognitoCheckBox.setEnabled(false);
                }
            } else {
                incognitoCheckBox.setSelected(false);
                incognitoCheckBox.setEnabled(false);
            }

        } catch (RegistryException e) {
            log.warn("Can not load data from registry", e);
            try {
                RegistryFixer.fixRegistry();
            } catch (FileNotFoundException | RegistryFixerAutoUpdateKeyFailException | RegistryFixerAppVersionKeyFailException e1) {
                RegistryManager.setDefaultSettings(); //To prevent crash
                try {
                    autoUpdateEnabledCheckBox.setSelected(RegistryManager.isAutoUpdateActive());
                } catch (RegistryCanNotReadInfoException ignore) {
                }
            } catch (RegistryFixerInstallPathKeyFailException e1) {
                log.warn("Can not fix install key ", e1);
            }
        }
    }

    private void onAbout() {
        AboutApplicationDialog dialog = new AboutApplicationDialog();
        dialog.setVisible(true);
    }

    private void onCancel() {
        dispose();
    }

    private void onOK() {
        try {
            updateRegistryAndDispose();
        } catch (RegistryException e) {
            log.warn("Can not save settings: " + RegistryManager.KEY_AUTO_UPDATE, e);
            try {
                RegistryFixer.fixRegistryAnyway();
                updateRegistryAndDispose();
            } catch (FileNotFoundException | RegistryException e1) {
                log.error("Can not fix registry", e1);
                UserUtils.showWarningMessageToUser(this, errorMessageTitle, canNotSaveSettingsToRegistryMessage);
            } catch (Exception e1) {
                log.warn("Can not update settings", e1);
            }
        }
    }

    private void onUpdateNow() {
        runUpdater();
        dispose();
    }

    private String openFileBrowser() {
        log.debug("Opening File Browser");

        FileDialog fd = new FileDialog(this, chooseAFile, FileDialog.LOAD);
        fd.setIconImage(Toolkit.getDefaultToolkit()
                .getImage(getClass().getResource("/images/balloonIcon64.png")));
        fd.setDirectory(System.getProperty("user.dir"));
        fd.setFile("*.exe");
        fd.setMultipleMode(false);
        fd.setVisible(true);
        File[] f = fd.getFiles();
        if (f.length > 0) {
            log.debug("Choice: " + fd.getFiles()[0].getAbsolutePath());
            return fd.getFiles()[0].getAbsolutePath();
        } else {
            log.debug("Choice canceled");
            return null;
        }
    }

    private void setSyntaxInfoButtonToolTip() {

        syntaxInfoLabel.addMouseListener(new MouseAdapter() {
            final int DEFAULT_TIME = 10_000;
            final int SHORT_TIME = 6_000;

            BalloonTip balloonTip = generateBalloonTip(toolTipText);

            @Override
            public void mouseClicked(MouseEvent e) {
                balloonTip.setVisible(true);
                TimingUtils.showTimedBalloon(balloonTip, DEFAULT_TIME);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                balloonTip.setVisible(true);
                TimingUtils.showTimedBalloon(balloonTip, SHORT_TIME);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                balloonTip = generateBalloonTip(toolTipText);
            }
        });


    }

    @Override
    public void showMessage(String message, int messageValue) {
        errorPanel.setBackground(MessagePushable.getMessageColor(messageValue));

        boolean wasVisible = errorPanel.isVisible();
        errorPanel.setVisible(true);
        errorTextPane.setText(message);

        if (!wasVisible) {
            updateSize(UpdateMode.BEFORE_HIDE);
        }

        if (messageTimer != null) {
            messageTimer.stop();
        }

        messageTimer = new Timer(DEFAULT_TIMER_DELAY, e -> {
            errorTextPane.setText("");
            errorPanel.setVisible(false);
            updateSize(UpdateMode.AFTER_HIDE);
        });
        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    private void translateDialog() {
        Translation translation = new Translation("translations/SettingsDialogBundle") {
            @Override
            public void initTranslations() {
                setTitle(messages.getString("windowTitle"));

                buttonOK.setText(messages.getString("buttonOk"));
                buttonCancel.setText(messages.getString("buttonCancel"));

                versionStringLabel.setText(messages.getString("versionString"));
                autoUpdateEnabledCheckBox.setText(messages.getString("autoUpdateEnabledCheckBox"));
                updateNowButton.setText(messages.getString("updateNowButton"));

                canNotSaveSettingsToRegistryMessage = messages.getString("canNotSaveSettingsToRegistryMessage");
                errorMessageTitle = messages.getString("errorMessage");

                toolTipText = messages.getString("toolTipText");

                customBrowserName = messages.getString("customBrowserName");
                chooseAFile = messages.getString("chooseAFile");
            }
        };
        translation.initTranslations();
    }

    private void updateRegistryAndDispose() throws RegistryCanNotReadInfoException, RegistryCanNotWriteInfoException {
        if (RegistryManager.isAutoUpdateActive() != autoUpdateEnabledCheckBox.isSelected()) {
            RegistryManager.setAutoUpdateActive(autoUpdateEnabledCheckBox.isSelected());
        }
        Browser browser = (Browser) comboBox.getSelectedItem();
        if (browser != null) {
            log.info("browser call: " + browser.getCall());
            if (comboBox.getSelectedIndex() != comboBox.getItemCount() - 1) {
                if (browser.getCall() != null) {
                    if (!RegistryManager.getBrowserValue().equals(browser.getCall())) {
                        if (!incognitoCheckBox.isSelected()) {
                            RegistryManager.setBrowserValue(browser.getCall());
                        }
                    }
                }
                if (browser.getIncognitoCall() != null) {
                    if (!RegistryManager.getBrowserValue().equals(browser.getIncognitoCall())) {
                        if (incognitoCheckBox.isSelected()) {
                            RegistryManager.setBrowserValue(browser.getIncognitoCall());
                        }
                    }
                }
            } else {
                if (!callTextField.getText().equals(browser.getIncognitoCall())) {
                    RegistryManager.setBrowserValue(callTextField.getText());
                }
            }
        }

        dispose();
    }

    private void updateSize(UpdateMode mode) {

        setResizable(true);
        revalidate();
        final int DEFAULT_APPLICATION_WIDTH = 350;
        if (mode == UpdateMode.BEFORE_HIDE) {
            pack();
            setSize(new Dimension(DEFAULT_APPLICATION_WIDTH, getHeight()));
        } else if (mode == UpdateMode.AFTER_HIDE) {
            setSize(new Dimension(DEFAULT_APPLICATION_WIDTH, 200));
        }
        setResizable(false);

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
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 10, 0), -1, -1));
        contentPane.setMinimumSize(new Dimension(400, 250));
        contentPane.setPreferredSize(new Dimension(400, 250));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 10, 0, 10), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        this.$$$loadButtonText$$$(buttonOK, ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("buttonOk"));
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        this.$$$loadButtonText$$$(buttonCancel, ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("buttonCancel"));
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aboutButton = new JButton();
        this.$$$loadButtonText$$$(aboutButton, ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("buttonAbout"));
        panel1.add(aboutButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(7, 6, new Insets(10, 10, 0, 10), -1, -1));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        autoUpdateEnabledCheckBox = new JCheckBox();
        autoUpdateEnabledCheckBox.setContentAreaFilled(true);
        Font autoUpdateEnabledCheckBoxFont = this.$$$getFont$$$(null, -1, -1, autoUpdateEnabledCheckBox.getFont());
        if (autoUpdateEnabledCheckBoxFont != null) autoUpdateEnabledCheckBox.setFont(autoUpdateEnabledCheckBoxFont);
        autoUpdateEnabledCheckBox.setHorizontalTextPosition(2);
        autoUpdateEnabledCheckBox.setSelected(true);
        this.$$$loadButtonText$$$(autoUpdateEnabledCheckBox, ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("autoUpdateEnabledCheckBox"));
        autoUpdateEnabledCheckBox.setVerifyInputWhenFocusTarget(false);
        autoUpdateEnabledCheckBox.setVerticalAlignment(0);
        panel3.add(autoUpdateEnabledCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(6, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        versionLabel = new JLabel();
        versionLabel.setText("|");
        panel3.add(versionLabel, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        versionStringLabel = new JLabel();
        this.$$$loadLabelText$$$(versionStringLabel, ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("versionString"));
        panel3.add(versionStringLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBox = new JComboBox();
        panel3.add(comboBox, new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("openInBrowser"));
        panel3.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateListButton = new JButton();
        updateListButton.setIcon(new ImageIcon(getClass().getResource("/images/refresh16.png")));
        updateListButton.setMargin(new Insets(2, 2, 2, 2));
        updateListButton.setText("");
        updateListButton.setToolTipText(ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("updateList"));
        panel3.add(updateListButton, new GridConstraints(4, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        callTextField = new JTextField();
        callTextField.setVisible(true);
        panel3.add(callTextField, new GridConstraints(5, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, -1), null, 0, false));
        callLabel = new JLabel();
        this.$$$loadLabelText$$$(callLabel, ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("customCallLabel"));
        callLabel.setVisible(true);
        panel3.add(callLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        incognitoCheckBox = new JCheckBox();
        incognitoCheckBox.setEnabled(false);
        incognitoCheckBox.setText("");
        incognitoCheckBox.setToolTipText(ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("incognitoModeTooltip"));
        panel3.add(incognitoCheckBox, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateNowButton = new JButton();
        this.$$$loadButtonText$$$(updateNowButton, ResourceBundle.getBundle("translations/SettingsDialogBundle").getString("updateNowButton"));
        panel3.add(updateNowButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel3.add(separator1, new GridConstraints(3, 0, 1, 6, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        syntaxInfoLabel = new JLabel();
        syntaxInfoLabel.setIcon(new ImageIcon(getClass().getResource("/images/infoIcon16.png")));
        syntaxInfoLabel.setText("");
        panel3.add(syntaxInfoLabel, new GridConstraints(5, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        errorPanel = new JPanel();
        errorPanel.setLayout(new BorderLayout(0, 0));
        errorPanel.setBackground(new Color(-65536));
        errorPanel.setVisible(false);
        contentPane.add(errorPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        errorTextPane = new JTextPane();
        errorTextPane.setEditable(false);
        errorTextPane.setFocusable(false);
        Font errorTextPaneFont = this.$$$getFont$$$("Segoe UI", Font.BOLD, 14, errorTextPane.getFont());
        if (errorTextPaneFont != null) errorTextPane.setFont(errorTextPaneFont);
        errorTextPane.setForeground(new Color(-1));
        errorTextPane.setOpaque(false);
        errorTextPane.setRequestFocusEnabled(false);
        errorTextPane.setSelectionEnd(5);
        errorTextPane.setSelectionStart(5);
        errorTextPane.setText("Error");
        errorTextPane.setVerifyInputWhenFocusTarget(false);
        errorPanel.add(errorTextPane, BorderLayout.NORTH);
        label1.setLabelFor(comboBox);
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

    enum UpdateMode {BEFORE_HIDE, AFTER_HIDE}
}
