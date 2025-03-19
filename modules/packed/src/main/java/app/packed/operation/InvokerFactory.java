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
package app.packed.operation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

import app.packed.bean.BeanConfiguration;
import app.packed.binding.InjectableToken;

/**
 * A factory interface for creating invokers that provide different ways to execute an underlying operation.
 * <p>
 * This interface provides methods to convert an abstract operation into concrete invocation mechanisms such as method
 * handles, var handles, or custom functional interfaces (SAM types). This allows for flexible execution strategies
 * while maintaining a consistent operation model.
 * <p>
 * The invoker factory is primarily used in contexts where the caller needs to choose the most appropriate invocation
 * mechanism based on their specific requirements, performance considerations, or integration needs.
 *
 * @see MethodHandle
 * @see VarHandle
 */
// Alternative to extending it
// invoke().as()...
// invoke().asMethodHandle();
// invoke().debug().as(....)
// OperationTemplaten, angiver om vi vil have en bean foerst...
public interface InvokerFactory {

    /**
     * Creates an invoker as an instance of the specified type.
     * <p>
     * This method creates an implementation of the specified class that will invoke the underlying operation. The specified
     * class must be either:
     * <ul>
     * <li>A functional interface (SAM type)</li>
     * <li>An abstract class with a single abstract method</li>
     * </ul>
     * <p>
     * If an abstract class is specified, the constructor may take parameters whose arguments can be specified using the
     * varargs parameter of this method.
     *
     * @param <T>
     *            the type of the invoker to create
     * @param handleClass
     *            the class or interface to implement
     * @param abstractClassConstructorArguments
     *            constructor arguments if the specified class is an abstract class
     * @return an instance of the specified class that can invoke the underlying operation
     *
     * @throws IllegalArgumentException
     *             if the specified class is not a valid functional interface or abstract class with a single abstract
     *             method
     * @throws IllegalArgumentException
     *             if the specified class is an abstract class taking parameters, and the provided arguments do not match
     *             the parameters
     * @throws IllegalArgumentException
     *             if the abstract method's signature is incompatible with the signature required by the underlying
     *             operation
     */
    <T> T invokerAs(Class<T> handleClass, Object... abstractClassConstructorArguments);

    // Det er her jeg gerne vil have Runnable
    // It would be nice to set a key
    default <T> void invokerAs2(BeanConfiguration bc, Class<T> handleClass, Object... abstractClassConstructorArguments) {
        throw new UnsupportedOperationException();
    }
    default <T> InjectableToken<T> invokerAs2(Class<T> handleClass, Object... abstractClassConstructorArguments) {
        throw new UnsupportedOperationException();
    }


    /**
     * Creates an invoker as a method handle.
     * <p>
     * This method returns a method handle that can be used to invoke the underlying operation directly. The returned method
     * handle will have a type matching {@link #invokerType()}.
     *
     * @return a method handle for invoking the underlying operation
     */
    MethodHandle invokerAsMethodHandle();

    /**
     * Creates an invoker as a var handle.
     * <p>
     * This method returns a var handle that can be used to access or modify the underlying field or variable. This is
     * primarily useful for operations that represent field access or modification.
     *
     * @return a var handle for accessing the underlying field
     * @throws UnsupportedOperationException
     *             if the underlying operation cannot be represented as a var handle, for example, if it does not represent
     *             a field or variable access
     */
    VarHandle invokerAsVarHandle();

    /**
     * Returns the signature of the underlying operation that the invoker must match.
     * <p>
     * This method returns the type signature of the underlying operation that the invoker can execute, which describes the
     * parameter types and return type required for invocation. This information can be used to determine if the operation
     * is compatible with a given functional interface or to create compatible method handles.
     * <p>
     * The method handle returned by {@link #invokerAsMethodHandle()} will have this type as its {@link MethodHandle#type()
     * method handle type}.
     *
     * @return the method type signature of the underlying operation
     */
    MethodType invokerType();
}