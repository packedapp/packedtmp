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

import app.packed.component.drivers.ComponentClassDriver;
import app.packed.component.drivers.ComponentDriver;
import packed.internal.component.PackedComponentDriver;

/**
 *
 */
public final class StatelessConfiguration extends BaseComponentConfiguration {

    /** A driver for this configuration. */
    @SuppressWarnings("rawtypes")
    private static final ComponentClassDriver DRIVER = PackedComponentDriver.ofClass(MethodHandles.lookup(), StatelessConfiguration.class,
            ComponentDriver.Option.statelessSource());

    private StatelessConfiguration(ComponentConfigurationContext context) {
        super(context);
    }

    /** {@inheritDoc} */
    @Override
    public StatelessConfiguration setName(String name) {
        context.setName(name);
        return this;
    }

    /**
     * Returns a driver that can be used to create stateless components.
     * 
     * @param <T>
     *            the type
     * @return a driver
     */
    @SuppressWarnings("unchecked")
    public static <T> ComponentClassDriver<StatelessConfiguration, T> driver() {
        return DRIVER;
    }
}
