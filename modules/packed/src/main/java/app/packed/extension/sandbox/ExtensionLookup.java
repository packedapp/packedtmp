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
package app.packed.extension.sandbox;

import static java.util.Objects.requireNonNull;

import app.packed.extension.Extension;
import packed.internal.util.ClassUtil;
import packed.internal.util.StackWalkerUtil;

/**
 *
 */
public final class ExtensionLookup {

    private final Class<? extends Extension<?>> extensionType;

    private ExtensionLookup(Class<? extends Extension<?>> extensionType) {
        this.extensionType = requireNonNull(extensionType);
    }

    public Class<? extends Extension<?>> extensionType() {
        return extensionType;
    }

    @SuppressWarnings("unchecked")
    public static ExtensionLookup lookup() {
        Class<?> callerClass = StackWalkerUtil.SW.getCallerClass();
        if (!Extension.class.isAssignableFrom(callerClass)) {
            throw new IllegalCallerException("Must be called from a subclass of " + Extension.class.getSimpleName());
        }
        return new ExtensionLookup((Class<? extends Extension<?>>) callerClass);
    }

    public static ExtensionLookup lookup(Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType);
        Class<?> callerClass = StackWalkerUtil.SW.getCallerClass();
        if (callerClass.getModule() != extensionType.getModule()) {
            throw new IllegalCallerException(
                    "Specified extensionType '" + extensionType + "' must be in same module as caller, caller module = " + callerClass.getModule().getName());
        }
        return new ExtensionLookup(extensionType);
    }
}
