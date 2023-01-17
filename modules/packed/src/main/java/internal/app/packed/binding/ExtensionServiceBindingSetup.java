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
package internal.app.packed.binding;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.binding.BindingClassifierKind;
import app.packed.container.Realm;
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifetime.ApplicationInitializationContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.Osi;

/**
 *
 */
public final class ExtensionServiceBindingSetup extends BindingSetup {

    public BeanSetup extensionBean;

    public final Class<?> extensionBeanClass;

    /**
     * @param operation
     * @param index
     */
    public ExtensionServiceBindingSetup(OperationSetup operation, int index, Class<?> extensionBeanClass) {
        super(operation, index, Realm.extension(BaseExtension.class));
        if (extensionBeanClass == ApplicationInitializationContext.class) {
            throw new Error();
        }
        this.extensionBeanClass = extensionBeanClass;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle bindIntoOperation(Osi osi, MethodHandle methodHandle) {
        System.out.println(methodHandle);
        osi.is.push(0);
        System.out.println("!");
        MethodHandle mh = extensionBean.instanceAccessOperation().generateMethodHandle();
        System.out.println("!2");
        return MethodHandles.collectArguments(methodHandle, index, mh);
    }

    public BindingClassifierKind kind() {
        return BindingClassifierKind.KEY;
    }
}
