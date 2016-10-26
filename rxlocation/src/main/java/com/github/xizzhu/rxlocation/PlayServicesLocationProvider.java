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
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Cancellable;

public final class PlayServicesLocationProvider implements RxLocationProvider {
    final Context applicationContext;

    public PlayServicesLocationProvider(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Single<Location> getLastLocation() {
        return Single.create(new Single.OnSubscribe<Location>() {
            @Override
            public void call(final SingleSubscriber<? super Location> singleSubscriber) {
                try {
                    final PlayServicesCallback callback =
                        new PlayServicesSingleCallback<Location>(singleSubscriber) {
                            @Override
                            public void onConnected(@Nullable Bundle connectionHint) {
                                try {
                                    @SuppressWarnings("MissingPermission") Location lastLocation =
                                        LocationServices.FusedLocationApi.getLastLocation(
                                            googleApiClient);
                                    if (lastLocation != null) {
                                        singleSubscriber.onSuccess(lastLocation);
                                    } else {
                                        singleSubscriber.onError(new IllegalStateException(
                                            "No last location available"));
                                    }
                                } catch (Throwable e) {
                                    singleSubscriber.onError(e);
                                }
                            }
                        };

                    final GoogleApiClient googleApiClient =
                        buildGoogleApiClient(applicationContext, callback);
                    singleSubscriber.add(new Subscription() {
                        @Override
                        public void unsubscribe() {
                            if (googleApiClient.isConnected() || googleApiClient.isConnecting()) {
                                googleApiClient.disconnect();
                            }
                        }

                        @Override
                        public boolean isUnsubscribed() {
                            return false;
                        }
                    });
                } catch (Throwable e) {
                    singleSubscriber.onError(e);
                }
            }
        });
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

                    final PlayServicesCallback callback =
                        new PlayServicesEmitterCallback<Location>(emitter) {
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

                    final GoogleApiClient googleApiClient =
                        buildGoogleApiClient(applicationContext, callback);
                    emitter.setCancellation(new Cancellable() {
                        @Override
                        public void cancel() throws Exception {
                            if (googleApiClient.isConnected() || googleApiClient.isConnecting()) {
                                googleApiClient.disconnect();
                            }
                        }
                    });
                } catch (Throwable e) {
                    emitter.onError(e);
                }
            }
        }, Emitter.BackpressureMode.NONE);
    }
}
