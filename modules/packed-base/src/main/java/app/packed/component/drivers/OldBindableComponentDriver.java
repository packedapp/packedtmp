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
package app.packed.component.drivers;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;

/**
 * A
 */
//ComponentDriverFactory
//ComponentDriverMaker
//ComponentDriverBootstrap
//SourceableComponentDriver
//BindableComponentDriver
// Altsaa i foerste
public /*/* sealed */ interface OldBindableComponentDriver<C extends ComponentConfiguration> {
    
    // Vi flytter de her metoder til ComponentDriver
    ComponentDriver<C> bind(Object source);

    // Vi flytter de her metoder til ComponentDriver
    ComponentDriver<C> bind(Object... sources);
}
// @apinote Earlier versions of this interface was spread over multiple type safe interfaces.
// However, in the end 

// ComponentDriver.bind(BindableComponentDriver, sdfsdf);

abstract class AlterSCD<C extends ComponentConfiguration> {

    // Kunne maaske endda declare den protected...
    public final ComponentDriver<C> create(Object source) {
        throw new UnsupportedOperationException();
    }

    protected final ComponentConfigurationContext source(Object source) {
        throw new UnsupportedOperationException();
    }
}

//class MyBeanConfigurationComponentDriver extends AlterSCD<ServiceComponentConfiguration<?>> {
//
//    @Provide
//    public String fff() {
//        return "fff";
//    }
//
//    public <T> ServiceComponentConfiguration<T> create(Class<T> type) {
//        return new ServiceComponentConfiguration<>(source(type));
//    }
//}