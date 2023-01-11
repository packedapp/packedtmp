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
package app.packed.application;

import java.util.function.Supplier;

import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.service.ServiceContract;

/**
 *
 */
public class ApplicationHostConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    public ApplicationHostConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    // vi har vel lazy
    // 1-deploye
    // 1-deploye-and remove
    // m-deploye-and remove
    
    public void deployLazy(Supplier<? extends Assembly> assemblySupplier, Wirelet... wirelets) {}
}

class MyS {
    
    public void host(ApplicationHostConfiguration<?> host, ServiceContract s) {
        // s maa enten kunne vaere requires, eller kun provide
        // s maa kun vaere provide...
        
        // Men kan godt have static requires. og dynamiske provides
    }
}
