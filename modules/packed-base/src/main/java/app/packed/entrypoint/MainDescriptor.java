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
package app.packed.entrypoint;

import app.packed.component.ComponentPath;
import app.packed.component.feature.Feature;

/**
 *
 */
// Main.Descriptor instead of....
// Provide.Descriptor instead of
// OnXXX.Descriptor??? passer ikke rigtig
// Daarlig ide...
// Lad os implementere det descriptor ting med Extensions foerst....
public interface MainDescriptor extends Feature {

    // Har man en descriptor id???
    //// Kunne jo vaere super f.eks. at kunne override den....
    //// Hvordan styrer man om man maa override den...

    /**
     * The path to the component that defines the main method.
     * <p>
     * If using {@link EntryPointExtension#main(Runnable)} the component path will point to the container in which the
     * extension is used.
     * 
     * @return the path to the component that defines the main method
     */
    @Override
    ComponentPath path();

    // Den har ogsaa en function.....
    // Is Either a
    // StackConfigSite -> If using LifecycleExtension.main
    // AnnotatedMethod -> If using @Main
    // Unknown -> If using LifecycleExtensionMain and stack capture is disabled
}
