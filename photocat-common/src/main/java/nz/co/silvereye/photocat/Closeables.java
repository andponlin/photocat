/*
 * Copyright 2016, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat;

import java.io.Closeable;
import java.io.IOException;

public class Closeables {

    /**
     * <p>This will close a resource quietly; and without logging.</p>
     */

    public static void closeQuietly(Closeable closable) {
        if(null!=closable) {
            try {
                closable.close();
            }
            catch(IOException ignore) {
            }
        }

    }

}
