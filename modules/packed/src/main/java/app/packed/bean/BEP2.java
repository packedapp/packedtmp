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
package app.packed.bean;

import app.packed.inject.Factory;

/**
 *
 */
public interface BEP2 {

    /**
     * @return a new builder
     */
    BeanExtensionPoint$BeanCustomizer.Builder<?> statelessBuilder();
    
    <T> BeanExtensionPoint$BeanCustomizer.Builder<?> statelessBuilder(Class<?> type);
    
    <T> BeanExtensionPoint$BeanCustomizer.Builder<T> unmanagedBuilder(Class<T> implementation);

    <T> BeanExtensionPoint$BeanCustomizer.Builder<T> unmanagedBuilder(Factory<T> factory);
    
    <T> BeanExtensionPoint$BeanCustomizer.Builder<T> managedBuilder(boolean withinOperation, Class<T> implementation);

    <T> BeanExtensionPoint$BeanCustomizer.Builder<T> managedBuilder(boolean withinOperation, Factory<T> factory);
}
