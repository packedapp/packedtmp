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
package app.packed.lang.invoke;

/**
 *
 */
// ProvideArgument

// Ting som @Provides ikke supportere
// 1) Annoteringer uden @Qualifier (!=Key) [Fixed via VariableSidecar]
// 2) Annoteringer paa en specifik plads [Fixed via VariableSidecar???]
// 3) Decorering af existerende typer (Giver templaten adgang til alle typer)

public @interface ArgumentProvider {
    // Hmm, selve checket laver vi jo andet steds...

    // indexes!=0 -> This method ia always used for parameter XYZ.
    int[] indexes() default {};
}

// Maaske have @IndexedArgument og @AnnotatedArgument

// return type must match parameter type

// Most specific type, or fail...

/// Caching af VariableSidecar...
/// Hvis man gerne cache parameter...
// per instance/per jvm/pen