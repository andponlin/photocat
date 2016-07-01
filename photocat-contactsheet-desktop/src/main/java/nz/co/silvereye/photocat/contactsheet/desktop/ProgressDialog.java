/*
 * Copyright 2016, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat.contactsheet.desktop;

import nz.co.silvereye.photocat.ProgressIndicatorInterface;
import nz.co.silvereye.photocat.contactsheet.Constants;
import nz.co.silvereye.photocat.contactsheet.Job;
import nz.co.silvereye.photocat.contactsheet.PhotoCatalogueDesktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProgressDialog extends JDialog implements ProgressIndicatorInterface {

    private static final long serialVersionUID = 1L;

    private static ProgressDialog sharedInstance = null;

    @SuppressWarnings("FieldCanBeLocal")
    private JPanel contentPane;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JButton cancelButton;

    private Job job = null;

    public static ProgressDialog singleton() {
        if (null == sharedInstance) {
            sharedInstance = new ProgressDialog();
        }

        return sharedInstance;
    }

    private ProgressDialog() {
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        setBounds(100, 100, 240, 130);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        contentPane.setLayout(gbl_contentPane);

        progressBar = new JProgressBar();
        GridBagConstraints gbc_progressBar = new GridBagConstraints();
        gbc_progressBar.weightx = 1.0;
        gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
        gbc_progressBar.insets = new Insets(0, 0, 5, 0);
        gbc_progressBar.gridx = 0;
        gbc_progressBar.gridy = 0;
        contentPane.add(progressBar, gbc_progressBar);

        statusLabel = new JLabel("Status....");
        statusLabel.setForeground(Color.BLACK);
        GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.anchor = GridBagConstraints.WEST;
        gbc_lblStatus.insets = new Insets(0, 0, 5, 0);
        gbc_lblStatus.gridx = 0;
        gbc_lblStatus.gridy = 1;
        contentPane.add(statusLabel, gbc_lblStatus);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(actionEvent -> {
            job.setCancelled();
            updateProgress();
        });

        GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
        gbc_btnNewButton.anchor = GridBagConstraints.EAST;
        gbc_btnNewButton.gridx = 0;
        gbc_btnNewButton.gridy = 3;
        contentPane.add(cancelButton, gbc_btnNewButton);
    }

    public void init(Job job) {
        this.job = job;
        updateProgress();
    }

    public void cleanup(Job job) {
        this.job = null;
        updateProgress();
    }

    //	-------------------------------------------------
    // INTERFACE TO HANDLE STATUS UPDATES
    //	-------------------------------------------------

    public void handleCompletion() {
        EventQueue.invokeLater(this::handleCompletionImpl);
    }

    public void handleFailure() {
        EventQueue.invokeLater(this::handleFailureImpl);
    }


    public void updateProgress() {
        EventQueue.invokeLater(this::updateProgressImpl);
    }

    //	-------------------------------------------------
    // IMPLEMENTATION OF METHODS TO HANDLE STATUS UPDATES
    //	-------------------------------------------------

    private void handleCompletionImpl() {
        PhotoCatalogueDesktop app = PhotoCatalogueDesktop.singleton();

        if (!job.isFailed() && !job.isCancelled()) {
            JOptionPane.showMessageDialog(
                    null,
                    app.getLocalizedStringForKey(Constants.KEY_L_GENERATINGFINISHEDMESSAGE),
                    app.getLocalizedStringForKey(Constants.KEY_L_GENERATINGFINISHEDTITLE),
                    JOptionPane.INFORMATION_MESSAGE);
        }

        setVisible(false);
    }

    private void handleFailureImpl() {
        PhotoCatalogueDesktop app = PhotoCatalogueDesktop.singleton();

        if (!job.isCancelled()) {
            JOptionPane.showMessageDialog(
                    null,
                    app.getLocalizedStringForKey(Constants.KEY_L_GENERATINGERRORMESSAGE),
                    app.getLocalizedStringForKey(Constants.KEY_L_GENERATINGERRORTITLE),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateProgressImpl() {
        if (null == job) {
            progressBar.setIndeterminate(true);
            progressBar.setValue(0);
            statusLabel.setText("");
        } else {
            PhotoCatalogueDesktop app = PhotoCatalogueDesktop.singleton();

            if (job.isCancelled()) {
                progressBar.setValue(job.getPercentage());
                progressBar.setIndeterminate(true);
                statusLabel.setText(app.getLocalizedStringForKey(Constants.KEY_L_STATUSCANCEL));
                cancelButton.setEnabled(false);
            } else {
                progressBar.setIndeterminate(false);
                cancelButton.setEnabled(true);
                progressBar.setValue(job.getPercentage());
                statusLabel.setText(job.getMessage());
            }
        }
    }

}
