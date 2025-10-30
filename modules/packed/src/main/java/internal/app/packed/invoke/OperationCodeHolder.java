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
package internal.app.packed.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanSourceKind;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.invoke.MethodHandleUtil.LazyResolvable;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.BeanFactoryOperationHandle;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
// Ideen er at flytte metoder og felter fra OperationHandle herind.

//
public final class OperationCodeHolder {

    /** The generated method handle. */
    @Nullable
    public MethodHandle generatedMethodHandle;

    @Nullable
    public LazyResolvable lmh;

    public final OperationSetup operation;

    public MethodHandle methodHandle;

    public void setMethodHandle(MethodHandle mh) {
        if (methodHandle != null) {
            throw new IllegalStateException();
        }
        this.methodHandle = mh;
    }

    public OperationCodeHolder(OperationSetup operation) {
        this.operation = requireNonNull(operation);
    }

    /**
     * Creates an invoker as a method handle.
     * <p>
     * This method returns a method handle that can be used to invoke the underlying operation directly. The returned method
     * handle will have a type matching {@link #invokerType()}.
     *
     * @return a method handle for invoking the underlying operation
     */
    public MethodHandle asMethodHandle() {
        // If we have already created the final method handle return this
        MethodHandle result = generatedMethodHandle;
        if (result != null) {
            return result;
        }

        // If we already created a lazy method handle return this
        LazyResolvable l = lmh;
        if (l != null) {
            return l.handle();
        }

        // Check that we are still configurable
        // Remove this check for now
        // checkIsOpen();

        if (operation.bean.container.application.isAssembling()) {
            // Still assembling, we need to create a lazy method handle
            l = lmh = MethodHandleUtil.lazyF(operation.template.methodType, () -> generatedMethodHandle = newMethodHandle());

            // If eager codegen .addToSomeQueue, in case we are static java£
            result = l.handle();
        } else {
            // No longer assembling, lets create the method handle directly
            result = newMethodHandle();
            assert (result.type() == operation.template.methodType);
        }
        return result;
    }

    public MethodHandle newMethodHandle() {
        MethodHandle mha = newMethodHandle0();
        if (operation.handle() instanceof BeanFactoryOperationHandle) {
            BeanSetup bean = operation.bean;
            if (bean.beanKind == BeanLifetime.SINGLETON) {
                assert (bean.bean.beanSourceKind != BeanSourceKind.INSTANCE);

                // We store container beans in a generic object array.
                // Don't care about the exact type of the bean.
                mha = mha.asType(mha.type().changeReturnType(Object.class));

                // Vil faktisk gemme den førends sidste
                mha = BeanInitializer.MH_INVOKE_INITIALIZER.bindTo(bean).bindTo(mha);
            }
        }
        return mha;
    }

    /**
     * Generates a method handle that can be used to invoke the underlying operation. *
     * <p>
     * This method should never be called directly, only through {@link #configuration}
     * <p>
     * This method will be never called more than once for a single operation.
     *
     * <p>
     * This method cannot be called earlier than the code generating phase of the application.
     * <p>
     * The {@link MethodType method type} of the returned method handle must be {@code invocationType()}.
     *
     * @return the generated method handle
     *
     * @throws IllegalStateException
     *             if called before the code generating phase of the application.
     */
    private MethodHandle newMethodHandle0() {
        // Maybe have a check here instead, and specifically mention generateMethodHandle when calling
        // Check only called once
        MethodHandle mh = generatedMethodHandle;
        if (mh == null) {
            mh = generatedMethodHandle = OperationCodeGenerator.newMethodHandle(operation);
        }
        // What if we subclassing fucks it op?
        // It will fuck up the lazy generation

        // I think we maybe have this protected.
        // And methodHandle() will just create a lazy method handle if we assembling.
        assert (mh.type() == operation.template.methodType);
        return mh;
    }
}
