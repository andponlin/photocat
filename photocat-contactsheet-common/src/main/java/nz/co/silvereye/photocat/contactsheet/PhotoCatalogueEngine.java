/*
 * Copyright 2016-2023, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat.contactsheet;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import nz.co.silvereye.photocat.*;
import org.apache.fop.apps.*;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * <p>This class is able to 'execute' a job.  This means;</p>
 * <ul>
 * <li>Checking the job</li>
 * <li>Producing the thumbnails</li>
 * <li>Generating an XML file output for the job -- and optionally saving it</li>
 * <li>Run the XML through XSLT to PDF (via FOP) to produce PDF output.</li>
 * <li>Clean up extraneous files</li>
 * </ul>
 *
 * @author apl
 */

class PhotoCatalogueEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoCatalogueEngine.class);

    private final DateFormat timestampFormat = new SimpleDateFormat(nz.co.silvereye.photocat.Constants.SIMPLEDATEFORMAT_SQL92_DATETIME);

    public PhotoCatalogueEngine() {
        super();
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

            if (rb.containsKey(key)) {
                return rb.getString(key);
            }
        }

        {
            ResourceBundle rb = ResourceBundle.getBundle(
                    Constants.RSRCBUNDLEPATH_LOCALIZATIONS,
                    Locale.getDefault());

            if (rb.containsKey(key)) {
                return rb.getString(key);
            }
        }
        return key;
    }

    // ----------------------------------------------
    // PROCESSING A JOB
    // ----------------------------------------------

    public synchronized void run(
            Job job,
            ProgressIndicatorInterface progressIndicator) {

        Preconditions.checkNotNull(job);

        PhasedProgress phasedProgress = new PhasedProgress(new int[]{
                5, // checking
                45, // image processing
                5, // generating DOM
                5, // output to XML file
                40, // render PDF
        });

        // [apl]
        // The two layers of try...finally here are so that a clean-up phase can
        // be assured, but if there are problems in the clean-up phase then the
        // completion phase will still be executed.

        try {
            {
                job.setPercentage(phasedProgress.absolutePercentage());
                job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSCHECKING));

                if (null != progressIndicator) {
                    progressIndicator.updateProgress();
                }

                if (null == job.getFoTransformUrlXsl()) {
                    throw new Exception("the FO transform has not been configured.");
                }

                if (job.getSourceFiles().isEmpty()) {
                    throw new Exception("there are no files supplied to render the page for.");
                }
            }

            phasedProgress.nextPhase();

            BlockingQueue<Runnable> jobSourceFilePreparationQueue = null;
            ThreadPoolExecutor jobSourceFilePreparationExecutor = null;

            try {
                if (job.isRunning()) {
                    job.setPercentage(phasedProgress.absolutePercentage());
                    job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSIMAGEPROCESSING));

                    if (null != progressIndicator) {
                        progressIndicator.updateProgress();
                    }

                    jobSourceFilePreparationQueue = new ArrayBlockingQueue<>(job.getSourceFiles().size());

                    jobSourceFilePreparationExecutor = new ThreadPoolExecutor(
                            0,
                            job.getThreadCount(),
                            5,
                            TimeUnit.SECONDS,
                            jobSourceFilePreparationQueue,
                            Executors.defaultThreadFactory(),
                            new ThreadPoolExecutor.CallerRunsPolicy());

                    // the first step is to generate all of the necessary thumb-nails

                    LOGGER.info("thumbnailing the {} files",job.getSourceFiles().size());

                    {
                        List<Future<?>> futures = new ArrayList<>();
                        int countDone = 0;

                        for (JobSourceFile jsf : job.getSourceFiles()) {
                            futures.add(jobSourceFilePreparationExecutor.submit(new JobSourceFilePreparation(jsf)));
                        }

                        Iterator<Future<?>> futuresI = futures.iterator();

                        while (
                                !job.isCancelled() &&
                                        futuresI.hasNext()) {
                            Future<?> future = futuresI.next();
                            boolean done = false;

                            while (!done && job.isRunning()) {
                                try {
                                    future.get();
                                    done = true;
                                } catch (InterruptedException ie) { /* ignore */ } catch (ExecutionException ee) // something went wrong when processing the image
                                {
                                    LOGGER.error("a problem has arisen in preparing an image", ee);
                                }
                            }

                            countDone++;
                            phasedProgress.setPercentageInPhase((countDone * 100) / futures.size());

                            job.setPercentage(phasedProgress.absolutePercentage());
                            job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSIMAGEPROCESSING));

                            if (null != progressIndicator)
                                progressIndicator.updateProgress();

                        }

                        // If the user did cancel then the rest of the processing can just be
                        // aborted.

                        if (job.isCancelled()) {
                            jobSourceFilePreparationExecutor.shutdownNow();
                            LOGGER.info("have shutdown the executor that is processing image and video data as the user has opted to cancel.");
                        }
                    }
                }

                phasedProgress.nextPhase();

                // all of the images should have been thumbnailed now.
                // now assemble a DOM object with all of the images in it.

                Element d = null;

                if (job.isRunning()) {
                    LOGGER.info("assembling the job to DOM");

                    job.setPercentage(phasedProgress.absolutePercentage());
                    job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSGENERATINGXML));

                    if (null != progressIndicator) {
                        progressIndicator.updateProgress();
                    }

                    d = assembleDocument(job);
                }

                phasedProgress.nextPhase();

                // render the DOM into a file if required.

                if (null != job.getOutputFileXml() && job.isRunning()) {
                    job.setPercentage(phasedProgress.absolutePercentage());
                    job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSWRITINGXML));

                    if (null != progressIndicator) {
                        progressIndicator.updateProgress();
                    }

                    LOGGER.info("exporting the DOM to a file");

                    FileOutputStream os = null;

                    try {
                        os = new FileOutputStream(job.getOutputFileXml());
                        XMLOutputter serializer = new XMLOutputter();
                        serializer.output(d, os);
                    } catch (IOException e) {
                        throw new Exception("unable to write XML to the file '" + job.getOutputFileXml() + "'", e);
                    } finally {
                        Closeables.closeQuietly(os);
                    }
                }

                phasedProgress.nextPhase();

                // render the DOM (via XSLT) into a PDF if required.

                if (null != job.getOutputFilePdf() && job.isRunning()) {
                    job.setPercentage(phasedProgress.absolutePercentage());
                    job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSGENERATINGPDF));

                    if (null != progressIndicator) {
                        progressIndicator.updateProgress();
                    }

                    LOGGER.info("rendering; DOM --> XML --[XSL]--> FO --> PDF");

                    FileOutputStream os = new FileOutputStream(job.getOutputFilePdf());

                    InputStream transformXslIs = null;
                    FopFactory fopFactory = new FopFactoryBuilder(new URI(".")).build();
                    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

                    try {
                        transformXslIs = job.getFoTransformUrlXsl().openStream();
                        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);

                        // Setup XSLT
                        TransformerFactory factory = TransformerFactory.newInstance();
                        Transformer transformer = factory.newTransformer(new StreamSource(transformXslIs));

                        // Setup input for XSLT transformation
                        Source src = new JDOMSource(d);

                        // Resulting SAX events (the generated FO) must be piped through to FOP
                        Result res = new SAXResult(fop.getDefaultHandler());

                        // Start XSLT transformation and FOP processing
                        transformer.transform(src, res);
                    } catch (Exception e) {
                        throw new Error("an error has arisen converting the XML data from the files through FOP into PDF.", e);
                    } finally {
                        Closeables.closeQuietly(transformXslIs);
                    }

                    os.close();
                }
            } catch (Throwable th) {
                LOGGER.error("a failure has arisen processing the job", th);

                job.setFailed();
                job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSERROR));

                if (null != progressIndicator)
                    progressIndicator.handleFailure();
            } finally {
                job.setPercentage(100);
                job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSCLEANUP));

                if (null != progressIndicator)
                    progressIndicator.updateProgress();

                // we need to clean-up afterwards and delete any temporary files which were
                // created as a part of generating the output.

                for (JobSourceFile jsf : job.getSourceFiles()) {
                    if (null != jsf.getThumbnailFile())
                        if (!jsf.getThumbnailFile().delete()) {
                            LOGGER.error("unable to delete file [{}]", jsf.getThumbnailFile());
                        }
                }

                // shutdown the executor.

                if (null != jobSourceFilePreparationExecutor) {
                    jobSourceFilePreparationExecutor.shutdown();
                    jobSourceFilePreparationExecutor = null;
                }

                if (null != jobSourceFilePreparationQueue)
                    jobSourceFilePreparationQueue = null;
            }
        } catch (Throwable th) // catch all for the engine.
        {
            job.setFailed();

            LOGGER.error("unable to process job", th);

            if (null != progressIndicator)
                progressIndicator.handleFailure();
        } finally // finalize the progress
        {
            LOGGER.info("completed processing.");

            job.setCompleted();
            job.setMessage(getLocalizedStringForKey(Constants.KEY_L_STATUSFINISHED));
            job.setPercentage(100);

            if (null != progressIndicator)
                progressIndicator.handleCompletion();
        }
    }

    // ----------------------------------------------
    // DOM-HANDLING
    // ----------------------------------------------

    private Element assembleDocument(Job job) {
        SimpleDateFormat timestampF = new SimpleDateFormat(nz.co.silvereye.photocat.Constants.SIMPLEDATEFORMAT_SQL92_DATETIME);
        List<JobSourceFile> jsfs = new ArrayList<JobSourceFile>(job.getSourceFiles());
        Collections.sort(jsfs);

        java.util.Date earliestD = jsfs.get(0).getTimestamp();
        java.util.Date latestD = jsfs.get(jsfs.size() - 1).getTimestamp();

        Element topE = new Element("photocat");
        topE.setAttribute("starttimestamp", timestampF.format(earliestD));
        topE.setAttribute("stoptimestamp", timestampF.format(latestD));

        // now assemble the results by month.

        GregorianCalendar gc = new GregorianCalendar();
        List<JobSourceFile> monthJsfs = new ArrayList<>();
        int month = -1;
        int year = -1;

        for (JobSourceFile jsf : jsfs) {
            gc.setTimeInMillis(jsf.getTimestamp().getTime());
            int jsfMonth = gc.get(Calendar.MONTH);
            int jsfYear = gc.get(Calendar.YEAR);

            if (monthJsfs.isEmpty())
                monthJsfs.add(jsf);
            else {
                if ((jsfMonth != month) || (jsfYear != year)) {
                    attachElementForMonth(topE, monthJsfs, year, month);
                    monthJsfs.clear();
                }

                monthJsfs.add(jsf);
            }

            month = jsfMonth;
            year = jsfYear;
        }

        if (!monthJsfs.isEmpty())
            attachElementForMonth(topE, monthJsfs, year, month);

        return topE;
    }

    private void attachElementForMonth(
            Element context,
            List<JobSourceFile> monthJsfs,
            int year,
            int month) {
        Element sectionE = new Element("section");

        sectionE.setAttribute("label", Integer.toString(year) + "-" + Integer.toString(month + 1));
        sectionE.setAttribute("code", UUID.randomUUID().toString());

        for (JobSourceFile jsf : monthJsfs) {
            Element fileContainerE = new Element("file");
            Element timestampE = new Element("timestamp");
            Element fileE = new Element("file");
            Element nameE = new Element("name");
            Element datatypeE = new Element("datatype");

            timestampE.addContent(timestampFormat.format(jsf.getTimestamp()));
            fileE.addContent(jsf.getFile().getAbsolutePath());
            nameE.addContent(jsf.getName());
            datatypeE.addContent(jsf.getDataType().name());

            fileContainerE.addContent(timestampE);
            fileContainerE.addContent(fileE);
            fileContainerE.addContent(nameE);
            fileContainerE.addContent(datatypeE);

            if(!Strings.isNullOrEmpty(jsf.getDescription())) {
                Element descriptionE = new Element("description");
                descriptionE.addContent(jsf.getDescription());
                fileContainerE.addContent(descriptionE);
            }

            if (null != jsf.getThumbnailFile()) {
                Element thumbnailE = new Element("thumbnailurl");

                try {
                    thumbnailE.addContent(jsf.getThumbnailFile().toURI().toURL().toExternalForm());
                } catch (MalformedURLException mue) {
                    throw new IllegalStateException("an error has occurred converting a java file into a URL", mue);
                }

                fileContainerE.addContent(thumbnailE);
            }

            sectionE.addContent(fileContainerE);
        }

        context.addContent(sectionE);

    }

}
