/*
 * Copyright 2016, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat.contactsheet.desktop;

import nz.co.silvereye.photocat.JobHelper;
import nz.co.silvereye.photocat.contactsheet.Constants;
import nz.co.silvereye.photocat.contactsheet.Job;
import nz.co.silvereye.photocat.contactsheet.PhotoCatalogueDesktop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * <p>This swing frame (window) allows the user to specify the settings for producing the
 * photos' contact sheets.</p>
 */

public class JobSpecificationFrame extends JFrame implements ActionListener {

    private static Logger LOGGER = LoggerFactory.getLogger(JobSpecificationFrame.class);

    private static final long serialVersionUID = 1L;

    private static JobSpecificationFrame sharedInstance = null;

    @SuppressWarnings("FieldCanBeLocal")
    private JPanel contentPanel;
    private JTextField sourceDirectoryTextField;
    private JTextField pdfOutputFileTextField;
    private JTextField xmlOutputFileTextField;
    private JTextField ffmpegBinaryFileTextField;
    private JTextField customTemplateTextField;
    private JTextField maximumWidthOrHeightPixelsTextField;
    private JButton generatePdfButton;
    private JRadioButton defaultOutputTemplateRadio;
    private JRadioButton customTemplateRadio;

    public static JobSpecificationFrame singleton() {
        if (null == sharedInstance)
            sharedInstance = new JobSpecificationFrame();

        return sharedInstance;
    }

    private JobSpecificationFrame() {
        PhotoCatalogueDesktop app = PhotoCatalogueDesktop.singleton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 319, 435);
        setResizable(false);
        setTitle(app.getLocalizedStringForKey(Constants.KEY_L_JOBSPECIFICATIONWINDOWTITLE));

        contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPanel);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        contentPanel.setLayout(gbl_contentPanel);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        GridBagLayout gridBagLayout = new GridBagLayout();
        settingsPanel.setLayout(gridBagLayout);

        JLabel sourceDirectoryLabel = new JLabel("Source Image/Movie Directory");
        GridBagConstraints gbc_soureceDirectoryLabel = new GridBagConstraints();
        gbc_soureceDirectoryLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_soureceDirectoryLabel.insets = new Insets(5, 5, 0, 5);
        gbc_soureceDirectoryLabel.gridx = 0;
        gbc_soureceDirectoryLabel.gridy = 0;
        settingsPanel.add(sourceDirectoryLabel, gbc_soureceDirectoryLabel);

        sourceDirectoryTextField = new JTextField();
        GridBagConstraints gbc_sourceDirectoryTextField = new GridBagConstraints();
        gbc_sourceDirectoryTextField.insets = new Insets(0, 5, 5, 1);
        gbc_sourceDirectoryTextField.anchor = GridBagConstraints.NORTH;
        gbc_sourceDirectoryTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sourceDirectoryTextField.gridx = 0;
        gbc_sourceDirectoryTextField.gridy = 1;
        settingsPanel.add(sourceDirectoryTextField, gbc_sourceDirectoryTextField);
        sourceDirectoryTextField.setColumns(10);

        JButton sourceDirectoryBrowseButton = new JButton("Browse...");

        sourceDirectoryBrowseButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);

            if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(null, "Locate Directory")) {
                File file = fileChooser.getSelectedFile();
                sourceDirectoryTextField.setText(file.getAbsolutePath());
            }
        });

        GridBagConstraints gbc_sourceDirectoryBrowseButton = new GridBagConstraints();
        gbc_sourceDirectoryBrowseButton.insets = new Insets(0, 0, 5, 0);
        gbc_sourceDirectoryBrowseButton.anchor = GridBagConstraints.SOUTHWEST;
        gbc_sourceDirectoryBrowseButton.gridx = 1;
        gbc_sourceDirectoryBrowseButton.gridy = 1;
        settingsPanel.add(sourceDirectoryBrowseButton, gbc_sourceDirectoryBrowseButton);

        JLabel pdfOutputFileLabel = new JLabel("PDF Output File");
        GridBagConstraints gbc_pdfOutputFileLabel = new GridBagConstraints();
        gbc_pdfOutputFileLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_pdfOutputFileLabel.insets = new Insets(5, 5, 0, 5);
        gbc_pdfOutputFileLabel.gridx = 0;
        gbc_pdfOutputFileLabel.gridy = 2;
        settingsPanel.add(pdfOutputFileLabel, gbc_pdfOutputFileLabel);

        pdfOutputFileTextField = new JTextField();
        GridBagConstraints gbc_pdfOutputFileTextField = new GridBagConstraints();
        gbc_pdfOutputFileTextField.insets = new Insets(0, 5, 5, 1);
        gbc_pdfOutputFileTextField.anchor = GridBagConstraints.NORTH;
        gbc_pdfOutputFileTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_pdfOutputFileTextField.gridx = 0;
        gbc_pdfOutputFileTextField.gridy = 3;
        settingsPanel.add(pdfOutputFileTextField, gbc_pdfOutputFileTextField);
        pdfOutputFileTextField.setColumns(10);

        JButton pdfOutputFileBrowseButton = new JButton("Browse...");

        pdfOutputFileBrowseButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();

            if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(null)) {
                File file = fileChooser.getSelectedFile();
                pdfOutputFileTextField.setText(file.getAbsolutePath());
            }
        });

        GridBagConstraints gbc_pdfOutputFileBrowseButton = new GridBagConstraints();
        gbc_pdfOutputFileBrowseButton.insets = new Insets(0, 0, 5, 0);
        gbc_pdfOutputFileBrowseButton.anchor = GridBagConstraints.SOUTHWEST;
        gbc_pdfOutputFileBrowseButton.gridx = 1;
        gbc_pdfOutputFileBrowseButton.gridy = 3;
        settingsPanel.add(pdfOutputFileBrowseButton, gbc_pdfOutputFileBrowseButton);

        JLabel xmlOutputFileLabel = new JLabel("XML Output File (optional)");
        GridBagConstraints gbc_xmlOutputFileLabel = new GridBagConstraints();
        gbc_xmlOutputFileLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_xmlOutputFileLabel.insets = new Insets(5, 5, 0, 5);
        gbc_xmlOutputFileLabel.gridx = 0;
        gbc_xmlOutputFileLabel.gridy = 4;
        settingsPanel.add(xmlOutputFileLabel, gbc_xmlOutputFileLabel);

        xmlOutputFileTextField = new JTextField();
        GridBagConstraints gbc_xmlOutputFileTextField = new GridBagConstraints();
        gbc_xmlOutputFileTextField.insets = new Insets(0, 5, 5, 1);
        gbc_xmlOutputFileTextField.anchor = GridBagConstraints.NORTH;
        gbc_xmlOutputFileTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_xmlOutputFileTextField.gridx = 0;
        gbc_xmlOutputFileTextField.gridy = 5;
        settingsPanel.add(xmlOutputFileTextField, gbc_xmlOutputFileTextField);
        xmlOutputFileTextField.setColumns(10);

        JButton xmlOutputFileBrowseButton = new JButton("Browse...");

        xmlOutputFileBrowseButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();

            if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(null)) {
                File file = fileChooser.getSelectedFile();
                xmlOutputFileTextField.setText(file.getAbsolutePath());
            }
        });

        GridBagConstraints gbc_xmlOutputFileBrowseButton = new GridBagConstraints();
        gbc_xmlOutputFileBrowseButton.insets = new Insets(0, 0, 5, 0);
        gbc_xmlOutputFileBrowseButton.anchor = GridBagConstraints.SOUTHWEST;
        gbc_xmlOutputFileBrowseButton.gridx = 1;
        gbc_xmlOutputFileBrowseButton.gridy = 5;
        settingsPanel.add(xmlOutputFileBrowseButton, gbc_xmlOutputFileBrowseButton);

        JLabel ffmpegBinaryFileLabel = new JLabel("ffmpeg Binary (optional)");
        GridBagConstraints gbc_ffmpegBinaryFileLabel = new GridBagConstraints();
        gbc_ffmpegBinaryFileLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_ffmpegBinaryFileLabel.insets = new Insets(5, 5, 0, 5);
        gbc_ffmpegBinaryFileLabel.gridx = 0;
        gbc_ffmpegBinaryFileLabel.gridy = 6;
        settingsPanel.add(ffmpegBinaryFileLabel, gbc_ffmpegBinaryFileLabel);

        ffmpegBinaryFileTextField = new JTextField();
        GridBagConstraints gbc_ffmpegBinaryFileTextField = new GridBagConstraints();
        gbc_ffmpegBinaryFileTextField.insets = new Insets(0, 5, 5, 1);
        gbc_ffmpegBinaryFileTextField.anchor = GridBagConstraints.NORTH;
        gbc_ffmpegBinaryFileTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_ffmpegBinaryFileTextField.gridx = 0;
        gbc_ffmpegBinaryFileTextField.gridy = 7;
        settingsPanel.add(ffmpegBinaryFileTextField, gbc_ffmpegBinaryFileTextField);
        ffmpegBinaryFileTextField.setColumns(10);

        File proposedFfmpegBinaryFile = new File(nz.co.silvereye.photocat.Constants.PATHDEFAULT_FFMPEG);

        if (proposedFfmpegBinaryFile.exists())
            ffmpegBinaryFileTextField.setText(proposedFfmpegBinaryFile.getAbsolutePath());

        JButton ffmpegBinaryFileBrowseButton = new JButton("Browse...");

        ffmpegBinaryFileBrowseButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();

            if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
                File file = fileChooser.getSelectedFile();
                ffmpegBinaryFileTextField.setText(file.getAbsolutePath());
            }
        });

        GridBagConstraints gbc_ffmpegBinaryFileBrowseButton = new GridBagConstraints();
        gbc_ffmpegBinaryFileBrowseButton.insets = new Insets(0, 0, 5, 0);
        gbc_ffmpegBinaryFileBrowseButton.anchor = GridBagConstraints.SOUTHWEST;
        gbc_ffmpegBinaryFileBrowseButton.gridx = 1;
        gbc_ffmpegBinaryFileBrowseButton.gridy = 7;
        settingsPanel.add(ffmpegBinaryFileBrowseButton, gbc_ffmpegBinaryFileBrowseButton);

        defaultOutputTemplateRadio = new JRadioButton("Default Output Template");
        defaultOutputTemplateRadio.setSelected(true);
        GridBagConstraints gbc_defaultOutputTemplateRadio = new GridBagConstraints();
        gbc_defaultOutputTemplateRadio.anchor = GridBagConstraints.NORTHWEST;
        gbc_defaultOutputTemplateRadio.insets = new Insets(5, 5, 0, 5);
        gbc_defaultOutputTemplateRadio.gridx = 0;
        gbc_defaultOutputTemplateRadio.gridy = 8;
        settingsPanel.add(defaultOutputTemplateRadio, gbc_defaultOutputTemplateRadio);

        customTemplateRadio = new JRadioButton("Custom Template");
        GridBagConstraints gbc_customTemplateRadio = new GridBagConstraints();
        gbc_customTemplateRadio.anchor = GridBagConstraints.NORTHWEST;
        gbc_customTemplateRadio.insets = new Insets(0, 5, 0, 5);
        gbc_customTemplateRadio.gridx = 0;
        gbc_customTemplateRadio.gridy = 9;
        settingsPanel.add(customTemplateRadio, gbc_customTemplateRadio);

        ButtonGroup outputTemplateButtonGroup = new ButtonGroup();
        outputTemplateButtonGroup.add(customTemplateRadio);
        outputTemplateButtonGroup.add(defaultOutputTemplateRadio);

        customTemplateTextField = new JTextField();
        GridBagConstraints gbc_customTemplateTextField = new GridBagConstraints();
        gbc_customTemplateTextField.anchor = GridBagConstraints.NORTH;
        gbc_customTemplateTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_customTemplateTextField.insets = new Insets(0, 32, 5, 1);
        gbc_customTemplateTextField.gridx = 0;
        gbc_customTemplateTextField.gridy = 10;
        settingsPanel.add(customTemplateTextField, gbc_customTemplateTextField);
        customTemplateTextField.setColumns(10);

        JButton customTemplateBrowseButton = new JButton("Browse...");

        customTemplateBrowseButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();

            if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
                File file = fileChooser.getSelectedFile();
                customTemplateTextField.setText(file.getAbsolutePath());
                customTemplateRadio.setSelected(true);
            }
        });

        GridBagConstraints gbc_customTemplateBrowseButton = new GridBagConstraints();
        gbc_customTemplateBrowseButton.insets = new Insets(0, 0, 5, 0);
        gbc_customTemplateBrowseButton.gridx = 1;
        gbc_customTemplateBrowseButton.gridy = 10;
        settingsPanel.add(customTemplateBrowseButton, gbc_customTemplateBrowseButton);

        JLabel maximumWidthOrHeightPixelsLabel = new JLabel("Maximum Thumbnail Edge Size");
        GridBagConstraints gbc_maximumWidthOrHeightPixelsLabel = new GridBagConstraints();
        gbc_maximumWidthOrHeightPixelsLabel.anchor = GridBagConstraints.WEST;
        gbc_maximumWidthOrHeightPixelsLabel.insets = new Insets(5, 5, 0, 5);
        gbc_maximumWidthOrHeightPixelsLabel.gridx = 0;
        gbc_maximumWidthOrHeightPixelsLabel.gridy = 11;
        settingsPanel.add(maximumWidthOrHeightPixelsLabel, gbc_maximumWidthOrHeightPixelsLabel);

        JPanel panel = new JPanel();
        panel.setBorder(null);
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.WEST;
        gbc_panel.insets = new Insets(0, 5, 5, 5);
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 12;
        settingsPanel.add(panel, gbc_panel);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        maximumWidthOrHeightPixelsTextField = new JTextField();
        maximumWidthOrHeightPixelsTextField.setText(Integer.toString(Constants.DEFAULT_MAXIMUMWIDTHORHEIGHTPIXELS));
        panel.add(maximumWidthOrHeightPixelsTextField);
        maximumWidthOrHeightPixelsTextField.setColumns(5);

        JLabel maximumWidthOrHeightPixelsTextFieldPixelsLabel = new JLabel("pixels");
        panel.add(maximumWidthOrHeightPixelsTextFieldPixelsLabel);

        GridBagConstraints gbc_settingsPanel = new GridBagConstraints();
        gbc_settingsPanel.anchor = GridBagConstraints.NORTH;
        gbc_settingsPanel.insets = new Insets(0, 0, 0, 0);
        gbc_settingsPanel.gridx = 0;
        gbc_settingsPanel.gridy = 0;
        contentPanel.add(settingsPanel, gbc_settingsPanel);

        generatePdfButton = new JButton("Generate PDF");
        generatePdfButton.addActionListener(this);
        GridBagConstraints gbc_generatePdfButton = new GridBagConstraints();
        gbc_generatePdfButton.anchor = GridBagConstraints.NORTHEAST;
        gbc_generatePdfButton.gridx = 0;
        gbc_generatePdfButton.gridy = 1;
        contentPanel.add(generatePdfButton, gbc_generatePdfButton);

        reset();
    }

    /**
     * <p>This method will reset the user interface to stored defaults or
     * derived defaults.</p>
     */

    private void reset() {
        Preferences prefs = Preferences.userNodeForPackage(JobSpecificationFrame.class); // gui prefs

        sourceDirectoryTextField.setText(prefs.get(Constants.KEY_CONFIG_SOURCEDIRECTORY, ""));
        pdfOutputFileTextField.setText(prefs.get(Constants.KEY_CONFIG_PDFOUTPUTFILE, ""));
        xmlOutputFileTextField.setText(prefs.get(Constants.KEY_CONFIG_XMLOUTPUTFILE, ""));
        ffmpegBinaryFileTextField.setText(prefs.get(Constants.KEY_CONFIG_FFMPEGBINARYFILE, ""));
        customTemplateTextField.setText(prefs.get(Constants.KEY_CONFIG_CUSTOMTEMPLATEFILE, ""));
        maximumWidthOrHeightPixelsTextField.setText(prefs.get(Constants.KEY_CONFIG_MAXIMUMWIDTHORHEIGHTPIXELS, Integer.toString(Constants.DEFAULT_MAXIMUMWIDTHORHEIGHTPIXELS)));

        if (Boolean.valueOf(prefs.get(Constants.KEY_CONFIG_USECUSTOMTEMPLATEFILEFLAG, Boolean.FALSE.toString()))) {
            defaultOutputTemplateRadio.setSelected(false);
            customTemplateRadio.setSelected(true);
        } else {
            defaultOutputTemplateRadio.setSelected(true);
            customTemplateRadio.setSelected(false);
        }

        // if there is an obvious ffmpeg binary present then use it.

        if (0 == ffmpegBinaryFileTextField.getText().length()) {
            File proposedFfmpegBinaryFile = new File(nz.co.silvereye.photocat.Constants.PATHDEFAULT_FFMPEG);

            if (proposedFfmpegBinaryFile.exists())
                ffmpegBinaryFileTextField.setText(proposedFfmpegBinaryFile.getAbsolutePath());
        }
    }

    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == generatePdfButton) {
            PhotoCatalogueDesktop app = PhotoCatalogueDesktop.singleton();

            boolean validationFailed = false;

            // check the maximum width or height fulfill sensible values
            // and can be parsed as an integer.

            String maximumWidthOrHeightPixelsS = maximumWidthOrHeightPixelsTextField.getText().trim();
            int maximumWidthOrHeightPixels = -1;

            if (0 != maximumWidthOrHeightPixelsS.length()) {
                try {
                    maximumWidthOrHeightPixels = Integer.parseInt(maximumWidthOrHeightPixelsS);
                } catch (NumberFormatException nfe) { /* ignore */ }
            }

            if (
                    (maximumWidthOrHeightPixels < Constants.MINIMUM_MAXIMUMWIDTHORHEIGHTPIXELS) ||
                            (maximumWidthOrHeightPixels > Constants.MAXIMUM_MAXIMUMWIDTHORHEIGHTPIXELS)) {
                validationFailed = true;
                JOptionPane.showMessageDialog(
                        null,
                        app.getLocalizedStringForKey(Constants.KEY_L_BADMAXIMUMWIDTHORHEIGHTPIXELS),
                        app.getLocalizedStringForKey(Constants.KEY_L_JOBSPECIFICATIONERROR),
                        JOptionPane.WARNING_MESSAGE);
            }

            // check the source directory exists and also contains some
            // files.

            String sourceDirectoryS = sourceDirectoryTextField.getText().trim();
            File sourceDirectory = null;

            if (0 != sourceDirectoryS.length())
                sourceDirectory = new File(sourceDirectoryS);

            if ((null == sourceDirectory) || !sourceDirectory.exists() || !sourceDirectory.isDirectory() || (0 == sourceDirectory.list().length)) {
                validationFailed = true;
                JOptionPane.showMessageDialog(
                        null,
                        app.getLocalizedStringForKey(Constants.KEY_L_BADSOURCEDIRECTORY),
                        app.getLocalizedStringForKey(Constants.KEY_L_JOBSPECIFICATIONERROR),
                        JOptionPane.WARNING_MESSAGE);
            }

            // check that the PDF output file is present.

            String pdfOutputFileS = pdfOutputFileTextField.getText().trim();
            File pdfOutputFile = null;

            if (0 != pdfOutputFileS.length())
                pdfOutputFile = new File(pdfOutputFileS);

            if (null == pdfOutputFile) {
                validationFailed = true;
                JOptionPane.showMessageDialog(
                        null,
                        app.getLocalizedStringForKey(Constants.KEY_L_BADPDFOUTPUTFILE),
                        app.getLocalizedStringForKey(Constants.KEY_L_JOBSPECIFICATIONERROR),
                        JOptionPane.WARNING_MESSAGE);
            }

            // if the XML output file is present then use it.

            String xmlOutputFileS = xmlOutputFileTextField.getText().trim();
            File xmlOutputFile = null;

            if (0 != xmlOutputFileS.length())
                xmlOutputFile = new File(xmlOutputFileS);

            // check the FFMPEG file - optional

            String ffmpegBinaryFileS = ffmpegBinaryFileTextField.getText().trim();
            File ffmpegBinaryFile = null;

            if (0 != ffmpegBinaryFileS.length())
                ffmpegBinaryFile = new File(ffmpegBinaryFileS);

            if (null != ffmpegBinaryFile) {
                if (!ffmpegBinaryFile.exists() || !ffmpegBinaryFile.isFile() || !ffmpegBinaryFile.canExecute()) {
                    validationFailed = true;
                    JOptionPane.showMessageDialog(
                            null,
                            app.getLocalizedStringForKey(Constants.KEY_L_BADFFMPEGBINARYFILE),
                            app.getLocalizedStringForKey(Constants.KEY_L_JOBSPECIFICATIONERROR),
                            JOptionPane.WARNING_MESSAGE);
                }
            }

            // check the optional XSLT file.

            File customTemplate = null;

            if (customTemplateRadio.isSelected()) {
                String customTemplateS = customTemplateTextField.getText().trim();

                if (0 != customTemplateS.length())
                    customTemplate = new File(customTemplateS);

                if ((null == customTemplate) || !customTemplate.exists() || !customTemplate.isFile()) {
                    validationFailed = true;
                    JOptionPane.showMessageDialog(
                            null,
                            app.getLocalizedStringForKey(Constants.KEY_L_BADCUSTOMTEMPLATE),
                            app.getLocalizedStringForKey(Constants.KEY_L_JOBSPECIFICATIONERROR),
                            JOptionPane.WARNING_MESSAGE);
                }
            }

            // if everything was OK then create the job and execute
            // the job.

            Job job = null;

            if (!validationFailed) {
                job = new Job();

                job.setMaximumWidthOrHeightPixels(maximumWidthOrHeightPixels);
                job.setOutputFilePdf(pdfOutputFile.getAbsoluteFile());
                job.setSourceDirectory(sourceDirectory);

                if (null != xmlOutputFile)
                    job.setOutputFileXml(xmlOutputFile.getAbsoluteFile());

                if (null != ffmpegBinaryFile)
                    job.setFfmpegBinary(ffmpegBinaryFile.getAbsoluteFile());

                if (null != customTemplate) {
                    try {
                        job.setFoTransformUrlXsl(customTemplate.toURI().toURL());
                    } catch (MalformedURLException mue) {
                        throw new IllegalStateException("unable to form a URL for '" + customTemplate + "'");
                    }
                } else {
                    job.setFoTransformUrlXsl(JobSpecificationFrame.class.getResource(Constants.RSRCPATH_DEFAULTXSLT));
                }

                // prepare the job sources.

                job.addSourceFiles(JobHelper.assembleJobSourceFiles(job));

                if (0 == job.getSourceFiles().size()) {
                    validationFailed = true;
                    JOptionPane.showMessageDialog(
                            null,
                            app.getLocalizedStringForKey(Constants.KEY_L_NOSOURCEFILES),
                            app.getLocalizedStringForKey(Constants.KEY_L_JOBSPECIFICATIONERROR),
                            JOptionPane.WARNING_MESSAGE);
                }
            }

            if ((null != job) && !validationFailed)
                PhotoCatalogueDesktop.singleton().run(job);

            // [apl 27.apr.2012]
            // now store some of those settings as preferences in order
            // that it is more convenient for the next time the tool
            // is run.

            if ((null != job) && !validationFailed) {
                Preferences prefs = Preferences.userNodeForPackage(JobSpecificationFrame.class); // gui prefs

                prefs.put(Constants.KEY_CONFIG_SOURCEDIRECTORY, sourceDirectoryTextField.getText());
                prefs.put(Constants.KEY_CONFIG_PDFOUTPUTFILE, pdfOutputFileTextField.getText());
                prefs.put(Constants.KEY_CONFIG_XMLOUTPUTFILE, xmlOutputFileTextField.getText());
                prefs.put(Constants.KEY_CONFIG_FFMPEGBINARYFILE, ffmpegBinaryFileTextField.getText());
                prefs.put(Constants.KEY_CONFIG_CUSTOMTEMPLATEFILE, customTemplateTextField.getText());
                prefs.put(Constants.KEY_CONFIG_MAXIMUMWIDTHORHEIGHTPIXELS, maximumWidthOrHeightPixelsTextField.getText());
                prefs.put(Constants.KEY_CONFIG_USECUSTOMTEMPLATEFILEFLAG, Boolean.toString(customTemplateRadio.isSelected()));

                try {
                    prefs.sync();
                } catch (BackingStoreException bse) {
                    LOGGER.error("unable to store the preferences for job setup",bse);
                }
            }
        }
    }

}
