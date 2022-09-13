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
    BeanHandle.Installer<?> statelessBuilder();
    
    <T> BeanHandle.Installer<?> statelessBuilder(Class<?> type);
    
    <T> BeanHandle.Installer<T> unmanagedBuilder(Class<T> implementation);

    <T> BeanHandle.Installer<T> unmanagedBuilder(Factory<T> factory);
    
    <T> BeanHandle.Installer<T> managedBuilder(boolean withinOperation, Class<T> implementation);

    <T> BeanHandle.Installer<T> managedBuilder(boolean withinOperation, Factory<T> factory);
}
// Vi har beans uden lifecycle men med instancer

// Fx en validerings bean <--

// LifetimeConfig
//// Unmanaged Bean instantiated and initialized by packed  (Init lifetime does not take bean instance)
//// Unmanaged Bean instantiated by the user and initialzied by packed (Init lifetime takes bean instance)

//// Stateless (with instance) validation bean only supports fx validation annotations?

//// 
// Unmanaged

// Setup - Teardown