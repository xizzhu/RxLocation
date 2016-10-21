/*
 * Copyright (C) 2016 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.xizzhu.rxlocation;

import android.location.Location;
import android.os.Build;

abstract class LocationUtils {
    private static final long FRESHNESS_THRESHOLD_IN_NANOSECONDS = 30000000L; // 30 seconds

    /**
     * @return true if location A is better than location B.
     */
    static boolean isBetterThan(Location locationA, Location locationB) {
        if (locationA == null) {
            return false;
        }
        if (locationB == null) {
            return true;
        }

        final long timeDiffInNanoseconds;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            timeDiffInNanoseconds =
                locationA.getElapsedRealtimeNanos() - locationB.getElapsedRealtimeNanos();
        } else {
            timeDiffInNanoseconds = (locationA.getTime() - locationB.getTime()) * 1000L;
        }
        if (timeDiffInNanoseconds > FRESHNESS_THRESHOLD_IN_NANOSECONDS) {
            return true;
        }

        if (!locationA.hasAccuracy()) {
            return false;
        }
        if (!locationB.hasAccuracy()) {
            return true;
        }
        return locationA.getAccuracy() < locationB.getAccuracy();
    }
}
