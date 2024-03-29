/*
 * Copyright 2016-2023, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobHelper {

    /**
     * <p>These are the genres of image input; basically either
     * a movie or a still image.  In either case, the enum has a
     * list of file extensions which can be used to help detect
     * if the file is a still or a movie.</p>
     *
     * @author apl
     */

    public enum DataType {
        UNKNOWN(new String[]{}),
        JPEG(new String[]{"jpeg", "jpg"}),
        MOVIE(new String[]{"avi", "mpeg", "mov", "ogg"});

        final String[] extensions;

        DataType(String[] extensions) {
            this.extensions = extensions;
        }

        public String[] getExtensions() {
            return extensions;
        }
    };

    /**
     * <p>This method will take the file and will return the type
     * of file; basically either unknown, movie or still image.
     * It will not return NULL.</p>
     */

    static DataType deriveDataType(File file) {
        Preconditions.checkArgument(null!=file, "the file must be provided");

        String name = file.getName();
        int lastDot = name.lastIndexOf('.');

        if (-1 != lastDot) {
            String extension = name.substring(lastDot + 1).toLowerCase();

            for (DataType dt : DataType.values()) {
                for (String dtE : dt.getExtensions()) {
                    if (extension.equals(dtE))
                        return dt;
                }
            }
        }

        return DataType.UNKNOWN;
    }

    /**
     * <p>This method will assemble and store the list of source
     * files for the job.  Note that it does not actually push
     * the job files into the job - just assembles the list.  It
     * will return the list of job files.</p>
     */

    public static List<JobSourceFile> assembleJobSourceFiles(AbstractJob job) {
        List<JobSourceFile> result = new ArrayList<>();
        assembleJobSourceFiles(result, job, job.getSourceDirectory());
        Collections.sort(result);
        return result;
    }

    protected static void assembleJobSourceFiles(
            List<JobSourceFile> result,
            AbstractJob job,
            File directoryF) {
        Preconditions.checkArgument(null!=result, "the result list must be provided");
        Preconditions.checkArgument(null!=job, "the job must be provided");

        if (null == directoryF)
            return;

        if ('.' == directoryF.getName().charAt(0)) // linux and mac put '.' in front of hidden directory
            return;

        for (File leaf : directoryF.listFiles()) {
            if (leaf.isFile() && ('.' != leaf.getName().charAt(0))) // linux puts '.' in front of hidden files
            {
                switch (deriveDataType(leaf)) {
                    case JPEG -> {
                        java.util.Date leafT = null;
                        String description = null;

                        try {
                            Metadata metadata = ImageMetadataReader.readMetadata(leaf);

                            if (null != metadata) {
                                leafT = metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)
                                        .stream()
                                        .filter(d -> d.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL))
                                        .findFirst()
                                        .map(d -> d.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL))
                                        .orElse(leafT);
                                description = metadata.getDirectoriesOfType(ExifIFD0Directory.class)
                                        .stream()
                                        .filter(d -> d.containsTag(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION))
                                        .findFirst()
                                        .map(d -> d.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION))
                                        .orElse(description);
                            }
                        } catch (IOException ioe) {
                            Logger logger = Logger.getLogger(Constants.LOGGER);

                            if (logger.isLoggable(Level.WARNING)) {
                                logger.log(Level.WARNING, "unable to read the meta data on the file; " + leaf, ioe);
                            }
                        } catch (ImageProcessingException ipe) {
                            Logger logger = Logger.getLogger(Constants.LOGGER);

                            if (logger.isLoggable(Level.WARNING))
                                logger.log(Level.WARNING, "unable to parse the metadata on the file; " + leaf, ipe);
                        }

                        if (null == leafT)
                            leafT = new java.util.Date(leaf.lastModified());

                        result.add(new JobSourceFile(job, UUID.randomUUID().toString(), leaf, leafT, description));
                    }
                    case MOVIE -> result.add(new JobSourceFile(
                            job,
                            UUID.randomUUID().toString(),
                            leaf,
                            new java.util.Date(leaf.lastModified()),
                            null));
                }
            } else {
                assembleJobSourceFiles(
                        result,
                        job,
                        leaf);
            }
        }

    }

}
