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
package app.packed.lifecycle;

import app.packed.base.invoke.InvokableDescriptor;
import app.packed.component.Component;
import app.packed.container.Extension;

/**
 *
 */
public interface LifecycleTaskDescriptor {

    /**
     * Returns the extension that is performing stuff
     * 
     * @return stuff
     */
    // Ideen er lidt at hvis vi f.eks. har en @CheckFileExist (FileExtension) som skal koeres
    // Saa Staar FileExtension som extension...
    // @OnInitialize -> LifecycleExtension

    // Det er hvem der styrer den (controllingextension) og ikke hvem
    // Maaske owner??? idet component.extension() != extension()
    Class<? extends Extension> extension();

    Component component(); // Wirelet can have made a fake component $Wirelet$ServiceExtension

    /**
     * Returns a readable description of the task.
     * 
     * @return a readable description of the task
     */
    String description();

    // Invokable...
    // Men vi vil jo ogsaa gerne kunne se hvordan den er resolvet???
    // Altsaa det er jo her det kunne vaere fedt at have det lidt mere dynamisk...
    // At ligesaa snart den resolver den. Saa dukker den InjectionContext op i properties
    // InjectionManager.resolve(LTD.invokeable?)

    InvokableDescriptor invokeable();

    LifecycleState state();
}
// En metode/executable/field/function

//ltds.at(LifecycleState.initializing).print();
