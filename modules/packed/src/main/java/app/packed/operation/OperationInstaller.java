/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.util.function.Function;

import app.packed.component.SidehandleBeanConfiguration;
import app.packed.context.Context;
import app.packed.extension.ExtensionPoint;
import internal.app.packed.operation.PackedOperationInstaller;

/**
 * An installer for operations.
 * <p>
 * An installer can only be used once. After an operation has been installed, all methods will throw
 * {@link IllegalStateException}.
 *
 */
public sealed interface OperationInstaller permits PackedOperationInstaller {

    OperationInstaller addContext(Class<? extends Context<?>> contextClass);

    /**
     * @param configuration
     *            the sidebean to attach this operation to
     * @return
     *
     * @throws UnsupportedOperationException
     *             if the target kind of the sidebean is not {@link app.packed.bean.SidebeanTargetKind#OPERATION}.
     */
    OperationInstaller attachToSidebean(SidehandleBeanConfiguration<?> configuration);

    // redelegate(ExtensionPoint.UseSite extension, OperationTemplate);
    OperationInstaller delegateTo(ExtensionPoint.ExtensionPointHandle extension);

    OperationInstaller returnType(Class<?> type);

    OperationInstaller returnIgnore();

    OperationInstaller returnDynamic();

    /**
     * Installs the operation.
     *
     * @param <H>
     *            the type of operation handle to represent the operation
     * @param factory
     *            a factory responsible for creating the operation handle to represent the operation
     * @return the operation handle for the operation
     *
     * @throws IllegalStateException
     *             if the installer has already been used
     * @throws InaccessibleBeanMemberException
     *             if the framework does not have access to invoke the method
     * @throws InternalExtensionException
     *             if the extension does not have access to invoke the method
     */
    <H extends OperationHandle<?>> H install(Function<? super OperationInstaller, H> factory);
}