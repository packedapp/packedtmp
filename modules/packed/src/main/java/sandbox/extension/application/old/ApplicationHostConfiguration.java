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
package sandbox.extension.application.old;

import java.util.function.Supplier;

import app.packed.assembly.Assembly;
import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Wirelet;

/**
 *
 */
class ApplicationHostConfiguration<T> extends InstanceBeanConfiguration<T> {


    // vi har vel lazy
    // 1-deploye
    // 1-deploye-and remove
    // m-deploye-and remove

    /**
     * @param handle
     */
    public ApplicationHostConfiguration(BeanHandle<?> handle) {
        super(handle);
    }

    public void deployLazy(Supplier<? extends Assembly> assemblySupplier, Wirelet... wirelets) {}
}

//class MyS {
//
//    public void host(ApplicationHostConfiguration<?> host, ServiceContract s) {
//        // s maa enten kunne vaere requires, eller kun provide
//        // s maa kun vaere provide...
//
//        // Men kan godt have static requires. og dynamiske provides
//    }
//}
