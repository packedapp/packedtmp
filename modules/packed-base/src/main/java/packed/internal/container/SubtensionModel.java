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
package packed.internal.container;

import java.lang.invoke.MethodHandle;

import app.packed.block.Extension;
import app.packed.block.InternalExtensionException;
import app.packed.block.Extension.Subtension;

/**
 *
 */

// Tror faktisk ikke der er nogen grund til at SubExtension.. kender 
// selve extensionen...
public final class SubtensionModel {

    private final ExtensionModel extension;

    private MethodHandle constructor;

    SubtensionModel(Builder builder) {
        extension = null;
    }

    public ExtensionModel extension() {
        return extension;
    }

    // Should we require
    Subtension newInstance(Extension extension, Class<?> requestor) {
        // MH contains a casting mh
        try {
            return (Subtension) constructor.invokeExact(extension, requestor);
        } catch (Throwable e) {
            throw new InternalExtensionException("Instantiation of " + Subtension.class.getSimpleName() + " failed", e);
        }
    }

    public static final class Builder {

    }
}
