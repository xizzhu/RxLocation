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
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

public final class PlayServicesLocationProvider implements RxLocationProvider {
    final Context applicationContext;

    public PlayServicesLocationProvider(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Single<Location> getLastLocation() {
        return Single.create(new SingleOnSubscribe<Location>() {
            @Override
            public void subscribe(final SingleEmitter<Location> emitter) throws Exception {
                try {
                    final PlayServicesCallback callback =
                        new PlayServicesSingleCallback<Location>(emitter) {
                            @Override
                            public void onConnected(@Nullable Bundle connectionHint) {
                                try {
                                    @SuppressWarnings("MissingPermission") Location lastLocation =
                                        LocationServices.FusedLocationApi.getLastLocation(
                                            googleApiClient);
                                    if (lastLocation != null) {
                                        emitter.onSuccess(lastLocation);
                                    } else {
                                        emitter.onError(new IllegalStateException(
                                            "No last location available"));
                                    }
                                } catch (Throwable e) {
                                    emitter.onError(e);
                                }
                            }
                        };

                    final GoogleApiClient googleApiClient =
                        buildGoogleApiClient(applicationContext, callback);
                    emitter.setDisposable(new Disposable() {
                        @Override
                        public void dispose() {
                            if (googleApiClient.isConnected() || googleApiClient.isConnecting()) {
                                googleApiClient.disconnect();
                            }
                        }

                        @Override
                        public boolean isDisposed() {
                            return false;
                        }
                    });
                } catch (Throwable e) {
                    emitter.onError(e);
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
        return Observable.create(new ObservableOnSubscribe<Location>() {
            @Override
            public void subscribe(final ObservableEmitter<Location> emitter) throws Exception {
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
                    emitter.setDisposable(new Disposable() {
                        @Override
                        public void dispose() {
                            if (googleApiClient.isConnected() || googleApiClient.isConnecting()) {
                                googleApiClient.disconnect();
                            }
                        }

                        @Override
                        public boolean isDisposed() {
                            return false;
                        }
                    });
                } catch (Throwable e) {
                    emitter.onError(e);
                }
            }
        });
    }
}
