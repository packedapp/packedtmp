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
package app.packed.component.drivers.old;

import java.lang.invoke.MethodHandles;

import app.packed.component.ComponentConfiguration;
import app.packed.component.drivers.ComponentDriver;
import app.packed.component.drivers.ComponentDriver.Option;
import app.packed.inject.Factory;
import packed.internal.component.PackedComponentDriver;

/**
 *
 */
public interface ComponentFactoryDriver<C extends ComponentConfiguration, I> extends ComponentClassDriver<C, I> {

    @Override
    default ComponentDriver<C> bind(Class<? extends I> implementation) {
        return bind(Factory.of(implementation));
    }

    ComponentDriver<C> bind(Factory<? extends I> factory);

    static <C extends ComponentConfiguration, I> ComponentFactoryDriver<C, I> of(MethodHandles.Lookup lookup, Class<? extends C> driverType, Option... options) {
        return PackedComponentDriver.ofFactory(lookup, driverType, options);
    }
}
