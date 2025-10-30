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
package internal.app.packed.invoke.sandbox;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.container.ContainerBuildLocal;
import app.packed.extension.Extension;
import internal.app.packed.util.StackWalkerUtil;
import internal.app.packed.util.types.ClassUtil;

/**
 * <p>
 * If you have an instance of ExtensionLookup<E> you can do anything you want for that particular extension
 */
// Hvor skal vi bruges

/// Indtil videre har vi de her 2 use cases

// ContainerTemplateAction
// Context
// ComponentKind???
public final class ExtensionLookup {

    private final Class<? extends Extension<?>> extensionType;

    private ExtensionLookup(Class<? extends Extension<?>> extensionType) {
        this.extensionType = requireNonNull(extensionType);
    }

    // HAve ExtensionLookup<T>????
    public ContainerBuildLocal<?> local() {
        throw new UnsupportedOperationException();
    }

    public Class<? extends Extension<?>> extensionType() {
        return extensionType;
    }

    public static ExtensionLookup of(MethodHandles.Lookup caller, Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, "extensionType");
        if (caller.hasFullPrivilegeAccess()) {
            throw new IllegalArgumentException();
        }
        if (caller.lookupClass().getModule() != extensionType.getModule()) {
            throw new IllegalCallerException("Specified extensionType '" + extensionType + "' must be in same module as caller, caller module = "
                    + caller.lookupClass().getModule().getName());
        }
        return new ExtensionLookup(extensionType);
    }

    @SuppressWarnings("unchecked")
    public static ExtensionLookup of() {
        Class<?> callerClass = StackWalkerUtil.SW.getCallerClass();
        if (!Extension.class.isAssignableFrom(callerClass)) {
            throw new IllegalCallerException("Must be called from a subclass of " + Extension.class.getSimpleName());
        }
        return new ExtensionLookup((Class<? extends Extension<?>>) callerClass);
    }
}
