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

import java.util.Optional;
import java.util.function.Supplier;

import app.packed.application.BuildException;
import app.packed.container.Extension;
import app.packed.container.ExtensionPoint;
import app.packed.container.InternalExtensionException;
import app.packed.operation.OperationHandle;

/** An extension point for {@link EntryPointExtension}. */
public class EntryPointExtensionPoint extends ExtensionPoint<EntryPointExtension> {

    EntryPointExtensionPoint() {}
    
    public OperationHandle specializeMirror(OperationHandle configuration, int id, Supplier<? extends EntryPointMirror> supplier) {
        // Ved ikke lige helt hvordan den skal fungere
        return configuration;
    }
    

    /**
     * {@return the extension that is managing the
     */
    public Optional<Class<? extends Extension<?>>> dispatcher() {
        return Optional.ofNullable(extension().share.dispatcher);
    }
    
    /**
     * @param beanOperation
     * @return the entry point id
     * 
     * @throws
     * @throws BuildException
     *             if another extension is already managing end points
     */
    // BuildException -> Altsaa tit er jo fordi brugeren har brugt annoteringer
    // for 2 forskellige extensions

    // return mirror?

    // Hvordan dispatcher vi videre til extensionen?
    // Vi kan registere en MethodHandle + Bean
    // Sige Main fungere som dobbelt
    // Callable er skidt syntes jeg
    // Skal vi have noget generelt dispatch teknologi?
    // Nå nej vi har jo entry point selectoren...
    public int registerEntryPoint(boolean isMain) {
        return extension().registerEntryPoint(usedBy(), isMain);
    }

    
    /**
     * Selects
     * 
     * <p>
     * If an extension that is not the managing extension. Attempts to have an instance of this interface injected. The
     * build will fail with an {@link InternalExtensionException}.
     */
    // Behoever kun blive brugt hvis man har mere end et EntryPoint
    // Maaske tager man evt. bare det foerste entry point som default
    // hvis der ikke blive sat noget

    // @AutoService
    // Kan injectes i enhver bean som er owner = managedBy...
    // For andre beans smider man InjectionException?

    public interface EntryPointSelector {

        /**
         * @param id
         *            the id of the entry point that should be invoked
         * @throws IllegalArgumentException
         *             if no entry point with the specified id exists
         * @throws IllegalStateException
         *             if the method is invoked more than once
         * @see EntryPointMirror#id()
         */
        void selectEntryPoint(int id);
    }

    // Her vender vi den om... og bruger ExtensionSupport#registerExtensionPoint
    public interface EntryPointReversePoint {
        int entryPoint();
    }
}
// Ideen er at man kan wrappe sin entrypoint wirelet..
// Eller hva...
// Du faar CLI.wirelet ind som kan noget med sine hooks
//static Wirelet wrap(Wirelet w) {
//  return w;
//}
