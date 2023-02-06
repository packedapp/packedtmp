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
package app.packed.extension.bridge;

import app.packed.container.Assembly;
import app.packed.container.ContainerHandle;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;

/**
 *
 */
public class Usage {

    public static void main(ContainerGuestBeanConfiguration<?> b) {

//        b.addBridge(ExtensionLifetimeBridge.EXPORTED_SERVICE_LOCATOR);

    }
}

class MyE extends Extension<MyE> {


    public void installSession(Assembly a) {
        ContainerGuestBeanConfiguration<String> b = base().newContainerGuest(String.class);

        ContainerHandle h = b.newInstaller(ContainerLifetimeMode.INITIALIZATION_AND_START_STOP).install(a);

        // Det giver basalt set ingen mening ikke at have disse i et map?
        // Men maaske det map ikke er synligt????

        @SuppressWarnings("unused")
        OperationHandle newSessionsHandle = h.lifetimeOperations().get(0);

        // mhStart.invokeExact(containerMapBean)
    }



}