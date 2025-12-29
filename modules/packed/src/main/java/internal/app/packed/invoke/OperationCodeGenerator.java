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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.function.Supplier;

import app.packed.bean.BeanLifetime;
import app.packed.binding.ProvisionException;
import app.packed.util.Nullable;
import internal.app.packed.bean.sidebean.PackedSidebeanAttachment;
import internal.app.packed.bean.sidebean.SidebeanHandle;
import internal.app.packed.binding.BindingProvider;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.invoke.MethodHandleUtil.LazyResolvable;
import internal.app.packed.lifecycle.LifecycleOperationHandle.FactoryOperationHandle;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;

/**
 *
 */
public final class OperationCodeGenerator {

    @Nullable
    public LazyResolvable cachedLazyMethodHandle;

    /** The generated method handle. */
    // Vi har behov for cachen fordi kode generering kalde generate rekursivt.
    @Nullable
    private MethodHandle cachedMethodHandle;

    /** The operation we are generating a method handle for */
    private final OperationSetup operation;

    @Nullable
    public
    final PackedSidebeanAttachment sidebean;

    /**
     * @param packedSideBeanUsage
     */
    public OperationCodeGenerator(OperationSetup operation, @Nullable PackedSidebeanAttachment sidebean) {
        this.operation = operation;
        this.sidebean = sidebean;
    }

    MethodHandle generate(boolean lazy) {
        if (lazy) {
            LazyResolvable lazyMH = cachedLazyMethodHandle;
            if (lazyMH == null) {
                lazyMH = cachedLazyMethodHandle = MethodHandleUtil.lazyF(operation.template.methodType, () -> newMethodHandle());
            }
            return lazyMH.handle();
        } else {
            return generateMethodHandle();
        }
    }

    /**
     * Creates an invoker as a method handle.
     * <p>
     * This method returns a method handle that can be used to invoke the underlying operation directly. The returned method
     * handle will have a type matching {@link #invokerType()}.
     *
     * @return a method handle for invoking the underlying operation
     */

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
    private MethodHandle generateMethodHandle() {
        // Maybe have a check here instead, and specifically mention generateMethodHandle when calling
        // Check only called once
        MethodHandle mh = cachedMethodHandle;
        if (mh == null) {
            mh = cachedMethodHandle = newMethodHandle();
        }
        return mh;
    }

    private MethodHandle newMethodHandle() {
        operation.bean.container.application.checkInCodegenPhase();

        boolean isFactory = operation.handle() instanceof FactoryOperationHandle;

        boolean requiresBeanInstance = !isFactory && operation.target instanceof MemberOperationTarget mot && !Modifier.isStatic(mot.target.modifiers());

        boolean isSideBeanInstance = sidebean != null;
        boolean isSideBeanClass = operation.bean.handle() instanceof SidebeanHandle;

        if (isSideBeanInstance) {
            // Get the incomplete MethodHandle
            MethodHandle methodHandle = operation.codeHolder.generateMethodHandle();
            if (isFactory) {
                methodHandle = methodHandle.asType(methodHandle.type().changeReturnType(Object.class));
                methodHandle = BeanLifecycleSupport.MH_INVOKE_INITIALIZER_SIDEBEAN.bindTo(sidebean).bindTo(methodHandle);
                return methodHandle;
            }
            MethodHandle tmp = MethodHandles.insertArguments(ServiceHelper.MH_CONSTANT_POOL_READER, 1, sidebean.lifetimeStoreIndex.index);
            assert tmp.type().returnType() == Object.class;
            // We need to convert it from Object to the expected type
            tmp = tmp.asType(tmp.type().changeReturnType(sidebean.sidebean.bean.beanClass));

            System.out.println("XXXX " + isFactory);
            System.out.println("XXXX " + operation.handle().getClass());

            System.out.println("YYYY " + operation.bean.beanKind);
            System.out.println("XXXX " + methodHandle.type());
            System.out.println("XXXX " + tmp.type());

            return MethodHandleUtil.merge(methodHandle, tmp);
        }

        ArrayList<Integer> permuters = new ArrayList<>();

        MethodType invocationType = operation.template.invocationType();

        MethodHandle mh = operation.target.methodHandle();

        if (requiresBeanInstance) {
            if (!isSideBeanClass) {
                BindingProvider beanInstanceProvider = operation.bean.beanInstanceBindingProvider();
                mh = provide(mh, beanInstanceProvider, permuters, isSideBeanClass);
            } else {
                // If we are a sidebean, the generated method handle, must take a bean instance as the first parameter.
                // This parameter will be bound later at the sidebean usage site, typically to lifetime array reader of the bean
                invocationType = invocationType.insertParameterTypes(0, operation.bean.bean.beanClass);
                permuters.add(0);
            }
        }

//        System.out.println(mh.type());

        for (BindingSetup binding : operation.bindings) {
            if (binding == null) {
                System.out.println(operation.type);
            }
            if (binding.provider() == null) {
                // Should not really be here where we fail, much earlier
                throw new ProvisionException("Oops for " + operation.type);
            }
            mh = provide(mh, binding.provider(), permuters, isSideBeanClass);
        }

        int[] result = new int[permuters.size()];
        for (int i = 0; i < permuters.size(); i++) {
            result[i] = permuters.get(i);
        }
        // System.out.println("InvokeAs " + invocationType + "Target " + operation.target.methodHandle().type() + " Perm:" +
        // permuters);
        // Embedded operations normally needs to return a value
        if (operation.embeddedInto != null) {
            invocationType = invocationType.changeReturnType(operation.type.returnRawType());
        }

        try {
            mh = MethodHandles.permuteArguments(mh, invocationType, result);
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.err.println("Permuter array " + permuters);
            result[1] = 1;
            mh = MethodHandles.permuteArguments(mh, invocationType, result);
            throw e;
        }

        if (mh.type() != invocationType) {
            System.err.println("OperationType " + operation.type.toString());
            System.err.println("Expected " + operation.template.methodType);
            System.err.println("Actual " + mh.type());
            throw new Error();
        }

        assert mh.type() == operation.template.invocationType();

        // We need to store singleton beans in their respective lifetime.
        boolean isFactoryStore = isFactory && operation.bean.beanKind == BeanLifetime.SINGLETON;
        if (isFactoryStore) {
            mh = mh.asType(mh.type().changeReturnType(Object.class)); // We store in Object[]
            mh = BeanLifecycleSupport.MH_INVOKE_INITIALIZER.bindTo(operation.bean).bindTo(mh);
            // Do we need to change the type back again????
            // Maybe
        }
        return mh;
    }

    private MethodHandle provide(MethodHandle mh, BindingProvider p, ArrayList<Integer> permuters, boolean isSidebean) {
        int extensionIndex = isSidebean ? 1 : 0;
        int pos = permuters.size();

        return switch (p) {
        // A constant has been supplied by the user
        case BindingProvider.FromConstant(_, Object constant) -> {
            // Type of constant has already been checked
            yield MethodHandles.insertArguments(mh, pos, constant);
        }

        // A Supplier has been provided by the user, the result of which
        // TODO could add a LazyConstant, which will create it on first usage, Possible runtime
        case BindingProvider.FromComputedConstant(Supplier<?> supplier, _) -> {
            Object constant = supplier.get();
            // TODO check type of supplied constant
            yield MethodHandles.insertArguments(mh, pos, constant);
        }

        // The value is taken directly from an argument provided by the invoking extension
        case BindingProvider.FromInvocationArgument(int argumentIndex) -> {
            permuters.add(extensionIndex + argumentIndex);
            yield mh;
        }

        // The value is the result of calling an embedded operation
        case BindingProvider.FromEmbeddedOperation(OperationSetup operation) -> {
            MethodHandle embeddedOperation = operation.codeHolder.generateMethodHandle();
            for (int j = 0; j < embeddedOperation.type().parameterCount(); j++) {
                permuters.add(j);
            }
            yield MethodHandles.collectArguments(mh, pos, embeddedOperation);
        }

        case BindingProvider.FromLifetimeArena(_, LifetimeStoreIndex index, Class<?> type) -> {
            // read from constant pool via extIdx
            permuters.add(extensionIndex);
            MethodHandle tmp = MethodHandles.insertArguments(ServiceHelper.MH_CONSTANT_POOL_READER, 1, index.index);
            assert tmp.type().returnType() == Object.class;
            tmp = tmp.asType(tmp.type().changeReturnType(type));
            yield MethodHandles.collectArguments(mh, extensionIndex, tmp);
        }

        case BindingProvider.FromSidebeanLifetimeArena(Class<?> type) -> {
            // If this should mirror FromLifetimeArena for sidebeans, adapt here.
            throw new UnsupportedOperationException("FromSidebeanLifetimeArena not implemented for type " + type);
        }
        };
    }
}
