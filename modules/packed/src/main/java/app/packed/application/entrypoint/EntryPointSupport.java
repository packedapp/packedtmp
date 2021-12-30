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
package app.packed.application.entrypoint;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.build.BuildException;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;

/**
 *
 */
@ExtensionMember(EntryPointExtension.class)
public class EntryPointSupport extends ExtensionSupport {

    final EntryPointExtension extension;

    EntryPointSupport(EntryPointExtension extension) {
        this.extension = requireNonNull(extension);
    }

    /**
     * @param beanOperation
     * @return the entry point id
     * 
     * @throws
     * @throws BuildException
     *             if another extension is already managing end points
     */
    public int registerEntryPoint(Object beanOperation) {
        return 0;
    }

    public Optional<Class<? extends Extension<?>>> managedBy() {
        // Man bliver managed foerste gang med registrer en end point
        return Optional.empty();
    }

    /**
     * Selects
     */
    // Behoever kun blive brugt hvis man har mere end et EntryPoint
    // Maaske tager man evt. bare det foerste entry point som default
    // hvis der ikke blive sat noget

    // @AutoService
    interface EntryPointSelector {

        /**
         * @param id
         *            the id of the entry point that should be invoked
         * @throws IllegalArgumentException
         *             if no entry point with the specified id exists
         * 
         * @see EntryPointMirror#id()
         */
        void selectEntryPoint(int id);
    }
}

///**
//* 
//* @throws IllegalStateException
//*             if multiple different extensions tries to add entry points
//*/
//public void manage() {
//  // Maaske automanager vi bare ting, naar bruger den her Subtension...
//  // Er ikke sikker paa der er nogen grund til at aktivere den, hvis
//  // man ikke har intension om at bruge den
//}

// Ideen er at man kan wrappe sin entrypoint wirelet..
// Eller hva...
// Du faar CLI.wirelet ind som kan noget med sine hooks
//static Wirelet wrap(Wirelet w) {
//  return w;
//}
