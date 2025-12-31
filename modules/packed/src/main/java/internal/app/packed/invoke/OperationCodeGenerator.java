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
import java.util.Arrays;
import java.util.function.Supplier;

import app.packed.bean.BeanLifetime;
import app.packed.binding.Key;
import app.packed.binding.ProvisionException;
import app.packed.util.Nullable;
import internal.app.packed.bean.sidebean.PackedSidebeanAttachment;
import internal.app.packed.bean.sidebean.PackedSidebeanAttachment.OfOperation;
import internal.app.packed.bean.sidebean.PackedSidebeanBinding;
import internal.app.packed.bean.sidebean.PackedSidebeanBinding.Constant;
import internal.app.packed.bean.sidebean.PackedSidebeanBinding.Invoker;
import internal.app.packed.bean.sidebean.SidebeanHandle;
import internal.app.packed.bean.sidebean.SidebeanInvoker;
import internal.app.packed.binding.BindingProvider;
import internal.app.packed.binding.BindingProvider.FromSidebeanAttachment;
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

    /** The invocation type of the method handle being generated. */
    private MethodType invocationType;

    /** The operation we are generating a method handle for */
    private final OperationSetup operation;

    @Nullable
    public final PackedSidebeanAttachment sidebeanAttachment;

    private final Supplier<MethodHandle> cachedMH = StableValue.supplier(() -> newMethodHandle());

    /**
     * @param packedSideBeanUsage
     */
    public OperationCodeGenerator(OperationSetup operation, @Nullable PackedSidebeanAttachment sidebean) {
        this.operation = operation;
        this.sidebeanAttachment = sidebean;
        this.invocationType = operation.template.invocationType();
    }

    boolean isDebug() {
        return false;
//        if (false) {
//            return operation.bean.bean.beanClass == HowDoesThisWork.class || operation.bean.bean.beanClass == HowDoesThisWorkWithParam.class;
//        }
//        return (operation.bean.bean.beanClass == DaemonJobSidebean.class || operation.bean.bean.beanClass == DaemonJobSidebeanWithManager.class)
//
//                && operation.handle() instanceof FactoryOperationHandle;
    }

    /**
     * Creates an invoker as a method handle.
     * <p>
     * This method returns a method handle that can be used to invoke the underlying operation directly. The returned method
     * handle will have a type matching {@link #invokerType()}.
     *
     * @return a method handle for invoking the underlying operation
     */

    MethodHandle generate(boolean lazy) {
        if (lazy) {
            LazyResolvable lazyMH = cachedLazyMethodHandle;
            if (lazyMH == null) {
                lazyMH = cachedLazyMethodHandle = MethodHandleUtil.lazyF(operation.template.methodType, cachedMH);
            }
            return lazyMH.handle();
        } else {
            return generateMethodHandle();
        }
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
    private MethodHandle generateMethodHandle() {
        return cachedMH.get();
    }

    private MethodHandle newMethodHandle() {
        operation.bean.container.application.checkInCodegenPhase();

        // If sidebeanAttachment we use special method. The special thing about sidebean attachments is that the sidebean itself
        // has already had its operations through this method. But parameters that use SidebeanBinding will have been left
        // unresolved for each attachment. And must there be bound individually for each attachment, using this special method
        if (sidebeanAttachment != null) {
            return newSidebeanAttachment();
        }

        boolean isFactory = operation.handle() instanceof FactoryOperationHandle;
        boolean requiresBeanInstance = !isFactory && operation.target instanceof MemberOperationTarget mot && !Modifier.isStatic(mot.target.modifiers());
        boolean isSideBeanClass = operation.bean.handle() instanceof SidebeanHandle;

        ArrayList<Integer> permuters = new ArrayList<>();

        MethodHandle mh = operation.target.methodHandle();

        if (requiresBeanInstance) {
            if (isSideBeanClass) {
                // If we are a sidebean, the generated method handle, must take a bean instance as the first parameter.
                // This parameter will be bound later at the sidebean usage site, (typically to lifetime array reader of the bean)
                invocationType = invocationType.insertParameterTypes(0, operation.bean.bean.beanClass);
                permuters.add(0);
            } else {
                // Otherwise we fetch the binding provider that can get the bean instance to use.
                BindingProvider beanInstanceProvider = operation.bean.beanInstanceBindingProvider();
                mh = provide(mh, 0, beanInstanceProvider, permuters);
            }
        }

        // Embedded operations normally needs to return a value
        if (operation.embeddedInto != null) {
            invocationType = invocationType.changeReturnType(operation.type.returnRawType());
        }

        if (isDebug()) {
            System.out.println("==========");
            System.out.println("Invoked as: " + invocationType);
            System.out.println("Method    : " + operation.type.toMethodType());
            System.out.println();
        }

        for (int i = 0; i < operation.bindings.length; i++) {
            BindingSetup binding = operation.bindings[i];
            if (binding == null || binding.provider() == null) {
                // Should not really be here where we fail, much earlier
                System.out.println(operation.type);
                System.out.println(Arrays.toString(operation.bindings));
                throw new ProvisionException("Oops for " + operation.type);
            }
            mh = provide(mh, i, binding.provider(), permuters);
        }

        int[] result = new int[permuters.size()];
        for (int i = 0; i < permuters.size(); i++) {
            result[i] = permuters.get(i);
        }

        if (Arrays.equals(result, new int[] { 0, 2, 3 })) {
            System.out.println("MODIFYIN®†");
            result = new int[] { 0, 1, 2 };
        }

        if (isDebug()) {
            System.out.println("Invoked as: " + invocationType);
            System.out.println("Target    : " + mh.type());
            System.out.println("Permuters : " + Arrays.toString(result) + " was " + permuters);
        }

        try {
            mh = MethodHandles.permuteArguments(mh, invocationType, result);
        } catch (RuntimeException e) {
            System.err.println("Invoked as: " + invocationType);
            System.err.println("Target    : " + mh.type());
            System.err.println("Permuter array " + permuters);
            throw e;
        }

        if (isDebug()) {
            System.out.println("Final     : " + mh.type());
            System.out.println("==========");
        }

        if (mh.type() != invocationType) {
            System.err.println("OperationType " + operation.type.toString());
            System.err.println("Expected " + operation.template.methodType);
            System.err.println("Actual " + mh.type());
            throw new Error();
        }

        // Singleton beans needs to be stored in their respective lifetime.
        boolean isFactoryStore = isFactory && operation.bean.beanKind == BeanLifetime.SINGLETON;
        if (isFactoryStore) {
            mh = mh.asType(mh.type().changeReturnType(Object.class)); // We store in Object[]
            mh = BeanLifecycleSupport.MH_INVOKE_INITIALIZER.bindTo(operation.bean).bindTo(mh);
            // Do we need to change the type back again????
            // Maybe
        }
        return mh;
    }

    private MethodHandle newSidebeanAttachment() {
        boolean isFactory = operation.handle() instanceof FactoryOperationHandle;

        // Get the incomplete MethodHandle that each sidebean attachment needs to adjust
        MethodHandle methodHandle = operation.codeHolder.generateMethodHandle();
        if (isFactory) {
            // System.out.println(" .... " + isFactory + " " + methodHandle.type());
            for (BindingSetup binding : operation.bindings) {
                if (binding.provider() instanceof FromSidebeanAttachment a) {
                    Key<?> key = a.key();
                    PackedSidebeanBinding b = a.handle().bindings.get(key); // Existance has been checked
                    if (b instanceof Constant _) {
                        Object instance = sidebeanAttachment.constants.get(key);
                        methodHandle = MethodHandles.insertArguments(methodHandle, 1, instance);
                    } else if (b instanceof Invoker invokerType) {
                        PackedSidebeanAttachment.OfOperation oo = (OfOperation) sidebeanAttachment;
                        MethodHandle methodHandle2 = oo.operation.codeHolder.generate(false);
                        MethodHandle mhh = SidebeanInvoker.generateInvoker(invokerType.invokerType());
                        mhh = mhh.bindTo(methodHandle2);
                        methodHandle = MethodHandles.collectArguments(methodHandle, 1, mhh);
                        MethodType finalType = methodHandle.type().dropParameterTypes(1, 2);
                        int[] reorder = new int[methodHandle.type().parameterCount()];
                        for (int i = 0, j = 0; i < reorder.length; i++) {
                            if (i == 1) {
                                reorder[i] = 0; // The provider's input (now at 'pos') comes from final param 0
                            } else {
                                reorder[i] = j++; // Other params map 1-to-1 to the remaining final params
                            }
                        }
                        methodHandle = MethodHandles.permuteArguments(methodHandle, finalType, reorder);
                    }
                }
            }
            methodHandle = methodHandle.asType(methodHandle.type().changeReturnType(Object.class));
            methodHandle = BeanLifecycleSupport.MH_INVOKE_INITIALIZER_SIDEBEAN.bindTo(sidebeanAttachment).bindTo(methodHandle);
            return methodHandle;
        }
        MethodHandle tmp = MethodHandles.insertArguments(ServiceHelper.MH_CONSTANT_POOL_READER, 1, sidebeanAttachment.lifetimeStoreIndex.index);
        assert tmp.type().returnType() == Object.class;
        // We need to convert it from Object to the expected type
        tmp = tmp.asType(tmp.type().changeReturnType(sidebeanAttachment.sidebean.bean.beanClass));

        return MethodHandleUtil.merge(methodHandle, tmp);
    }

    private MethodHandle provide(MethodHandle mh, int bindingIndex, BindingProvider p, ArrayList<Integer> permuters) {
        int extensionIndex = 0;
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
            MethodHandle beanFetcher = MethodHandles.insertArguments(ServiceHelper.MH_CONSTANT_POOL_READER, 1, index.index);
            assert beanFetcher.type().returnType() == Object.class;
            beanFetcher = beanFetcher.asType(beanFetcher.type().changeReturnType(type));
            yield MethodHandles.collectArguments(mh, extensionIndex, beanFetcher);
        }

        case BindingProvider.FromSidebeanAttachment(Key<?> _, SidebeanHandle<?> _) -> {
            extensionIndex = 1;
            // Shared constant can be bound immediately, otherwise we need to bind the actual for the attachment
            // at a later timer
            Class<?> clazz = mh.type().parameterType(extensionIndex + pos - 1);
            invocationType = invocationType.appendParameterTypes(clazz);
            // System.out.println("Addingp permuter" + (bindingIndex + 1));
            permuters.add(pos + 1);
            yield mh;
        }
        };
    }
}
