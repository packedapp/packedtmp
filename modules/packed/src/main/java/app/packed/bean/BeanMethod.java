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
package app.packed.bean;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.bean.BeanProcessor.BeanElement;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.inject.FactoryType;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationInvocationType;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTargetMirror;
import internal.app.packed.bean.hooks.PackedBeanMethod;

/**
 * This class represents a {@link Method} on a bean.
 * 
 * @see BeanExtensionPoint.MethodHook
 * @see BeanProcessor#onMethod(BeanMethod)
 */
public sealed interface BeanMethod extends BeanElement permits PackedBeanMethod {

    /** {@return a factory type for this method.} */
    FactoryType factoryType();

    /**
     * {@return the modifiers of then underlying method.}
     *
     * @see Method#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
     */
    int getModifiers();

    /**
     * @return
     */
    boolean hasInvokeAccess(); // isOperational?

    /** {@return the underlying method.} */
    Method method();

    /**
     * <p>
     * Any {@link OperationMirror} created from the operation, will have a {@link OperationTargetMirror.OfMethodInvoke} as
     * its {@link OperationMirror#target()}.
     * 
     * @param operator
     *            the extension bean that will invoke the operation. The extension bean must be located in the same (or a
     *            direct ancestor) container as the bean that declares this method.
     * @return a new operation
     * 
     * @see Lookup#unreflect(Method)
     * @see BeanMethodHook#allowInvoke()
     * @see BeanClassHook#allowAllAccess()
     * 
     * @throws IllegalArgumentException
     *             if the specified operator is not a direct ancestor of the method's bean.
     */
    OperationConfiguration newOperation(ExtensionBeanConfiguration<?> operator, OperationInvocationType operationType);
}

/**
 * Returns a direct method handle to the {@link #method()} (without any intervening argument bindings or transformations
 * that may have been configured elsewhere).
 * 
 * @return a direct method handle to the underlying method
 * @see Lookup#unreflect(Method)
 * @see BeanMethodHook#allowInvoke()
 * @see BeanClassHook#allowAllAccess()
 * 
 * @throws UnsupportedOperationException
 *             if invocation access has not been granted via {@link BeanMethodHook#allowInvoke()} or
 *             BeanClassHook#allowAllAccess()
 */