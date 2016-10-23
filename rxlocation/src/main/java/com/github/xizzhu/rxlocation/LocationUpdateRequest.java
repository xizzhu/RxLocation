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

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import com.google.android.gms.location.LocationRequest;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LocationUpdateRequest {
    public static final int PRIORITY_HIGH_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    public static final int PRIORITY_BALANCED_POWER_ACCURACY =
        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final int PRIORITY_LOW_POWER = LocationRequest.PRIORITY_LOW_POWER;
    public static final int PRIORITY_NO_POWER = LocationRequest.PRIORITY_NO_POWER;

    @IntDef({
        PRIORITY_HIGH_ACCURACY, PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER,
        PRIORITY_NO_POWER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Priority {
    }

    @LocationUpdateRequest.Priority
    private final int priority;
    private final long intervalInMillis;
    private final long fastestIntervalInMillis;
    private final long maxWaitingTimeInMillis;
    private final float smallestDistanceInMeters;

    LocationUpdateRequest(int priority, long intervalInMillis, long fastestIntervalInMillis,
        long maxWaitingTimeInMillis, float smallestDistanceInMeters) {
        this.priority = priority;
        this.intervalInMillis = intervalInMillis;
        this.fastestIntervalInMillis = fastestIntervalInMillis;
        this.maxWaitingTimeInMillis = maxWaitingTimeInMillis;
        this.smallestDistanceInMeters = smallestDistanceInMeters;
    }

    public int getPriority() {
        return priority;
    }

    public long getIntervalInMillis() {
        return intervalInMillis;
    }

    public long getFastestIntervalInMillis() {
        return fastestIntervalInMillis;
    }

    public long getMaxWaitingTimeInMillis() {
        return maxWaitingTimeInMillis;
    }

    public float getSmallestDistanceInMeters() {
        return smallestDistanceInMeters;
    }

    @NonNull
    public Builder toBuilder() {
        return new Builder().priority(priority)
            .intervalInMillis(intervalInMillis)
            .fastestIntervalInMillis(fastestIntervalInMillis)
            .maxWaitingTimeInMillis(maxWaitingTimeInMillis)
            .smallestDistanceInMeters(smallestDistanceInMeters);
    }

    public static class Builder {
        @LocationUpdateRequest.Priority
        private int priority = PRIORITY_BALANCED_POWER_ACCURACY;
        private long intervalInMillis;
        private long fastestIntervalInMillis;
        private long maxWaitingTimeInMillis;
        private float smallestDistanceInMeters;

        public Builder() {
        }

        @NonNull
        public Builder priority(@LocationUpdateRequest.Priority int priority) {
            this.priority = priority;
            return this;
        }

        @NonNull
        public Builder intervalInMillis(long intervalInMillis) {
            this.intervalInMillis = intervalInMillis;
            return this;
        }

        @NonNull
        public Builder fastestIntervalInMillis(long fastestIntervalInMillis) {
            this.fastestIntervalInMillis = fastestIntervalInMillis;
            return this;
        }

        @NonNull
        public Builder maxWaitingTimeInMillis(long maxWaitingTimeInMillis) {
            this.maxWaitingTimeInMillis = maxWaitingTimeInMillis;
            return this;
        }

        @NonNull
        public Builder smallestDistanceInMeters(float smallestDistanceInMeters) {
            this.smallestDistanceInMeters = smallestDistanceInMeters;
            return this;
        }

        @NonNull
        public LocationUpdateRequest build() {
            return new LocationUpdateRequest(priority, intervalInMillis, fastestIntervalInMillis,
                maxWaitingTimeInMillis, smallestDistanceInMeters);
        }
    }
}
