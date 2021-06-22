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
package app.packed.job;

import app.packed.application.ApplicationDriver;
import app.packed.base.TypeToken;
import app.packed.component.Assembly;

/**
 *
 */
// Er det en speciel shell?? Ja det ville jeg mene

// Altsaa det giver sjaeldent mening at lave den direkte 
//Is not a system namespace by default..
//What about shutdown... I don't think it is active...
public interface Job<R> {

    R get();
    
    static <R> ApplicationDriver<Job<?>> driver() {
        throw new UnsupportedOperationException();
    }

    static <R> ApplicationDriver<Job<R>> driver(TypeToken<R> resultType) {
        throw new UnsupportedOperationException();
    }

    static <R> ApplicationDriver<Job<R>> driver(Class<R> resultType) {
        // Returnere vi en nye
        throw new UnsupportedOperationException();
    }

    static void compute(Assembly<?> assembly) {
        throw new UnsupportedOperationException();
    }

    static <S, T extends Assembly<?> & ResultBearing<S>> Job<S> start(T assembly) {
        throw new UnsupportedOperationException();
    }

    // Must have a compute function...
    static <S, T extends Assembly<?> & ResultBearing<S>> Job<S> run(T assembly) {
        throw new UnsupportedOperationException();
    }


    // Must have a compute function...
    static <T> Job<T> run(JobAssembly<T> assembly) {
        throw new UnsupportedOperationException();
    }
    // Job<Void> of()
}

// A job consists of 1 or more tasks ordered in a tree...
// But why not use the Component API...
// Basically it is the job-class that is the root 
interface Task {

}

// IDK er det det vi returnere...
interface Result<R> {

}