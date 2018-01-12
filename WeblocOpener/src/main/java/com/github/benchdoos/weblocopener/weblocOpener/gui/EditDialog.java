package com.github.benchdoos.weblocopener.weblocOpener.gui;

import com.github.benchdoos.weblocopener.commons.core.ApplicationConstants;
import com.github.benchdoos.weblocopener.commons.core.Translation;
import com.github.benchdoos.weblocopener.commons.utils.FrameUtils;
import com.github.benchdoos.weblocopener.commons.utils.MessagePushable;
import com.github.benchdoos.weblocopener.commons.utils.UserUtils;
import com.github.benchdoos.weblocopener.weblocOpener.service.UrlsProceed;
import com.github.benchdoos.weblocopener.weblocOpener.service.gui.ClickListener;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import static com.github.benchdoos.weblocopener.commons.utils.Logging.getCurrentClassName;

public class EditDialog extends JFrame implements MessagePushable {
    final static int DEFAULT_APPLICATION_WIDTH = 350;
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    private final static int DEFAULT_APPLICATION_HEIGHT = 200;
    private String path = "";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField;
    private JLabel iconLabel;
    private JLabel urlLabel;
    private JTextPane errorTextPane;
    private JPanel errorPanel;
    private String incorrectUrlMessage = "Incorrect URL";
    private String errorTitle = "Error";
    private Timer messageTimer;
    private JLabel createWeblocFileTextPane;

    @SuppressWarnings("unchecked")
    public EditDialog(String pathToEditingFile) {
        translateDialog();

        initGui(pathToEditingFile);

        log.debug("Got path: [" + pathToEditingFile + "]");
    }

    private void initGui(String pathToEditingFile) {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon96.png")));

        this.path = pathToEditingFile;
        setContentPane(contentPane);


        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                if (textField.getText().isEmpty()) {
                    fillTextFieldWithClipboard();
                }
                super.windowActivated(e);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        createWeblocFileTextPane.setBackground(new Color(232, 232, 232));


        initTextField(pathToEditingFile);

        pack();

        /*setMinimumSize(getSize());
        setPreferredSize(getSize());*/

        setSize(DEFAULT_APPLICATION_WIDTH, 200);
        setResizable(false); //TODO fix setMaximumSize

        setLocation(FrameUtils.getFrameOnCenterLocationPoint(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        UrlsProceed.shutdownLogout();
    }

    private void fillTextField(String pathToEditingFile) {
        try {
            URL url = new URL(UrlsProceed.takeUrl(new File(pathToEditingFile)));
            textField.setText(url.toString());
            textField.setCaretPosition(textField.getText().length());
            textField.selectAll();
            log.debug("Got URL [" + url + "] from [" + pathToEditingFile + "]");
        } catch (Exception e) {
            log.warn("Can not read url from: [" + pathToEditingFile + "]");
            fillTextFieldWithClipboard();
        }
    }

    private void fillTextFieldWithClipboard() {
        String data = "<empty clipboard>";
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            URL url = new URL(data);
            UrlValidator urlValidator = new UrlValidator();
            if (urlValidator.isValid(data)) {
                textField.setText(url.toString());
                setTextFieldFont(textField.getFont(), TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                textField.setCaretPosition(textField.getText().length());
                textField.selectAll();
                log.debug("Got URL from clipboard: " + url);
            }
        } catch (UnsupportedFlavorException | IllegalStateException | HeadlessException | IOException e) {
            textField.setText("");
            log.warn("Can not read URL from clipboard: [" + data + "]", e);
        }
    }

    private void initTextField(String pathToEditingFile) {
        textField.addMouseListener(new ClickListener() {
            @Override
            public void doubleClick(MouseEvent e) {
                textField.selectAll();
            }
        });

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTextFont();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTextFont();
            }

            private void updateTextFont() {
                UrlValidator urlValidator = new UrlValidator();
                if (urlValidator.isValid(textField.getText())) {
                    if (textField != null) {
                        setTextFieldFont(textField.getFont(), TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                        textField.setForeground(Color.BLUE);
                    }
                } else {
                    if (textField != null) {
                        setTextFieldFont(textField.getFont(), TextAttribute.UNDERLINE, -1);
                        textField.setForeground(Color.BLACK);
                    }
                }
            }

        });

        UndoManager undoManager = new UndoManager();
        textField.getDocument().addUndoableEditListener(new UndoableEditListener() {

            public void undoableEditHappened(UndoableEditEvent evt) {
                undoManager.addEdit(evt.getEdit());
            }

        });

        textField.getActionMap().put("Undo",
                new AbstractAction("Undo") {
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            if (undoManager.canUndo()) {
                                undoManager.undo();
                            }
                        } catch (CannotUndoException e) {
                        }
                    }
                });

        textField.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        textField.getActionMap().put("Redo",
                new AbstractAction("Redo") {
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            if (undoManager.canRedo()) {
                                undoManager.redo();
                            }
                        } catch (CannotRedoException e) {
                        }
                    }
                });

        textField.getInputMap().put(KeyStroke.getKeyStroke("control shift Z"), "Redo");

        fillTextField(pathToEditingFile);
    }

    private void onCancel() {
        dispose();
    }

    private void onOK() {
        try {
            URL url = new URL(textField.getText());
            UrlValidator urlValidator = new UrlValidator();
            if (urlValidator.isValid(textField.getText())) {
                UrlsProceed.createWebloc(path, url);
                dispose();
            } else {
                throw new MalformedURLException();
            }
        } catch (MalformedURLException e) {
            log.warn("Can not parse URL: [" + textField.getText() + "]", e);

            String message = incorrectUrlMessage + ": [";
            String incorrectUrl = textField.getText()
                    .substring(0, Math.min(textField.getText().length(), 10));
            //Fixes EditDialog long url message showing issue
            message += textField.getText().length() > incorrectUrl.length() ? incorrectUrl + "...]" : incorrectUrl + "]";


            UserUtils.showWarningMessageToUser(this, errorTitle,
                    message);
        }

    }

    private void setTextFieldFont(Font font, TextAttribute attribute1, int attribute2) {
        Map attributes = font.getAttributes();
        attributes.put(attribute1, attribute2);
        textField.setFont(font.deriveFont(attributes));
    }

    @Override
    public void setVisible(boolean b) {
        FrameUtils.showOnTop(this);
        super.setVisible(b);
    }

    @Override
    public void showMessage(String message, int messageValue) {
        errorPanel.setBackground(MessagePushable.getMessageColor(messageValue));

        boolean wasVisible = errorPanel.isVisible();
        errorPanel.setVisible(true);
        errorTextPane.setText(message);

        if (!wasVisible) {
            updateSize(SettingsDialog.UpdateMode.BEFORE_HIDE);
        }

        if (messageTimer != null) {
            messageTimer.stop();
        }

        messageTimer = new Timer(DEFAULT_TIMER_DELAY, e -> {
            errorTextPane.setText("");
            errorPanel.setVisible(false);
            updateSize(SettingsDialog.UpdateMode.AFTER_HIDE);
        });
        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    private void translateDialog() {
        Translation translation = new Translation("translations/EditDialogBundle") {
            @Override
            public void initTranslations() {
                setTitle(messages.getString("windowTitle"));
                urlLabel.setText(messages.getString("urlLabelText"));
                /*createWeblocFileTextPane.setText(
                        "<html>\n" +
                                "  <body style=\"font-family:Open Sans; font-size:12px;\">" + "\t"
                                + messages.getString("textPane1") + " <b>.webloc</b> "
                                + messages.getString("textPane2") + ":\n"
                                + "  </body>\n" +
                                "</html>\n");*/

                buttonOK.setText(messages.getString("buttonOk"));
                buttonCancel.setText(messages.getString("buttonCancel"));
                iconLabel.setToolTipText(messages.getString("iconLabel") + ApplicationConstants.APP_VERSION);
                incorrectUrlMessage = messages.getString("incorrectUrlMessage");
                errorTitle = messages.getString("errorTitle");
            }
        };
        translation.initTranslations();
    }

    private void updateSize(SettingsDialog.UpdateMode mode) {

        setResizable(true);

        revalidate();
        if (mode == SettingsDialog.UpdateMode.BEFORE_HIDE) {
            pack();
            setSize(new Dimension(DEFAULT_APPLICATION_WIDTH, getHeight()));
        } else if (mode == SettingsDialog.UpdateMode.AFTER_HIDE) {
            setSize(new Dimension(DEFAULT_APPLICATION_WIDTH, DEFAULT_APPLICATION_HEIGHT));
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 10, 10, 10), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        this.$$$loadButtonText$$$(buttonOK, ResourceBundle.getBundle("translations/EditDialogBundle").getString("buttonOk"));
        buttonOK.putClientProperty("hideActionText", Boolean.FALSE);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        this.$$$loadButtonText$$$(buttonCancel, ResourceBundle.getBundle("translations/EditDialogBundle").getString("buttonCancel"));
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 3, new Insets(5, 10, 0, 10), -1, -1));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        createWeblocFileTextPane = new JLabel();
        createWeblocFileTextPane.setAutoscrolls(true);
        createWeblocFileTextPane.setEnabled(true);
        createWeblocFileTextPane.setFocusable(false);
        Font createWeblocFileTextPaneFont = this.$$$getFont$$$("Open Sans", Font.BOLD, 14, createWeblocFileTextPane.getFont());
        if (createWeblocFileTextPaneFont != null) createWeblocFileTextPane.setFont(createWeblocFileTextPaneFont);
        createWeblocFileTextPane.setOpaque(false);
        createWeblocFileTextPane.setRequestFocusEnabled(true);
        this.$$$loadLabelText$$$(createWeblocFileTextPane, ResourceBundle.getBundle("translations/EditDialogBundle").getString("EditWeblocLink"));
        createWeblocFileTextPane.setVerifyInputWhenFocusTarget(false);
        createWeblocFileTextPane.setVisible(true);
        panel3.add(createWeblocFileTextPane, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        urlLabel = new JLabel();
        Font urlLabelFont = this.$$$getFont$$$("Open Sans", Font.BOLD, 14, urlLabel.getFont());
        if (urlLabelFont != null) urlLabel.setFont(urlLabelFont);
        this.$$$loadLabelText$$$(urlLabel, ResourceBundle.getBundle("translations/EditDialogBundle").getString("urlLabelText"));
        panel4.add(urlLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField = new JTextField();
        Font textFieldFont = this.$$$getFont$$$("Open Sans", -1, 12, textField.getFont());
        if (textFieldFont != null) textField.setFont(textFieldFont);
        textField.setText("");
        panel4.add(textField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 1, false));
        iconLabel = new JLabel();
        iconLabel.setIcon(new ImageIcon(getClass().getResource("/icon96.png")));
        iconLabel.setText("");
        panel3.add(iconLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        errorPanel = new JPanel();
        errorPanel.setLayout(new BorderLayout(0, 0));
        errorPanel.setBackground(new Color(-65536));
        errorPanel.setVisible(false);
        contentPane.add(errorPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        errorTextPane = new JTextPane();
        errorTextPane.setEditable(false);
        errorTextPane.setFocusCycleRoot(false);
        errorTextPane.setFocusable(false);
        Font errorTextPaneFont = this.$$$getFont$$$("Segoe UI", Font.BOLD, 14, errorTextPane.getFont());
        if (errorTextPaneFont != null) errorTextPane.setFont(errorTextPaneFont);
        errorTextPane.setForeground(new Color(-1));
        errorTextPane.setOpaque(false);
        errorTextPane.setRequestFocusEnabled(false);
        errorTextPane.setText("Error");
        errorTextPane.setVerifyInputWhenFocusTarget(true);
        errorPanel.add(errorTextPane, BorderLayout.NORTH);
        urlLabel.setLabelFor(textField);
        iconLabel.setLabelFor(createWeblocFileTextPane);
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
}