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

import app.packed.inject.Factory;

/**
 *
 */
// Maaske bare 
public interface BindableComponentDriver<C extends ComponentConfiguration, I> {

    /**
     * Applies the specified instance in order to create a component driver.
     * 
     * @param instance
     *            the instance
     * @return a component driver with applied instance
     */
    ComponentDriver<C> applyInstance(I instance);

    /**
     * @param implementation
     *            the implementation to bind
     * @return a new bound component driver
     */
    ComponentDriver<C> bind(Class<? extends I> implementation);
    
    ComponentDriver<C> bind(Factory<? extends I> factory);

    /**
     * Binds the specified function and returns a final component driver.
     * 
     * @param function
     *            the function to bind
     * @return a new component driver
     */
    ComponentDriver<C> bindFunction(Object function);
}

