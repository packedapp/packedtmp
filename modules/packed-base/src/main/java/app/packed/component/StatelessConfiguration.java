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

import packed.internal.component.PackedWireableComponentDriver;

/**
 *
 */
// I don't like the name...
public interface StatelessConfiguration extends ComponentConfiguration {

    /**
     * Yup
     * 
     * @return yup
     */
    Class<?> definition();

    /** {@inheritDoc} */
    @Override
    StatelessConfiguration setName(String name);

    /**
     * Returns a driver that can be used to create stateless components.
     * 
     * @param <T>
     *            the type
     * @return a driver
     */
    static <T> ClassSourcedDriver<StatelessConfiguration, T> driver() {
        return PackedWireableComponentDriver.StatelessComponentDriver.driver();
    }
}