/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.application;

import java.util.concurrent.Future;

import app.packed.container.Assembly;
import app.packed.container.Wirelet;

/**
 *
 */
// Problemet med JobApp er jo at vi i virkeligheden gerne vil have

// JobApp og JobDaemonApp<T> app

// Hvor man i JobDaemon kan cancel jobbet

public interface JobApp<T> {

    Future<T> asFuture();

    static <T> JobApp.Image<T> imageOf(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static <T> T run(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    interface Image<T> {

        JobApp<T> start();

        /**
         * @return
         */
        T run();

    }
}

//
//static <T> Result<T> compute(Class<T> resultType, Assembly assembly, Wirelet... wirelets) {
//  throw new UnsupportedOperationException();
//}

//static <T> Future<T> runAsync(Class<?> resultType, Assembly assembly, Wirelet... wirelets) {
//    throw new UnsupportedOperationException();
//}
//
