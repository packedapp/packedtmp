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
package app.packed.component;

import java.lang.invoke.MethodHandles;

import app.packed.component.ComponentDriver.Option;
import packed.internal.component.PackedComponentDriver;

/**
 *
 */
public interface ComponentInstanceDriver<C, I> extends ComponentFactoryDriver<C, I> {

    ComponentDriver<C> bindInstance(I instance);

    static <C, I> ComponentInstanceDriver<C, I> of(MethodHandles.Lookup lookup, Class<? extends C> driverType, Option... options) {
        return PackedComponentDriver.ofInstance(lookup, driverType, options);
    }

    // <C,I> cast();

    // Altsaa vi vil have folk til at kalde den her

//    static <C, I> ComponentInstanceDriver<C, I> of(Object stuff) {
//        throw new UnsupportedOperationException();
//    }
}
