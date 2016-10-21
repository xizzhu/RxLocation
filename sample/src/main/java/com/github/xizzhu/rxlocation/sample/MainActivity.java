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
import com.github.xizzhu.rxlocation.LocationUtils;
import com.github.xizzhu.rxlocation.RxLocation;
import java.util.concurrent.TimeUnit;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RxLocation rxLocation = new AndroidLocationProvider(this);
        rxLocation.getLastLocation()
            .switchIfEmpty(
                rxLocation.getSingleUpdate(LocationUtils.PRIORITY_BALANCED_POWER_ACCURACY))
            .timeout(5L, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<Location>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "getLastLocation.onCompleted()");
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onNext(Location location) {
                    Log.d(TAG, "getLastLocation.onNext(): " + location);
                }
            });
    }
}
