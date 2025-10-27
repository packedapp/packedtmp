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

import app.packed.bean.lifecycle.DependantOrder;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.InitializeOperationConfiguration;
import app.packed.bean.lifecycle.InitializeOperationMirror;
import app.packed.bean.lifecycle.Inject;
import app.packed.bean.lifecycle.InjectOperationConfiguration;
import app.packed.bean.lifecycle.InjectOperationMirror;
import app.packed.bean.lifecycle.OnStart;
import app.packed.bean.lifecycle.OnStartContext;
import app.packed.bean.lifecycle.StartOperationConfiguration;
import app.packed.bean.lifecycle.StartOperationMirror;
import app.packed.bean.lifecycle.Stop;
import app.packed.bean.lifecycle.StopContext;
import app.packed.bean.lifecycle.StopOperationConfiguration;
import app.packed.bean.lifecycle.StopOperationMirror;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.context.ContextTemplate;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTemplate;

/** A handle for the a lifecycle operation on bean. */
public abstract sealed class BeanLifecycleOperationHandle extends OperationHandle<OperationConfiguration> implements Comparable<BeanLifecycleOperationHandle> {

    public final InternalBeanLifecycleKind lifecycleKind;

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

    public static byte complement(byte b) {
        return (byte) ~b;
    }

    /** A handle for initialization lifecycle operation. */
    public static final class BeanInitializeOperationHandle extends BeanLifecycleOperationHandle {

        /** An operation template for {@link Inject} and {@link Initialize}. */
        private static final OperationTemplate OPERATION_LIFECYCLE_TEMPLATE = OperationTemplate.builder().returnIgnore().build();

        /**
         * @param installer
         */
        public BeanInitializeOperationHandle(OperationInstaller installer, InternalBeanLifecycleKind lifecycleKind) {
            super(installer, lifecycleKind);
        }

        @Override
        protected InitializeOperationConfiguration newOperationConfiguration() {
            return new InitializeOperationConfiguration(this);
        }

        @Override
        protected InitializeOperationMirror newOperationMirror() {
            return new InitializeOperationMirror(this);
        }

        public static void fromInitializeAnnotation(Initialize annotation, BeanIntrospector.OnMethod method) {
            InternalBeanLifecycleKind lk = annotation.order() == DependantOrder.RUN_BEFORE_DEPENDANTS ? InternalBeanLifecycleKind.INITIALIZE_PRE_ORDER
                    : InternalBeanLifecycleKind.INITIALIZE_POST_ORDER;
            method.newOperation(OPERATION_LIFECYCLE_TEMPLATE).install(i -> new BeanInitializeOperationHandle(i, lk));
        }
    }

    public static final class BeanInjectOperationHandle extends BeanLifecycleOperationHandle {

        /** An operation template for {@link Inject} and {@link Initialize}. */
        private static final OperationTemplate OPERATION_LIFECYCLE_TEMPLATE = OperationTemplate.builder().returnIgnore().build();

        private BeanInjectOperationHandle(OperationInstaller installer) {
            super(installer, InternalBeanLifecycleKind.INJECT);
        }

        @Override
        protected InjectOperationConfiguration newOperationConfiguration() {
            return new InjectOperationConfiguration(this);
        }

        @Override
        protected InjectOperationMirror newOperationMirror() {
            return new InjectOperationMirror(this);
        }

        public static void install(Inject annotation, BeanIntrospector.OnMethod method) {
            method.newOperation(OPERATION_LIFECYCLE_TEMPLATE).install(i -> new BeanInjectOperationHandle(i));
        }

        public static void install(Inject annotation, BeanIntrospector.OnField field) {
            // TODO we need wrap/unwrap
            field.newSetOperation(OPERATION_LIFECYCLE_TEMPLATE).install(i -> new BeanInjectOperationHandle(i));

            // checkNotStatic
            // Det er jo inject service!???
            // field.newBindableVariable().unwrap();
            // OperationHandle handle = field.newSetOperation(null) .newOperation(temp);
            // bean.lifecycle.addInitialize(handle, null);
            throw new UnsupportedOperationException();
        }

        public static void fromInitializeAnnotation(Initialize annotation, BeanIntrospector.OnMethod method) {
            InternalBeanLifecycleKind lk = annotation.order() == DependantOrder.RUN_BEFORE_DEPENDANTS ? InternalBeanLifecycleKind.INITIALIZE_PRE_ORDER
                    : InternalBeanLifecycleKind.INITIALIZE_POST_ORDER;
            method.newOperation(OPERATION_LIFECYCLE_TEMPLATE).install(i -> new BeanInitializeOperationHandle(i, lk));
        }
    }

    public static final class LifecycleOnStartHandle extends BeanLifecycleOperationHandle {

        public boolean fork;
        public boolean interruptOnStopping;

        public boolean stopOnFailure;

        private LifecycleOnStartHandle(OperationInstaller installer, OnStart annotation) {
            InternalBeanLifecycleKind lifecycleKind = annotation.order() == DependantOrder.RUN_BEFORE_DEPENDANTS ? InternalBeanLifecycleKind.START_PRE_ORDER
                    : InternalBeanLifecycleKind.START_POST_ORDER;
            this.stopOnFailure = annotation.stopOnFailure();
            this.interruptOnStopping = annotation.interruptOnStopping();
            this.fork = annotation.fork();

            super(installer, lifecycleKind);
        }

        @Override
        protected StartOperationConfiguration newOperationConfiguration() {
            return new StartOperationConfiguration(this);
        }

        @Override
        protected StartOperationMirror newOperationMirror() {
            return new StartOperationMirror(this);
        }

        /** A context template for {@link StartContext}. */
        private static final ContextTemplate CONTEXT_ON_START_TEMPLATE = ContextTemplate.of(OnStartContext.class);

        /** An operation template for {@link Start}. */
        private static final OperationTemplate OPERATION_ON_START_TEMPLATE = OperationTemplate.builder().returnIgnore().context(CONTEXT_ON_START_TEMPLATE)
                .build();

        public static void install(OnStart annotation, BeanIntrospector.OnMethod method) {
            method.newOperation(OPERATION_ON_START_TEMPLATE).install(i -> new LifecycleOnStartHandle(i, annotation));
        }
    }

    public static final class LifecycleOperationStopHandle extends BeanLifecycleOperationHandle {

        public boolean fork;

        private LifecycleOperationStopHandle(OperationInstaller installer, Stop annotation) {
            super(installer, annotation.order() == DependantOrder.RUN_BEFORE_DEPENDANTS ? InternalBeanLifecycleKind.STOP_PRE_ORDER
                    : InternalBeanLifecycleKind.STOP_POST_ORDER);
            this.fork = annotation.fork();
        }

        @Override
        protected StopOperationConfiguration newOperationConfiguration() {
            return new StopOperationConfiguration(this);
        }

        /** A context template for {@link StopContext}. */
        private static final ContextTemplate CONTEXT_ON_STOP_TEMPLATE = ContextTemplate.of(StopContext.class);

        /** An operation template for {@link Stop}. */
        private static final OperationTemplate OPERATION_ON_STOP_TEMPLATE = OperationTemplate.defaults().withReturnIgnore()
                .withContext(CONTEXT_ON_STOP_TEMPLATE);

        @Override
        protected OperationMirror newOperationMirror() {
            return new StopOperationMirror(this);
        }

        public static void install(Stop annotation, BeanIntrospector.OnMethod method) {
            method.newOperation(OPERATION_ON_STOP_TEMPLATE).install(i -> new LifecycleOperationStopHandle(i, annotation));
        }
    }
}
