/*
 * Copyright 2016, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat;

/**
 * <p>This interface is invoked as the engine runs, allowing
 * a user interface to provide some feedback.</p>
 * @author apl
 */

public interface ProgressIndicatorInterface {

    void updateProgress();

    void handleFailure();

    void handleCompletion();

}
