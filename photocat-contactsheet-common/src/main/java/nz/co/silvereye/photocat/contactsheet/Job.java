/*
 * Copyright 2016, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat.contactsheet;

import nz.co.silvereye.photocat.AbstractJob;
import nz.co.silvereye.photocat.JobSourceFile;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class represents a job which is to be run.  It
 * maintains the <em>specification</em> of the job, but
 * not the state of the running job.</p>
 */

public class Job extends AbstractJob {

    /**
     * <p>This is the file to which the output PDF file
     * will be written.</p>
     */

    private File outputFilePdf;

    /**
     * <p>When the program runs, it will produce an XML
     * output (as DOM) and then this is combined with
     * XSLT to produce the FO -> PDF.  This member
     * variable points to a file where the intermediate
     * XML can be stored in order to debug or develop
     * XSLT scripts.</p>
     */

    private File outputFileXml;

    /**
     * <p>This is a URL to the transformation XSLT script
     * which will take the XML and turn it into FO.</p>
     */

    private URL foTransformUrlXsl;


    public Job() {
        super();
        setMaximumWidthOrHeightPixels(Constants.DEFAULT_MAXIMUMWIDTHORHEIGHTPIXELS);
    }

    public File getOutputFilePdf() {
        return outputFilePdf;
    }

    public File getOutputFileXml() {
        return outputFileXml;
    }

    public URL getFoTransformUrlXsl() {
        return foTransformUrlXsl;
    }

    public void setOutputFilePdf(File value) {
        outputFilePdf = value;
    }

    public void setOutputFileXml(File value) {
        outputFileXml = value;
    }

    public void setFoTransformUrlXsl(URL value) {
        foTransformUrlXsl = value;
    }

}
