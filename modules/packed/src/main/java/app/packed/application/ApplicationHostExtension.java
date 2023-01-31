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

import java.lang.invoke.MethodHandle;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.extension.BaseExtensionPoint.BeanInstaller;
import app.packed.extension.FrameworkExtension;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;

/**
 *
 */

// Skal man ogsaa bruge den til lazy???
// Eller kun til dynamic.

// Er ude i maaske multiple application er paa BaseExtension.
// Men vil man deploye at runtime jo need this host/deployer extension
public class ApplicationHostExtension extends FrameworkExtension<ApplicationHostExtension> {

    static final OperationTemplate ot = OperationTemplate.raw().withArg(ApplicationInitializationContext.class).withReturnTypeObject();

    MethodHandle mh;
    ApplicationHostExtension() {}

    public <T> ApplicationHostConfiguration<T> newApplication(Class<T> guestBean) {
        // We need the attachment, because ContainerGuest is on
        BeanInstaller bi = base().newBean(BeanKind.MANYTON).attach(InstallingAppHost.class, new InstallingAppHost()).lifetimes(ot);
        return newApplication(bi.install(guestBean));
    }

    public <T> ApplicationHostConfiguration<T> newApplication(Op<T> guestBean) {
        // We need the attachment, because ContainerGuest is on
        BeanInstaller bi = base().newBean(BeanKind.MANYTON).attach(InstallingAppHost.class, new InstallingAppHost()).lifetimes(ot);
        return newApplication(bi.install(guestBean));
    }

    private <T> ApplicationHostConfiguration<T> newApplication(BeanHandle<T> handle) {
        OperationHandle oh = handle.lifetimeOperations().get(0);
        this.runOnCodegen(() -> {
            mh = oh.generateMethodHandle();
        });
        return new ApplicationHostConfiguration<>(handle);

    }


    static class BootstrapBean {

    }

    static class InstallingAppHost {


        public InstallingAppHost() {
        }
    }

}
// installPermanent
// installLazy (with activator)