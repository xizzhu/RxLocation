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

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;

public final class PlayServicesLocationProvider implements RxLocation {
    final Context applicationContext;

    public PlayServicesLocationProvider(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Observable<Location> getLastLocation() {
        return Observable.fromEmitter(new Action1<Emitter<Location>>() {
            @Override
            public void call(Emitter<Location> emitter) {
                try {
                    final PlayServicesCallback callback = new PlayServicesCallback(emitter) {
                        @Override
                        public void onConnected(@Nullable Bundle connectionHint) {
                            try {
                                @SuppressWarnings("MissingPermission") Location lastLocation =
                                    LocationServices.FusedLocationApi.getLastLocation(
                                        googleApiClient);
                                if (lastLocation != null) {
                                    emitter.onNext(lastLocation);
                                }
                                emitter.onCompleted();
                            } catch (Throwable e) {
                                emitter.onError(e);
                            }
                        }
                    };

                    emitter.setCancellation(new PlayServicesCancellable(
                        buildGoogleApiClient(applicationContext, callback)));
                } catch (Throwable e) {
                    emitter.onError(e);
                }
            }
        }, Emitter.BackpressureMode.NONE);
    }

    static GoogleApiClient buildGoogleApiClient(Context context, PlayServicesCallback callback) {
        final GoogleApiClient googleApiClient =
            new GoogleApiClient.Builder(context, callback, callback).addApi(LocationServices.API)
                .build();
        callback.setGoogleApiClient(googleApiClient);
        googleApiClient.connect();
        return googleApiClient;
    }

    @NonNull
    @Override
    public Observable<Location> getLocationUpdates(
        @NonNull final LocationUpdateRequest locationUpdateRequest) {
        return Observable.fromEmitter(new Action1<Emitter<Location>>() {
            @Override
            public void call(final Emitter<Location> emitter) {
                try {
                    final LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            emitter.onNext(location);
                        }
                    };

                    final PlayServicesCallback callback = new PlayServicesCallback(emitter) {
                        @Override
                        public void onConnected(@Nullable Bundle connectionHint) {
                            try {
                                final LocationRequest locationRequest = LocationRequest.create()
                                    .setPriority(locationUpdateRequest.getPriority())
                                    .setInterval(locationUpdateRequest.getIntervalInMillis())
                                    .setFastestInterval(
                                        locationUpdateRequest.getFastestIntervalInMillis())
                                    .setMaxWaitTime(
                                        locationUpdateRequest.getMaxWaitingTimeInMillis())
                                    .setSmallestDisplacement(
                                        locationUpdateRequest.getSmallestDistanceInMeters());
                                //noinspection MissingPermission
                                LocationServices.FusedLocationApi.requestLocationUpdates(
                                    googleApiClient, locationRequest, locationListener);
                            } catch (Throwable e) {
                                emitter.onError(e);
                            }
                        }
                    };

                    emitter.setCancellation(new PlayServicesCancellable(
                        buildGoogleApiClient(applicationContext, callback)));
                } catch (Throwable e) {
                    emitter.onError(e);
                }
            }
        }, Emitter.BackpressureMode.NONE);
    }
}
