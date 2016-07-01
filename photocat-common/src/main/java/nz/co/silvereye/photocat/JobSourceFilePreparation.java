/*
 * Copyright 2016, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat;

import com.google.common.base.Preconditions;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * <p>This is a process which is configured with a source job
 * file and can be executed to achieve the end goal of
 * producing a thumbnail.  Note that the original JPEG data may
 * need to be modified in order to make it 'parsable'.</p>
 * @author apl
 */

public class JobSourceFilePreparation implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(JobSourceFilePreparation.class);

    private JobSourceFile jobSourceFile = null;

    public JobSourceFilePreparation(JobSourceFile jobSourceFile) {
        Preconditions.checkArgument(null!=jobSourceFile, "the job source file must be provided");
        this.jobSourceFile = jobSourceFile;
    }

    private void createThumbnail(OutputStream outputStream) throws Exception {
        Preconditions.checkArgument(null!=outputStream, "the output stream must be provided");

        Preconditions.checkNotNull(outputStream);

        switch (JobHelper.deriveDataType(jobSourceFile.getFile())) {
            case JPEG:
                createThumbnailJpeg(outputStream);
                break;
            case MOVIE:
                createThumbnailMovie(outputStream);
                break;
        }
    }

    private void createScaledJpeg(File inputFile, OutputStream outputStream) throws TransformerException {
        Preconditions.checkArgument(null!=outputStream, "the output stream must be provided");
        Preconditions.checkArgument(null!=inputFile, "the input file must be provided");

        try {
            BufferedImage bi = ImageIO.read(inputFile);
            BufferedImage scaledBi = Scalr.resize(bi, jobSourceFile.getJob().getMaximumWidthOrHeightPixels());
            ImageIO.write(scaledBi, "JPG", outputStream);
        } catch (Throwable th) {
            throw new TransformerException("unable to process the file into a thumbnail; " + jobSourceFile.getFile(), th);
        }
    }

    private void createThumbnailMovie(OutputStream outputStream) throws Exception {
        Preconditions.checkArgument(null!=outputStream, "the output stream must be provided");

        File ffmpegBinaryFile = jobSourceFile.getJob().getFfmpegBinary();

        // the only (easy) way this can be done for now which actually works
        // is to call out to ffmpeg to produce a snapshot from the video
        // data.

        File frameTempFile = null;

        try {
            frameTempFile = File.createTempFile(jobSourceFile.getCode() + "-FRAME", ".JPG");

            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegBinaryFile.getAbsolutePath(),
                    "-y",
                    "-i",
                    jobSourceFile.getFile().getAbsolutePath(),
                    "-ss",
                    "00:00:00.000",
                    "-vframes",
                    "1",
                    "-an",
                    frameTempFile.getAbsolutePath());

            LOGGER.info("launching ffmpeg to create a frame-grab from the movie; {}",jobSourceFile.getFile().getName());

            Process proc = pb.start();

            if (0 == proc.waitFor()) {
                createScaledJpeg(frameTempFile, outputStream);
            }
        } finally {
            if(null!=frameTempFile) {
                if (frameTempFile.exists()) {
                    frameTempFile.delete();
                }
            }
        }
    }

    private void createThumbnailJpeg(OutputStream outputStream) throws Exception {

        Preconditions.checkArgument(null!=outputStream, "the output stream must be provided");

        // first strip any EXIF data from the JPEG file.  This seems to be a problem
        // which can happen sometimes;
        // http://stackoverflow.com/questions/4470958/why-does-loading-this-jpg-using-javaio-give-cmmexception
        // (plus other cases -- see google).
        // It seems that the JPEG parser in java 1.6 has problems reading some of the
        // data in the EXIF or JFIF file.  I tried a few things like using Senselan
        // and another library, but neither approach worked so I just strip that data
        // out myself using a rudimentary JPEG parser and then it seems to be able to
        // cope.  Maybe not the best, but I'm sure it is fine when making some basic
        // thumbnails.

        File massagedJpegFile = null;

        try {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try {
                massagedJpegFile = File.createTempFile(jobSourceFile.getCode() + "-STRIPPED", ".JPG");
                fis = new FileInputStream(jobSourceFile.getFile());
                fos = new FileOutputStream(massagedJpegFile);
                bis = new BufferedInputStream(fis, 10 * 1024);
                bos = new BufferedOutputStream(fos, 10 * 1024);

                LOGGER.info("stripping unnecessary data (into temporary file) from; {}",jobSourceFile.getName());

                JpegHelper.passThroughJpegImageWithUnnecessarySegmentsStripped(bis, bos);
            } catch (Throwable th) {
                throw new Exception("unable to strip EXIF data from the JPEG file; " + jobSourceFile.getFile(), th);
            } finally {
                Closeables.closeQuietly(bis);
                Closeables.closeQuietly(bos);
                Closeables.closeQuietly(fis);
                Closeables.closeQuietly(fos);
            }

            createScaledJpeg(massagedJpegFile, outputStream);

        } finally {
            if (null != massagedJpegFile) {
                if (!massagedJpegFile.delete()) {
                    LOGGER.warn("unable to delete the intermediate jpeg file; {}", massagedJpegFile);
                }
            }
        }
    }

    public void run() {
        FileOutputStream fos = null;

        try {
            jobSourceFile.setThumbnailFile(File.createTempFile(jobSourceFile.getCode() + "-THUMB", ".JPG"));
            fos = new FileOutputStream(jobSourceFile.getThumbnailFile());
            createThumbnail(fos);
        } catch (Exception e) {
            jobSourceFile.setThumbnailFile(null);
            LOGGER.error("unable to convert the file; {}",jobSourceFile.getName(), e);
        } finally {
            Closeables.closeQuietly(fos);
        }
    }

}
