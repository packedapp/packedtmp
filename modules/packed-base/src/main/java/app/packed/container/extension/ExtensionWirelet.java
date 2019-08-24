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
package app.packed.container.extension;

import static java.util.Objects.requireNonNull;

import app.packed.container.Wirelet;
import packed.internal.util.StringFormatter;

/**
 *
 *
 * 
 */

// CheckConfigurable is more important than ever...
// Bacause basically anyone

// Maybe check that they are the same module....
public abstract class ExtensionWirelet<T extends Extension> extends Wirelet {

    final Class<T> extensionType;

    protected ExtensionWirelet(Class<T> extensionType) {
        this.extensionType = requireNonNull(extensionType, "extensionType is null");
        if (extensionType.getModule() != getClass().getModule()) {
            throw new IllegalArgumentException("The wirelet and the extension must be defined in the same module, however extension "
                    + StringFormatter.format(extensionType) + " was defined in " + extensionType.getModule() + ", and this wirelet type "
                    + StringFormatter.format(getClass()) + " was defined in module " + getClass().getModule());
        }
    }

    /// *, ArtifactInstantiationContext context/*
    // Eftersom vi kan lave flere instanser fra det samme image.
    // Saa bliver vi noedt til at have instantiation context med

    // create container...

    protected abstract void process(T extension);
}
