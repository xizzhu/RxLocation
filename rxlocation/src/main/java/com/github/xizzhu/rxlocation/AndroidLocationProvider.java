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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

public final class AndroidLocationProvider implements RxLocationProvider {
    final Context applicationContext;

    public AndroidLocationProvider(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Single<Location> getLastLocation() {
        return Single.create(new SingleOnSubscribe<Location>() {
            @Override
            public void subscribe(SingleEmitter<Location> emitter) throws Exception {
                try {
                    final LocationManager locationManager =
                        (LocationManager) applicationContext.getSystemService(
                            Context.LOCATION_SERVICE);
                    Location bestLocation = null;
                    for (String provider : locationManager.getAllProviders()) {
                        //noinspection MissingPermission
                        final Location location = locationManager.getLastKnownLocation(provider);
                        if (LocationUtils.isBetterThan(location, bestLocation)) {
                            bestLocation = location;
                        }
                    }
                    if (bestLocation != null) {
                        emitter.onSuccess(bestLocation);
                    } else {
                        emitter.onError(new IllegalStateException("No last location available"));
                    }
                } catch (Throwable e) {
                    emitter.onError(e);
                }
            }
        });
    }

    @NonNull
    @Override
    public Observable<Location> getLocationUpdates(
        @NonNull final LocationUpdateRequest locationUpdateRequest) {
        return Observable.create(new ObservableOnSubscribe<Location>() {
            @Override
            public void subscribe(final ObservableEmitter<Location> emitter) throws Exception {
                try {
                    final LocationManager locationManager =
                        (LocationManager) applicationContext.getSystemService(
                            Context.LOCATION_SERVICE);
                    final LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            emitter.onNext(location);
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            // do nothing
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            // do nothing
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            // do nothing
                        }
                    };

                    final long minTime = locationUpdateRequest.getIntervalInMillis();
                    final float minDistance = locationUpdateRequest.getSmallestDistanceInMeters();
                    switch (locationUpdateRequest.getPriority()) {
                        case LocationUpdateRequest.PRIORITY_HIGH_ACCURACY:
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                minTime, minDistance, locationListener, Looper.getMainLooper());
                            break;
                        case LocationUpdateRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                minTime, minDistance, locationListener, Looper.getMainLooper());
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                minTime, minDistance, locationListener, Looper.getMainLooper());
                            break;
                        case LocationUpdateRequest.PRIORITY_LOW_POWER:
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                minTime, minDistance, locationListener, Looper.getMainLooper());
                            break;
                        case LocationUpdateRequest.PRIORITY_NO_POWER:
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                                minTime, minDistance, locationListener, Looper.getMainLooper());
                            break;
                        default:
                            throw new IllegalArgumentException(
                                "Unsupported priority - " + locationUpdateRequest.getPriority());
                    }

                    emitter.setDisposable(new Disposable() {
                        @Override
                        public void dispose() {
                            //noinspection MissingPermission
                            locationManager.removeUpdates(locationListener);
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
