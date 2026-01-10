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
package internal.app.packed.lifecycle;

import static java.util.Objects.requireNonNull;

import app.packed.bean.BeanIntrospector;
import app.packed.lifecycle.FactoryOperationConfiguration;
import app.packed.lifecycle.FactoryOperationMirror;
import app.packed.lifecycle.Initialize;
import app.packed.lifecycle.InitializeOperationConfiguration;
import app.packed.lifecycle.InitializeOperationMirror;
import app.packed.lifecycle.Inject;
import app.packed.lifecycle.InjectOperationConfiguration;
import app.packed.lifecycle.InjectOperationMirror;
import app.packed.lifecycle.Start;
import app.packed.lifecycle.StartContext;
import app.packed.lifecycle.StartOperationConfiguration;
import app.packed.lifecycle.StartOperationMirror;
import app.packed.lifecycle.Stop;
import app.packed.lifecycle.StopContext;
import app.packed.lifecycle.StopOperationConfiguration;
import app.packed.lifecycle.StopOperationMirror;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationMirror;
import internal.app.packed.extension.base.BaseExtensionOperationHandle;
import internal.app.packed.invoke.BeanLifecycleSupport;

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
            method.newOperation().returnIgnore().install(i -> new InitializeOperationHandle(i, lk));
        }
    }

    public static final class InjectOperationHandle extends AbstractInitializingOperationHandle {

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
            // TODO we need box/unbox
            field.newSetOperation().install(i -> new InjectOperationHandle(i));

            // checkNotStatic
            // Det er jo inject service!???
            // field.newBindableVariable().unbox();
            // OperationHandle handle = field.newSetOperation(null) .newOperation(temp);
            // bean.lifecycle.addInitialize(handle, null);
            throw new UnsupportedOperationException();
        }

        public static void install(Inject annotation, BeanIntrospector.OnMethod method) {
            method.newOperation().returnIgnore().install(i -> new InjectOperationHandle(i));
        }
    }

    public static final class StartOperationHandle extends LifecycleOperationHandle {

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
            method.newOperation().returnIgnore().addContext(StartContext.class) .install(i -> new StartOperationHandle(i, annotation));
        }
    }

    public static final class StopOperationHandle extends LifecycleOperationHandle {


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
            method.newOperation().returnIgnore().addContext(StopContext.class).install(i -> new StopOperationHandle(i, annotation));
        }
    }
}
