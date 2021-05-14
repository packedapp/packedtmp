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

import app.packed.container.BaseAssembly;

/**
 * Component drivers are responsible for configuring and creating new components.
 * <p>
 * They are rarely created by end-users. And it is possible to use Packed without every being directly exposed to
 * component drivers. Instead users would normally used some of the predifined used drivers such as ....
 * <p>
 * 
 * Every time you, for example, call {@link BaseAssembly#install(Class)} it actually de
 * 
 * install a
 * 
 * @param <C>
 *            the type of configuration that is returned when creating a new component
 */
public /* sealed */ interface ComponentDriver<C extends ComponentConfiguration> {

    ComponentDriver<C> with(Wirelet... wirelet);
    
    // ComponentType componentType();
    
    // Bliver noedt til at have en type omkring hvad det er vi er ved at tilfoeje...
    /// BeanComponentDriver??? ContainerComponentDriver
    /// Taenker man kan lave nogle checks i wire()
    // if (driver instanceof BeanComponentDriver bcd) {
    // assert(bcd.class instanceof Foo);
    // 
}
