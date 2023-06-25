/*
 * Copyright 2016-2023, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat.contactsheet;

import com.google.common.base.Preconditions;
import nz.co.silvereye.photocat.ProgressIndicatorInterface;
import nz.co.silvereye.photocat.contactsheet.desktop.JobSpecificationFrame;
import nz.co.silvereye.photocat.contactsheet.desktop.ProgressDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>This is a GUI application that presents a window into which
 * the user is able to enter settings for a photo catalogue job,
 * press [Start] and it will generate the PDF output.</p>
 *
 * @author apl
 */

public class PhotoCatalogueDesktop {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSpecificationFrame.class);

    private static PhotoCatalogueDesktop sharedInstance = null;

    public static PhotoCatalogueDesktop singleton() {
        if (null == sharedInstance)
            sharedInstance = new PhotoCatalogueDesktop();

        return sharedInstance;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(
                () -> {
                    try {
                        JobSpecificationFrame.singleton().setVisible(true);
                    } catch (Throwable th) {
                        LOGGER.error("a problem has arisen in the opening the photo catalogue gui", th);
                    }
                });

    }

    /**
     * <p>This method will look into the application's
     * localizations and see if it can find a value to
     * return for a given key.  If it can't find anything
     * then it will just give the key back again.</p>
     */

    public String getLocalizedStringForKey(String key) {
        {
            ResourceBundle rb = ResourceBundle.getBundle(
                    Constants.RSRCBUNDLEPATH_LOCALIZATIONS,
                    Locale.getDefault());

            if (rb.containsKey(key))
                return rb.getString(key);
        }

        {
            ResourceBundle rb = ResourceBundle.getBundle(
                    Constants.RSRCBUNDLEPATH_LOCALIZATIONS,
                    Locale.getDefault());

            if (rb.containsKey(key))
                return rb.getString(key);
        }
        return key;
    }

    public void run(Job job) {
        Preconditions.checkNotNull(job);

        job.resetRunState();
        ProgressDialog.singleton().init(job);
        JobAndProgress jobAndProgress = new JobAndProgress(job, ProgressDialog.singleton());

        Thread th = new Thread(jobAndProgress);
        th.start();

        ProgressDialog.singleton().setVisible(true); // see handling in the panel itself for stopping being modal.

        ProgressDialog.singleton().cleanup(job);
        job.resetRunState();
    }

    /**
     * <p>This class represents a job and a progress listener
     * that can be run in a thread.  This is used in the run
     * method in this application.</p>
     */

    private static class JobAndProgress implements Runnable {
        private final Job job;
        private final ProgressIndicatorInterface progressIndicator;

        private JobAndProgress(
                Job job,
                ProgressIndicatorInterface progressIndicator) {
            super();

            Preconditions.checkNotNull(job);

            this.job = job;
            this.progressIndicator = progressIndicator;
        }

        public void run() {
            PhotoCatalogueEngine pce = new PhotoCatalogueEngine();
            try {
                pce.run(job, progressIndicator);
            } catch (Exception e) {
                LOGGER.error("untrapped exception processing a job", e);
            }
        }

    }

}
