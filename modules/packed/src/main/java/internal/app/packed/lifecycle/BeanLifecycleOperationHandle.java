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
package internal.app.packed.lifecycle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.DependantOrder;
import app.packed.bean.lifecycle.OnStart;
import app.packed.bean.lifecycle.Stop;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTemplate;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.IntrospectorOnMethod;
import internal.app.packed.lifecycle.lifetime.ContainerLifetimeSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.handlers.BeanLifecycleHandlers;

/** A handle for the a lifecycle operation on bean. */
public abstract sealed class BeanLifecycleOperationHandle extends OperationHandle<OperationConfiguration> implements Comparable<BeanLifecycleOperationHandle> {

    public final InternalBeanLifecycleKind lifecycleKind;

    public MethodHandle methodHandle;

    /**
     * @param installer
     */
    private BeanLifecycleOperationHandle(OperationInstaller installer, InternalBeanLifecycleKind lifecycleKind) {
        super(installer);
        this.lifecycleKind = requireNonNull(lifecycleKind);
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(BeanLifecycleOperationHandle entry) {
        return lifecycleKind.ordinal() - entry.lifecycleKind.ordinal();
    }

    public void setMethodHandle(MethodHandle mh) {
        if (methodHandle != null) {
            throw new IllegalStateException();
        }
        this.methodHandle = mh;
    }

    public static byte complement(byte b) {
        return (byte) ~b;
    }

    /** A handle for initialization lifecycle operation. */
    public static final class ForInitialize extends BeanLifecycleOperationHandle {

        ForInitialize(OperationInstaller installer, @Nullable Initialize annotation) {
            super(installer, annotation.order() == DependantOrder.RUN_BEFORE_DEPENDANTS ? InternalBeanLifecycleKind.INITIALIZE_PRE_ORDER
                    : InternalBeanLifecycleKind.INITIALIZE_POST_ORDER);
        }

        /** An operation template for {@link Inject} and {@link Initialize}. */
        private static final OperationTemplate OPERATION_LIFECYCLE_TEMPLATE = OperationTemplate.defaults().withReturnIgnore();

        public static void from(Initialize annotation, IntrospectorOnMethod method) {
            BeanSetup bean = method.bean();

//            checkNotStaticBean(bean, Initialize.class);
            ForInitialize handle = method.newOperation(OPERATION_LIFECYCLE_TEMPLATE)
                    .install(i -> new ForInitialize(i, annotation));
            bean.operations.addLifecycleHandle(handle);
        }

        /**
         * @param installer
         */
        public ForInitialize(OperationInstaller installer, InternalBeanLifecycleKind lifecycleKind) {
            super(installer, lifecycleKind);
        }

        @Override
        protected OperationConfiguration newOperationConfiguration() {
            return BeanLifecycleHandlers.newInitializeOperationConfiguration(this);
        }

        @Override
        protected OperationMirror newOperationMirror() {
            return BeanLifecycleHandlers.newInitializeOperationMirror(this);
        }

        @Override
        public MethodHandle newMethodHandle() {
            if (lifecycleKind == InternalBeanLifecycleKind.FACTORY) {
                OperationSetup os = OperationSetup.crack(this);
                BeanSetup bean = os.bean;
                if (bean.beanKind == BeanKind.CONTAINER || bean.beanKind == BeanKind.LAZY) {
                    assert (bean.bean.beanSourceKind != BeanSourceKind.INSTANCE);
                    MethodHandle mha = super.newMethodHandle();

                    // We store container beans in a generic object array.
                    // Don't care about the exact type of the bean.
                    mha = mha.asType(mha.type().changeReturnType(Object.class));

                    // Vil faktisk gemme den førends sidste
                    mha = ContainerLifetimeSetup.MH_INVOKE_INITIALIZER.bindTo(bean).bindTo(mha);
                    return mha;
                }
            }
            return super.newMethodHandle();
        }

    }

    public static final class LifecycleOnStartHandle extends BeanLifecycleOperationHandle {

        public boolean fork;
        public boolean interruptOnStopping;

        public boolean stopOnFailure;

        /**
         * @param installer
         */
        LifecycleOnStartHandle(OperationInstaller installer, OnStart annotation) {
            super(installer, annotation.order() == DependantOrder.RUN_BEFORE_DEPENDANTS ? InternalBeanLifecycleKind.START_PRE_ORDER
                    : InternalBeanLifecycleKind.START_POST_ORDER);
            this.stopOnFailure = annotation.stopOnFailure();
            this.interruptOnStopping = annotation.interruptOnStopping();
            this.fork = annotation.fork();
        }

        @Override
        protected OperationConfiguration newOperationConfiguration() {
            return BeanLifecycleHandlers.newStartOperationConfiguration(this);
        }

        @Override
        protected OperationMirror newOperationMirror() {
            return BeanLifecycleHandlers.newStartOperationMirror(this);
        }
    }

    public static final class LifecycleOperationStopHandle extends BeanLifecycleOperationHandle {

        public boolean fork;

        /**
         * @param installer
         */
        LifecycleOperationStopHandle(OperationInstaller installer, Stop annotation) {
            super(installer, annotation.order() == DependantOrder.RUN_BEFORE_DEPENDANTS ? InternalBeanLifecycleKind.STOP_PRE_ORDER
                    : InternalBeanLifecycleKind.STOP_POST_ORDER);
            this.fork = annotation.fork();
        }

        @Override
        protected OperationConfiguration newOperationConfiguration() {
            return BeanLifecycleHandlers.newStopOperationConfiguration(this);
        }

        @Override
        protected OperationMirror newOperationMirror() {
            return BeanLifecycleHandlers.newStopOperationMirror(this);
        }
    }
}
