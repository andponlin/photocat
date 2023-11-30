/*
 * Copyright 2016-2023, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class represents a photo-cat producing job which is to be run.  It
 * maintains the <em>specification</em> of the job and the state of the
 * running job.</p>
 */

public abstract class AbstractJob {

    /**
     * <p>This is the source directory from which images
     * will be obtained.</p>
     */

    private File sourceDirectory;

    /**
     * <p>Video material can be converted to stills in
     * order to incorporate them into a sheet; to do this
     * it unfortunately has to rely on FFMPEG tool because
     * all of the "pure java" options appear to be quite
     * poor.  This member variable points to where the
     * job should find the FFMPEG binary</p>
     */

    private File ffmpegBinary = new File(Constants.PATHDEFAULT_FFMPEG);

    /**
     * <p>The quantity of threads that will be launched
     * concurrently to process images.</p>
     */

    private int threadCount = Runtime.getRuntime().availableProcessors();

    /**
     * <p>This is the "longest length" of an image in pixels
     * as it will appear in the output.  Obviously, the larger
     * the side, the larger the images and the larger the PDF.
     * </p>
     */

    private int maximumWidthOrHeightPixels;

    /**
     * <p>These are the source files for the job.</p>
     */

    private final Map<String, JobSourceFile> sourceFiles = new HashMap<>();

    // These member variables are thread-safe and are intended
    // to be used for managing the state of the job.

    private boolean cancelled = false;
    private boolean failed = false;
    private boolean completed = false;
    private int percentage = 0;
    private String message = null;

    // -------------------------------------------------
    // RUN STATE MANAGEMENT
    // -------------------------------------------------

    public synchronized void resetRunState() {
        setPercentage(0);
        cancelled = false;
        failed = false;
        completed = false;
        message = null;
    }

    public synchronized boolean isRunning() {
        return !isCompleted() && !isCancelled() && !isFailed();
    }

    public synchronized boolean isCompleted() {
        return completed;
    }

    public synchronized void setCompleted() {
        completed = true;
    }

    public synchronized boolean isFailed() {
        return failed;
    }

    public synchronized void setFailed() {
        failed = true;
    }

    public synchronized boolean isCancelled() {
        return cancelled;
    }

    public synchronized void setCancelled() {
        cancelled = true;
    }

    public synchronized int getPercentage() {
        return percentage;
    }

    public synchronized void setPercentage(int value) {
        percentage = value;
    }

    public synchronized String getMessage() {
        return message;
    }

    public synchronized void setMessage(String value) {
        message = value;
    }

    // -------------------------------------------------
    // SETTINGS / CONFIGURATION
    // -------------------------------------------------

    public int getThreadCount() {
        return threadCount;
    }

    public File getFfmpegBinary() {
        return ffmpegBinary;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public Collection<JobSourceFile> getSourceFiles() {
        return sourceFiles.values();
    }

    public int getMaximumWidthOrHeightPixels() {
        return maximumWidthOrHeightPixels;
    }

    public void addSourceFile(JobSourceFile jsf) {
        sourceFiles.put(jsf.getCode(), jsf);
    }

    public void addSourceFiles(Collection<JobSourceFile> jsfs) {
        for (JobSourceFile jsf : jsfs)
            addSourceFile(jsf);
    }

    public void setThreadCount(int value) {
        threadCount = value;
    }

    public void setFfmpegBinary(File value) {
        ffmpegBinary = value;
    }

    public void setSourceDirectory(File value) {
        sourceDirectory = value;
    }

    public void setMaximumWidthOrHeightPixels(int value) {
        maximumWidthOrHeightPixels = value;
    }

}
