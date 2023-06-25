/*
 * Copyright 2016-2023, Andrew Lindesay. All Rights Reserved.
 * Distributed under the terms of the MIT License.
 *
 * Authors:
 *		Andrew Lindesay, apl@lindesay.co.nz
 */

package nz.co.silvereye.photocat;

import com.google.common.base.Preconditions;

/**
 * <p>This class is designed to keep track of the progress happening
 * in the job engine.  It breaks up the progress into phases and can
 * then be allocated a percentage.  Once a phase is complete it can
 * be moved over into the new phase.</p>
 * @author apl
 */

public class PhasedProgress {

    private int phase = 0;
    private int percentageInPhase = 0;
    private final int[] weights;

    public PhasedProgress(int[] weights) {
        Preconditions.checkArgument(null!=weights, "weights must be provided");
        Preconditions.checkArgument(0!=weights.length, "at least one weight must be supplied");
        this.weights = weights;
    }

    public void nextPhase() {
        phase++;
        percentageInPhase = 0;
    }

    public void setPercentageInPhase(int value) {
        percentageInPhase = value;
        if (percentageInPhase < 0) {
            percentageInPhase = 0;
        }
        if (percentageInPhase > 100) {
            percentageInPhase = 100;
        }
    }

    public int absolutePercentage() {
        int total = 0;
        int maximum = 0;

        for (int i = 0; i < weights.length; i++) {
            maximum += weights[i] * 100;

            if (phase == i) {
                total += weights[i] * percentageInPhase;
            }
            else {
                if (phase > i) {
                    total += weights[i] * 100;
                }
            }
        }

        total *= 100;
        total /= maximum;

        return total;
    }

    public String toString() {
        return "phase:" + phase + " absoluteperc:" + absolutePercentage();
    }

}
