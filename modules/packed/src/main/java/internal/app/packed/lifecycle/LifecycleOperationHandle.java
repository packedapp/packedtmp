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

import app.packed.bean.BeanIntrospector;
import app.packed.bean.lifecycle.FactoryOperationConfiguration;
import app.packed.bean.lifecycle.FactoryOperationMirror;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.InitializeOperationConfiguration;
import app.packed.bean.lifecycle.InitializeOperationMirror;
import app.packed.bean.lifecycle.Inject;
import app.packed.bean.lifecycle.InjectOperationConfiguration;
import app.packed.bean.lifecycle.InjectOperationMirror;
import app.packed.bean.lifecycle.Start;
import app.packed.bean.lifecycle.StartContext;
import app.packed.bean.lifecycle.StartOperationConfiguration;
import app.packed.bean.lifecycle.StartOperationMirror;
import app.packed.bean.lifecycle.Stop;
import app.packed.bean.lifecycle.StopContext;
import app.packed.bean.lifecycle.StopOperationConfiguration;
import app.packed.bean.lifecycle.StopOperationMirror;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationMirror;
import internal.app.packed.extension.base.BaseExtensionOperationHandle;
import internal.app.packed.invoke.BeanLifecycleSupport;
import internal.app.packed.operation.PackedOperationTemplate;

/** An operation handle for lifecycle operation on a bean. */
public abstract sealed class LifecycleOperationHandle extends BaseExtensionOperationHandle<OperationConfiguration>
        implements Comparable<LifecycleOperationHandle> {

    public final PackedBeanLifecycleKind lifecycleKind;

    /**
     * @param installer
     */
    private LifecycleOperationHandle(OperationInstaller installer, PackedBeanLifecycleKind lifecycleKind) {
        super(installer);
        this.lifecycleKind = requireNonNull(lifecycleKind);
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(LifecycleOperationHandle entry) {
        return lifecycleKind.ordinal() - entry.lifecycleKind.ordinal();
    }

    @Override
    protected void onInstall() {
        InvokableLifecycleOperationHandle<LifecycleOperationHandle> soh = new InvokableLifecycleOperationHandle<>(this, null);
        bean().operations.addLifecycleHandle(soh);
        BeanLifecycleSupport.addLifecycleHandle(soh);
    }

    public static byte complement(byte b) {
        return (byte) ~b;
    }

    public static abstract non-sealed class AbstractInitializingOperationHandle extends LifecycleOperationHandle {
        private AbstractInitializingOperationHandle(OperationInstaller installer, PackedBeanLifecycleKind lifecycleKind) {
            super(installer, lifecycleKind);
        }
    }

    /** A handle for initialization lifecycle operation. */
    public static final class FactoryOperationHandle extends AbstractInitializingOperationHandle {

        public FactoryOperationHandle(OperationInstaller installer) {
            super(installer, PackedBeanLifecycleKind.FACTORY);
        }

        @Override
        protected FactoryOperationConfiguration newOperationConfiguration() {
            return new FactoryOperationConfiguration(this);
        }

        @Override
        protected FactoryOperationMirror newOperationMirror() {
            return new FactoryOperationMirror(this);
        }
    }

    /** A handle for initialization lifecycle operation. */
    public static final class InitializeOperationHandle extends AbstractInitializingOperationHandle {

        /** An operation template for {@link Inject} and {@link Initialize}. */
        private static final PackedOperationTemplate OPERATION_LIFECYCLE_TEMPLATE = PackedOperationTemplate.builder().returnIgnore().build();

        /**
         * @param installer
         */
        private InitializeOperationHandle(OperationInstaller installer, PackedBeanLifecycleKind lifecycleKind) {
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

        public static void install(Initialize annotation, BeanIntrospector.OnMethod method) {
            PackedBeanLifecycleKind lk = annotation.naturalOrder() ? PackedBeanLifecycleKind.INITIALIZE_PRE_ORDER
                    : PackedBeanLifecycleKind.INITIALIZE_POST_ORDER;
            method.newOperation(OPERATION_LIFECYCLE_TEMPLATE).install(i -> new InitializeOperationHandle(i, lk));
        }
    }

    public static final class InjectOperationHandle extends AbstractInitializingOperationHandle {

        /** An operation template for {@link Inject} and {@link Initialize}. */
        private static final PackedOperationTemplate OPERATION_LIFECYCLE_TEMPLATE = PackedOperationTemplate.builder().returnIgnore().build();

        private InjectOperationHandle(OperationInstaller installer) {
            super(installer, PackedBeanLifecycleKind.INJECT);
        }

        @Override
        protected InjectOperationConfiguration newOperationConfiguration() {
            return new InjectOperationConfiguration(this);
        }

        @Override
        protected InjectOperationMirror newOperationMirror() {
            return new InjectOperationMirror(this);
        }

        public static void install(Inject annotation, BeanIntrospector.OnField field) {
            // TODO we need wrap/unwrap
            field.newSetOperation(OPERATION_LIFECYCLE_TEMPLATE).install(i -> new InjectOperationHandle(i));

            // checkNotStatic
            // Det er jo inject service!???
            // field.newBindableVariable().unwrap();
            // OperationHandle handle = field.newSetOperation(null) .newOperation(temp);
            // bean.lifecycle.addInitialize(handle, null);
            throw new UnsupportedOperationException();
        }

        public static void install(Inject annotation, BeanIntrospector.OnMethod method) {
            method.newOperation(OPERATION_LIFECYCLE_TEMPLATE).install(i -> new InjectOperationHandle(i));
        }
    }

    public static final class StartOperationHandle extends LifecycleOperationHandle {

        /** An operation template for {@link Start}. */
        private static final PackedOperationTemplate OPERATION_ON_START_TEMPLATE = PackedOperationTemplate.builder().returnIgnore().context(StartContext.class)
                .build();

        public boolean fork;

        public boolean interruptOnStopping;

        public boolean stopOnFailure;

        private StartOperationHandle(OperationInstaller installer, Start annotation) {
            PackedBeanLifecycleKind lifecycleKind = annotation.naturalOrder() ? PackedBeanLifecycleKind.START_PRE_ORDER
                    : PackedBeanLifecycleKind.START_POST_ORDER;
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

        public static void install(Start annotation, BeanIntrospector.OnMethod method) {
            method.newOperation(OPERATION_ON_START_TEMPLATE).install(i -> new StartOperationHandle(i, annotation));
        }
    }

    public static final class StopOperationHandle extends LifecycleOperationHandle {


        /** An operation template for {@link Stop}. */
        private static final PackedOperationTemplate OPERATION_ON_STOP_TEMPLATE = PackedOperationTemplate.DEFAULTS.withReturnIgnore()
                .withContext(StopContext.class);

        public boolean fork;

        private StopOperationHandle(OperationInstaller installer, Stop annotation) {
            super(installer, annotation.naturalOrder() ? PackedBeanLifecycleKind.STOP_POST_ORDER : PackedBeanLifecycleKind.STOP_PRE_ORDER);
            this.fork = annotation.fork();
        }

        @Override
        protected StopOperationConfiguration newOperationConfiguration() {
            return new StopOperationConfiguration(this);
        }

        @Override
        protected OperationMirror newOperationMirror() {
            return new StopOperationMirror(this);
        }

        public static void install(Stop annotation, BeanIntrospector.OnMethod method) {
            method.newOperation(OPERATION_ON_STOP_TEMPLATE).install(i -> new StopOperationHandle(i, annotation));
        }
    }
}
