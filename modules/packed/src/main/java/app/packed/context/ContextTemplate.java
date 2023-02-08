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
package app.packed.context;

import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.extension.Extension;
import internal.app.packed.context.PackedContextClass;

/**
 *
 */
public sealed interface ContextTemplate permits PackedContextClass {

    /** {@return the type of arguments that must be provided.} */
    List<Class<?>> contextArguments();

    /** {@return the context this template is a part of.} */
    Class<? extends Context<?>> contextClass();

    /** {@return the extension the context is a part of.} */
    Class<? extends Extension<?>> extensionClass();

    boolean isHidden();

    static ContextTemplate of(MethodHandles.Lookup caller, Class<? extends Context<?>> contextClass, Class<?>... invocationArguments) {
        return PackedContextClass.of(caller, false, contextClass, invocationArguments);
    }

    static ContextTemplate ofHidden(MethodHandles.Lookup caller, Class<? extends Context<?>> contextClass, Class<?>... invocationArguments) {
        return PackedContextClass.of(caller, true, contextClass, invocationArguments);
    }
}
