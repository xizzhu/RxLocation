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

package com.github.xizzhu.rxlocation.sample;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.github.xizzhu.rxlocation.AndroidLocationProvider;
import com.github.xizzhu.rxlocation.LocationUpdateRequest;
import com.github.xizzhu.rxlocation.RxLocationProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RxLocationProvider rxLocationProvider = new AndroidLocationProvider(this);
        disposable = rxLocationProvider.getLastLocation()
            .onErrorResumeNext(rxLocationProvider.getLocationUpdates(
                new LocationUpdateRequest.Builder().priority(
                    LocationUpdateRequest.PRIORITY_HIGH_ACCURACY).build()).firstOrError())
            .timeout(5L, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.d(TAG, "getLastLocation.onSuccess(): " + location);
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "getLastLocation.onError()", e);
                }
            });
    }

    @Override
    protected void onDestroy() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        super.onDestroy();
    }
}
