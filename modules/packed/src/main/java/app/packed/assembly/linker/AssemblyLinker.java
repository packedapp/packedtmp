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
package app.packed.assembly.linker;

import java.util.ServiceLoader;

import app.packed.assembly.Assembly;
import app.packed.container.Wirelet;
import app.packed.service.ProvidableBeanConfiguration;

/**
 *
 */
// AssemblyFinder kan bruges for flere
public interface AssemblyLinker {

    // Uses parent assembly.class.getClassLoader().find

    // Parent assembly/container must be able to instantiate the class u
    void link(String className, Wirelet... wirelets);

    void link(Module module, String className, Wirelet... wirelets);

    void link(String module, String className, Wirelet... wirelets);

    void link(Assembly assembly, Wirelet... wirelets);

    // fail if more than one
    void link(ServiceLoader<? extends Assembly> loader, Wirelet... wirelets);

    // Eneste problem er at vi faar en bean per container
    // Boer maask
    <T> ProvidableBeanConfiguration<T> linkAsBean(Class<T> bean, Assembly assembly, Wirelet... wirelets);

//    <T> ServiceableConfiguration<T> linkAsBean(GuestBeanConfiguration<T> bean, Assembly assembly, Wirelet... wirelets);

}
