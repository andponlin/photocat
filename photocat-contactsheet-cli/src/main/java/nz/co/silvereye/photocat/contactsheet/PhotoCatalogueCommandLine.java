/*
 * Copyright 2016-2023, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat.contactsheet;

import nz.co.silvereye.photocat.JobHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * <p>This is the command-line tool that can be used to produce photo
 * catalogues.</p>
 *
 * @author apl
 */

public class PhotoCatalogueCommandLine implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(PhotoCatalogueCommandLine.class);

    @Option(name = "-o", usage = "Output file for PDF data")
    private File outputPdfFile ;

    @Option(name = "-x", usage = "Output file for XML intermediate data")
    private File outputXmlFile;

    @Option(name = "-f", usage = "FFMPEG binary to run to process video data")
    private File ffmpegBinary;

    @Option(name = "-d", required = true, usage = "directory from which to obtain data")
    private File sourceDirectory;

    @Option(name = "-t", usage = "thread count for processing data")
    private Integer threadCount;

    private static void syntax(String message) {
        System.err.println(message);
        System.err.print("java -jar <jarfile>");
        System.err.print(" [-o <outpdffile>]");
        System.err.print(" [-x <outxmlfile>]");
        System.err.print(" [-f <ffmpegbinary>]");
        System.err.print(" [-t <threadcount>]");
        System.err.print(" -d <inputdirectory>");
        System.exit(1);
    }

    public static void main(String args[]) {

        // initialize the logging using SLF4J
        // http://www.slf4j.org/legacy.html#jul-to-slf4j
        // note that there are problems here for higher volume logging.

        {
            java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");

            for(Handler handler : rootLogger.getHandlers())
                rootLogger.removeHandler(handler);

            SLF4JBridgeHandler.install();
        }

        // parse the arguments and launch the application.

        PhotoCatalogueCommandLine main = new PhotoCatalogueCommandLine();
        CmdLineParser parser = new CmdLineParser(main);

        try {
            parser.parseArgument(args);
            main.run();
        }
        catch(CmdLineException cle) {
            syntax(cle.getMessage());
        }
    }

    public void run() {

        Job job = new Job();

        job.setFfmpegBinary(ffmpegBinary);
        job.setOutputFilePdf(outputPdfFile);
        job.setOutputFileXml(outputXmlFile);
        job.setSourceDirectory(sourceDirectory);

        if(null!=threadCount) {
            job.setThreadCount(threadCount);
        }

        if(job.getThreadCount() <= 0) {
            syntax("thread count must be a positive integer");
        }

        if(!job.getSourceDirectory().exists() || !job.getSourceDirectory().isDirectory()) {
            syntax("source directory must exist");
        }

        // find the input files.

        job.setFoTransformUrlXsl(PhotoCatalogueCommandLine.class.getResource(Constants.RSRCPATH_DEFAULTXSLT));
        job.addSourceFiles(JobHelper.assembleJobSourceFiles(job));

        // now process it.

        PhotoCatalogueEngine pce = new PhotoCatalogueEngine();

        try {
            pce.run(job, null);
        }
        catch(Exception e) {
            LOGGER.error("not able to produce output",e);
        }
    }

}
