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

import android.support.annotation.NonNull;
import com.google.android.gms.common.ConnectionResult;
import io.reactivex.SingleEmitter;

abstract class PlayServicesSingleCallback<T> extends PlayServicesCallback {
    private final SingleEmitter<T> single;

    PlayServicesSingleCallback(SingleEmitter<T> single) {
        this.single = single;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        single.onError(new IllegalStateException("Connection to Google Play Services suspended"));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        single.onError(new IllegalStateException("Connection to Google Play Services failed"));
    }
}
