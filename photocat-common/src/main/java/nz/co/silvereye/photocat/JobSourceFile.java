/*
 * Copyright 2016-2023, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat;

import nz.co.silvereye.photocat.JobHelper.DataType;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * <p>This class represents a job source file.  A job source file
 * keeps track of the original file, the thumbnail file if one has
 * been generated yet.  The timestamp of the file (which is probably
 * derived by looking the EXIF data or similar.  Each file also
 * gets a code that can be used to uniquely identify it.</p>
 */

public class JobSourceFile implements Comparable<JobSourceFile> {

    /**
     * <p>This is the original file data; input movie or image for
     * example.</p>
     */

    private final File file;

    /**
     * <p>This file is a pointer to a thumbnail that has been generated
     * for the file.</p>
     */

    private File thumbnailFile;

    /**
     * <p>This timestamp is generated by looking at the EXIF data or
     * even be looking at the timestamp of the file itself.</p>
     */

    private final java.util.Date timestamp;

    /**
     * <p>This code can be used to uniquely identify the source file.</p>
     */

    private final String code;

    /**
     * @since 2016-09-19
     */

    private final String description;

    protected AbstractJob job;

    JobSourceFile(
            AbstractJob job,
            String code,
            File file,
            java.util.Date timestamp,
            String description) {
        super();

        this.job = job;
        this.code = code;
        this.file = file;
        this.timestamp = timestamp;
        this.description = description;

        thumbnailFile = null;
    }

    public DataType getDataType() {
        return JobHelper.deriveDataType(getFile());
    }

    public AbstractJob getJob() {
        return job;
    }

    public String getCode() {
        return code;
    }

    public File getFile() {
        return file;
    }

    public java.util.Date getTimestamp() {
        return timestamp;
    }

    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(File file) {
        thumbnailFile = file;
    }

    public String getName() {
        return getFile().getName();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(JobSourceFile o) {
        int result = getTimestamp().compareTo(o.getTimestamp());
        if (0 != result) return result;
        return getFile().compareTo(o.getFile());
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.SIMPLEDATEFORMAT_SQL92_DATETIME);
        return sdf.format(getTimestamp()) + " : " + getFile().getAbsolutePath();
    }

    // ----------------------------------------------

}
