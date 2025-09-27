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

import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.extension.ExtensionPoint;
import app.packed.namespace.NamespaceHandle;

/**
 * An installer for operations.
 * <p>
 * An installer can only be used once. After an operation has been installed, all methods will throw
 * {@link IllegalStateException}.
 */
public interface OperationInstaller /* permits PackedOperationInstaller */ {

    // redelegate(ExtensionPoint.UseSite extension, OperationTemplate);
    OperationInstaller delegateTo(ExtensionPoint.ExtensionPointHandle extension);

    /**
     * Creates the operation.
     *
     * @param <H>
     *            the type of operation handle to represent the operation
     * @param factory
     *            a factory responsible for creating the operation handle to represent the operation
     * @return the operation handle for the operation
     *
     * @throws IllegalStateException
     *             if the installer has already been used
     */
    <H extends OperationHandle<?>> H install(Function<? super OperationInstaller, H> factory);

    /**
     * Creates the operation and installs it into the specified namespace.
     *
     * @param <H>
     *            the type of operation handle to represent the operation
     * @param <N>
     *            the type of namespace we are installing the operation into
     * @param namespace
     *            the namespace we are installing the operation into
     * @param factory
     *            a factory responsible for creating the operation handle to represent the operation
     * @return the operation handle for the operation
     *
     * @throws IllegalStateException
     *             if the installer has already been used
     */
    <H extends OperationHandle<?>, N extends NamespaceHandle<?, ?>> H install(N namespace, BiFunction<? super OperationInstaller, N, H> factory);
}