/*
 * Copyright 2016-2023, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>This class provides helpers for working with JPEG images.</p>
 *
 * @author apl
 */

public class JpegHelper {

    protected static Logger LOGGER = LoggerFactory.getLogger(JpegHelper.class);

    // convert from moto endian to intel endian

    private static int read16(InputStream is) throws IOException {
        int a = is.read();
        if (-1 == a) return -1;
        int b = is.read();
        if (-1 == b) throw new IOException("unexpected end of file when reading a 16 bit number.");
        return (a << 8) | b;
    }

    private static void write16(int value, OutputStream os) throws IOException {
        int a = (value >> 8) & 0xFF;
        int b = value & 0xFF;
        os.write(a);
        os.write(b);
    }

    /**
     * <p>Returns true if it has reached the end of the JPEG data.</p>
     */

    private static boolean passThroughJpegImageSegmentWithUnnecessarySegmentsStripped(
            int segmentMarker,
            InputStream is,
            OutputStream os,
            byte[] passThroughBuffer) throws IOException {

        Preconditions.checkNotNull(os);
        Preconditions.checkNotNull(is);

        if ((segmentMarker & 0xFF00) != 0xFF00)
            throw new IOException("bad segment marker; all segment markers should start with 0xFF");

        boolean writeSegment = switch (segmentMarker) {
            case 0xFFE0, 0xFFE1, 0xFFE2, 0xFFE3, 0xFFE4, 0xFFE5, 0xFFE6, 0xFFE7, 0xFFE8, 0xFFE9 -> false;
            default -> true;
        };

        if (writeSegment) {
            write16(segmentMarker, os);
        }

        if (0xFFD9 == segmentMarker) /* EOI - End of Image */ {
            return true;
        }

        int len = read16(is);

        if (writeSegment) {
            write16(len, os);
        }

        if (0xFFDA != segmentMarker) {
            // transcribe the data over into the output.
            // was using InputStream.skip(..) here for the
            // case of not writing, but this seemed to cause
            // some problems with the buffered input stream.

            int yetToRead = len - 2;

            while (yetToRead > 0) {
                int toRead = yetToRead;
                if (toRead > passThroughBuffer.length) toRead = passThroughBuffer.length;
                int wasRead = is.read(passThroughBuffer, 0, toRead);

                if (-1 == wasRead)
                    throw new IOException("unexpected end of stream reached while reading JPEG data from segment; " + Integer.toString(segmentMarker, 16));

                if (writeSegment) {
                    os.write(passThroughBuffer, 0, wasRead);
                }

                yetToRead -= wasRead;
            }
        } else {
            boolean exhaustedSosSegment = false;

            // the SOS marker indicates actual data and the only way
            // to cope with it is to quasi parse it.

            while (!exhaustedSosSegment) {
                int c = is.read();

                if (-1 == c) {
                    throw new IOException("premature end of stream when reading a JPEG SOS segment.");
                }

                if (writeSegment) {
                    os.write(c);
                }

                if (0xFF == c) {
                    c = is.read();

                    if (-1 == c) {
                        throw new IOException("premature end of stream when reading a JPEG SOS segment.");
                    }

                    if (writeSegment) {
                        os.write(c);
                    }

                    if (0x00 != c) {
                        if ((c < 0xD0) || (c > 0xD7)) {
                            exhaustedSosSegment = true;
                            int nextSegmentMarker = (0xFF << 8) | c;

                            if(LOGGER.isTraceEnabled()) {
                                LOGGER.trace("processing next segment marker; {}", Integer.toString(nextSegmentMarker, 16));
                            }

                            if(passThroughJpegImageSegmentWithUnnecessarySegmentsStripped(nextSegmentMarker, is, os, passThroughBuffer)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * <p>Basically this method will take the data on the input stream, interpret
     * it as JPEG and just keep those segments which are actually really necessary
     * for the JPEG image.  This means no EXIF data.  This is used because the
     * EXIF data seems to be able to stop the JPEG image from loading in the J2SE
     * environment.</p>
     */

    static void passThroughJpegImageWithUnnecessarySegmentsStripped(
            InputStream inputStream,
            OutputStream outputStream) throws IOException {

        Preconditions.checkArgument(null!=inputStream, "the jpeg input stream must be provided");
        Preconditions.checkArgument(null!=outputStream, "the jpeg output stream must be provided");

        byte[] passThroughBuffer = new byte[32 * 1024];

        if (0xFFD8 != read16(inputStream)) {
            throw new IOException("missing SOI marker");
        }

//		System.out.println("segment; "+Integer.toString(0xFFD8,16));

        write16(0xFFD8, outputStream);

        int segmentMarker;

        while (-1 != (segmentMarker = read16(inputStream))) {
            if(LOGGER.isTraceEnabled()) {
                LOGGER.trace("processing segment marker; {}", Integer.toString(segmentMarker, 16));
            }

            if(passThroughJpegImageSegmentWithUnnecessarySegmentsStripped(segmentMarker, inputStream, outputStream, passThroughBuffer)) {
                return; // if we reached the end, some images may be padded so bail early.
            }
        }
    }

}
