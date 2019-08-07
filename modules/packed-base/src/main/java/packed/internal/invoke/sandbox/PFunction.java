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
package packed.internal.invoke.sandbox;

import app.packed.util.Nullable;

/**
 *
 */
public abstract class PFunction<T> {

    /**
     * Instantiates a new object using the specified parameters
     * 
     * @param params
     *            the parameters to use
     * @return the new instance
     */
    @Nullable
    public abstract T invoke(WhatAreYouDoing spec, Object[] params);

    // Ideen er at vi kan tage den med som parameter...
    // For eksempel om vi skal smide en exception
    public enum WhatAreYouDoing {}
}
// Input/Output

/// Input OutPout

// return type -> void type.... void is valid
// Parameters... might be dependencies if used for this, but does not have to

// expected

// Parameter count

// isExactType
// isAnyType();
// boolean isNullable

// Type
// IsNullable
// HasExactType

// Bundle ting,
// registrering required optionally, exposed... -> exposes the following features
// Nogle runtimeklasser...

// requires, exposes, ... -> Key-> Service

// Et eller andet slags registrerings object
/// Baade scanning af klasserne.... (Med annotering...)
/// Og reject af prototype...

// Supportere ogsaa manuel registrering af objekter
