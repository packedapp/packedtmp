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
package sandbox.extension.operation;

import java.lang.invoke.VarHandle.AccessMode;

import app.packed.bean.BeanTemplate;

/**
 *
 */
// What is this???
// Jeg tror det er noget ed Field at goere (F'et i navnet)

public interface BF {

    // if Static Bean -> Takes no BeanInstance, If NonStaticBean takes instance
    // Omvendt kunne det vaere rart at droppe

    void newVarHandleOperation(ContextList list);

    void newGetOperation(Class<?> invocationType, ContextList list);

    void newSetOperation(Class<?> invocationType, ContextList list);

    void newOperation(AccessMode accessMode, Class<?> invocationType, ContextList list);

    interface ContextList {}

    OperationHandle factoryForThis(); // Always a get operation

    // Will create a dependency on this bean, even if static
    BeanTemplate.Installer newInstaller(BeanTemplate kind); // cannot be static
}
