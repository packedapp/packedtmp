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
package app.packed.component;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;

/**
 *
 */
// Vi har ikke null artifacts mere...
// som minimum returnere 
//skal ikke returnere null fra de der void artifacts....
// Cool saa kan vi ogsaa returnere fx. Error instead of 
// throwing an exception!!

// Maaske har vi ogsaa noget exit code??
// Eller er det en anden classe???
// Altsaa ved ikke om det ville vaere rart at kunne se hvilke skridt det
// gik galt i??? build, initialization, execution?
public final class Completion {

    private static final Completion SUCCESS = new Completion(null);

    /** If non-null, the failure; if null, indicates success. */
    @Nullable
    private final Throwable throwable;

    private Completion(@Nullable Throwable throwable) {
        this.throwable = throwable;
    }

    public boolean isFailed() {
        return throwable != null;
    }
    
    public boolean isSuccess() {
        return throwable == null;
    }

    public static Completion failed(Throwable throwable) {
        requireNonNull(throwable, "throwable is null");
        return new Completion(throwable);
    }

    // Are also patterns
    // completed???
    public static Completion success() {
        return SUCCESS;
    }
}

class Result<T> {
    // hmmm Saa virker den ikke med primitives
    // Maaske to felter alligevel
    @Nullable
    private Object value;

    public static <T> Result<T> failure(Throwable t) {
        return null;
    }

    public static <T> Result<T> success(T value) {
        requireNonNull(value, "value is null");
        if (value instanceof Throwable) {
            throw new IllegalArgumentException("Cannot specify a Throwable as the value");
        }
        return null;
    }
}
// Taenker vi skal have en "Job" version