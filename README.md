RxLocation
==========

Wraps Android location code with RxJava, supporting both Play Services and platform location provider.

The current version works with [RxJava](https://github.com/ReactiveX/RxJava) 1.2.1 (or higher). If client wants to use the location services from [Play Services](https://developers.google.com/android/guides/overview), version 9.6.1 (or higher) is needed.

How to Use
----------

### Create Location Provider

````java
// creates one using Play Services LocationServices
RxLocationProvider playServicesLocationProvider = new PlayServicesLocationProvider(context);

// creates one using Android platform LocationManager
RxLocationProvider androidLocationProvider = new AndroidLocationProvider(context);
````

### Get Last Known Location

````java
Observable<Location> lastKnownLocationObservable = rxLocationProvider.getLastLocation();
````

### Get Location Updates

````java
// this is a direct mapping to Play Services LocationRequest
LocationUpdateRequest locationUpdateRequest = new LocationUpdateRequest.Builder()
    .priority(LocationUpdateRequest.PRIORITY_HIGH_ACCURACY)
    .intervalInMillis(5000L)
    .fastestIntervalInMillis(1000L) // ignored by AndroidLocationProvider
    .maxWaitingTimeInMillis(15000L) // ignored by AndroidLocationProvider
    .smallestDistanceInMeters(10.0F)
    .build();
Observable<Location> locationUpdatesObservable
    = rxLocationProvider.getLocationUpdates(locationUpdateRequest);
````

License
-------
    Copyright (C) 2016 Xizhi Zhu

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
