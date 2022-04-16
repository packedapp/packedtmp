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
package app.packed.extension;

import app.packed.component.Realm;
import packed.internal.container.PackedExtensionPointContext;

/**
 * A context object that can be injected into subclasses of {@link ExtensionPoint}.
 */
public sealed interface ExtensionPointContext permits PackedExtensionPointContext {

    default void checkInSameContainerAs(Extension<?> extension) {
     // Maaske vi skal lave nogle checks saa man ikke bare kan bruge den hvor man har lyst.
     // Men at vi binder den til en container...

        // IDK
     // ExtensionSupportUSer???
    }
    
    /**
     * 
     * @see Extension#checkConfigurable()
     */
    void checkConfigurable();

    Class<? extends Extension<?>> extensionType();

    Realm realm();
}
//
//// checkExtendable...
///**
// * Checks that the new extensions can be added to the container in which this extension is registered.
// * 
// * @see #onAssemblyClose()
// */
//// Altsaa det er jo primaert taenkt paa at sige at denne extension operation kan ikke blive invokeret
//// af brugeren med mindre XYZ...
//// Det er jo ikke selve extension der ved en fejl kommer til at kalde operationen...
//protected final void checkExtensionConfigurable(Class<? extends Extension<?>> extensionType) {
//    configuration().checkExtensionConfigurable(extensionType);
//}