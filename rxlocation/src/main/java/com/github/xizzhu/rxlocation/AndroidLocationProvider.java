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
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;

public final class AndroidLocationProvider implements RxLocation {
    final Context applicationContext;

    public AndroidLocationProvider(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Observable<Location> getLastLocation() {
        return Observable.fromEmitter(new Action1<Emitter<Location>>() {
            @Override
            public void call(Emitter<Location> emitter) {
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
                        emitter.onNext(bestLocation);
                    }

                    emitter.onCompleted();
                } catch (Throwable e) {
                    emitter.onError(e);
                }
            }
        }, Emitter.BackpressureMode.NONE);
    }

    @NonNull
    @Override
    public Observable<Location> getSingleUpdate(@LocationUtils.Priority final int priority) {
        return Observable.fromEmitter(new Action1<Emitter<Location>>() {
            @Override
            public void call(final Emitter<Location> emitter) {
                try {
                    final LocationManager locationManager =
                        (LocationManager) applicationContext.getSystemService(
                            Context.LOCATION_SERVICE);
                    final LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            try {
                                //noinspection MissingPermission
                                locationManager.removeUpdates(this);
                            } catch (Throwable ignored) {
                            }

                            emitter.onNext(location);
                            emitter.onCompleted();
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

                    switch (priority) {
                        case LocationUtils.PRIORITY_HIGH_ACCURACY:
                            //noinspection MissingPermission
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                                locationListener, Looper.getMainLooper());
                            break;
                        case LocationUtils.PRIORITY_BALANCED_POWER_ACCURACY:
                            //noinspection MissingPermission
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                                locationListener, Looper.getMainLooper());
                            //noinspection MissingPermission
                            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                                locationListener, Looper.getMainLooper());
                            break;
                        case LocationUtils.PRIORITY_LOW_POWER:
                            //noinspection MissingPermission
                            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                                locationListener, Looper.getMainLooper());
                            break;
                        case LocationUtils.PRIORITY_NO_POWER:
                            //noinspection MissingPermission
                            locationManager.requestSingleUpdate(LocationManager.PASSIVE_PROVIDER,
                                locationListener, Looper.getMainLooper());
                            break;
                        default:
                            throw new IllegalArgumentException(
                                "Unsupported priority - " + priority);
                    }
                } catch (Throwable e) {
                    emitter.onError(e);
                }
            }
        }, Emitter.BackpressureMode.NONE);
    }
}
