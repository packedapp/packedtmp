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
package app.packed.component.sandbox;

import java.util.function.BiPredicate;

import app.packed.component.Component;

/**
 *
 */

// I modsaetning til et predicate. Har den ogsaa metoder for at walke et trae...

// Process component?
// Process children?
// Order?

// Starter altid fra en specific origin..
//// Ihvertfald hvis man kunne arbejde med relativ dybde...
//// Og i samme container
//// Faktisk hvis man vil kunne arbejde med noget interssant
public final class ComponentSelector {

    private static final ComponentSelector ALL = new ComponentSelector();

    private ComponentSelector() {}

    public void processIf(BiPredicate<? super Component, ? super Component> originActualPredicate) {

    }

    public static ComponentSelector all() {
        return ALL;
    }
}
