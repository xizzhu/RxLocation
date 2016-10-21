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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Cancellable;

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
                    final GoogleApiClientCallback callback = new GoogleApiClientCallback(emitter);
                    final GoogleApiClient googleApiClient =
                        new GoogleApiClient.Builder(applicationContext, callback, callback).addApi(
                            LocationServices.API).build();
                    callback.setGoogleApiClient(googleApiClient);
                    googleApiClient.connect();

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

    private static class GoogleApiClientCallback
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        private final Emitter<Location> emitter;
        private GoogleApiClient googleApiClient;

        GoogleApiClientCallback(Emitter<Location> emitter) {
            this.emitter = emitter;
        }

        void setGoogleApiClient(GoogleApiClient googleApiClient) {
            this.googleApiClient = googleApiClient;
        }

        @Override
        public void onConnected(@Nullable Bundle connectionHint) {
            try {
                @SuppressWarnings("MissingPermission") Location lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (lastLocation != null) {
                    emitter.onNext(lastLocation);
                }
                emitter.onCompleted();
            } catch (Throwable e) {
                emitter.onError(e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            emitter.onError(
                new IllegalStateException("Connection to Google Play Services suspended"));
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult result) {
            emitter.onError(new IllegalStateException("Connection to Google Play Services failed"));
        }
    }
}
