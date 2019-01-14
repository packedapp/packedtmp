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
package app.packed.hook;

import java.lang.annotation.Annotation;

/**
 *
 */

// Analysis

// On Initialize

// @Inject <- Inject phase ..... Saa burde det vel ogsaa virke i injector:!>>!!! only, on non-provided methods...
// betyder det vi ogsaa har hooks....??? Naaah, maaske vi goer det paa en anden maade
// @OnInitialize
// @OnStart
// @OnStop
// @OnNative.....
public @interface AnnotatedXHook {
    Class<? extends Annotation> annotation();
}

// Maaske allow it..... on non-provided methods.... Its

// Okay
