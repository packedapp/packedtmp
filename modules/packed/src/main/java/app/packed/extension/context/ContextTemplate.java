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
package app.packed.extension.context;

import java.lang.invoke.MethodHandles;

import app.packed.context.Context;
import app.packed.extension.Extension;
import internal.app.packed.context.PackedContextTemplate;

/**
 *
 */
public sealed interface ContextTemplate permits PackedContextTemplate {

    /** {@return the context this template is a part of.} */
    Class<? extends Context<?>> contextClass();

    /** {@return the extension the context is a part of.} */
    Class<? extends Extension<?>> extensionClass();

    boolean isHidden();

    /** {@return the type of value the context provides.} */
    Class<?> valueClass();

    static ContextTemplate of(MethodHandles.Lookup caller, Class<? extends Context<?>> contextClass, Class<?> valueType) {
        return PackedContextTemplate.of(caller, false, contextClass, valueType);
    }

    static ContextTemplate ofHidden(MethodHandles.Lookup caller, Class<? extends Context<?>> contextClass, Class<?> valueType) {
        return PackedContextTemplate.of(caller, true, contextClass, valueType);
    }

    // Maaske har vi ogsaa Span her... Saa maa man bare lave mere end en instans

    // never visible to extensions that does not have a dependency on the #extensionClass
    enum Visibility {
        ALL, ALL_DEPENDENCIES, THIS_EXTENSION;
    }
    // Or Private, Protected, Public
    // Ved ikke om vi kan bruge den andre steder

}
