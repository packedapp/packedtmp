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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.operation.Op;
import internal.app.packed.bean.PackedBeanHandle.InstallerOption.CustomIntrospector;
import internal.app.packed.bean.PackedBeanHandle.InstallerOption.CustomPrefix;
import internal.app.packed.bean.PackedBeanHandle.InstallerOption.NonUnique;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.operation.op.PackedOp;

/**
 * Implementation of {@link BeanHandle}.
 * 
 * @apiNote we could let {@link BeanSetup} implement {@link BeanHandle}, but we choose not to, to avoid parameterizing
 *          {@link BeanSetup}.
 */
public /* primitive */ record PackedBeanHandle<T> (BeanSetup bean) implements BeanHandle<T> {

    /** {@inheritDoc} */
    @Override
    public PackedBeanHandle<T> onWireRun(Runnable action) {
        requireNonNull(action, "action is null");
        Runnable w = bean.onWiringAction;
        if (w == null) {
            bean.onWiringAction = action;
        } else {
            bean.onWiringAction = () -> {
                w.run();
                action.run();
            };
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> beanClass() {
        return bean.beanClass();
    }

    /** {@inheritDoc} */
    @Override
    public BeanKind beanKind() {
        return bean.props.kind();
    }

    /** {@inheritDoc} */
    @Override
    public void decorateInstance(Function<? super T, ? extends T> decorator) {}

    /** {@inheritDoc} */
    @Override
    public void peekInstance(Consumer<? super T> consumer) {
        // check sourceKind!=INSTANCE
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> defaultKey() {
        if (beanClass() == void.class) {
            throw new UnsupportedOperationException("Keys are not support for void bean classes");
        }
        return Key.of(beanClass());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return !bean.realm.isClosed();
    }

    /** {@inheritDoc} */
    @Override
    public void specializeMirror(Supplier<? extends BeanMirror> mirrorFactory) {
        throw new UnsupportedOperationException();
    }

    private static <T> BeanHandle<T> install(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, Class<?> beanClass,
            BeanKind kind, BeanSourceKind sourceKind, @Nullable Object source, BeanHandle.Option... options) {
        boolean nonUnique = false;
        BeanIntrospector customIntrospector = null;
        String namePrefix = null;
        BeanClassModel beanModel = sourceKind == BeanSourceKind.NONE ? null : new BeanClassModel(beanClass);
        requireNonNull(options, "options is null");
        for (Option o : options) {
            requireNonNull(o, "option was null");
            InstallerOption io = (InstallerOption) o;
            io.validate(kind);
            if (io instanceof InstallerOption.CustomIntrospector ci) {
                customIntrospector = ci.introspector();
            } else if (io instanceof InstallerOption.CustomPrefix cp) {
                namePrefix = cp.prefix();
            } else {
                nonUnique = true;
            }

        }
        
        BeanProps bp = new BeanProps(kind, beanClass, sourceKind, source, beanModel, operator, realm, extensionOwner, namePrefix,
                nonUnique);

        realm.wireCurrentComponent();

        BeanSetup bean = new BeanSetup(realm, bp);

        // bean.initName

        // Scan the bean class for annotations unless the bean class is void or scanning is disabled
        if (sourceKind != BeanSourceKind.NONE) {
            new Introspector(bean, customIntrospector).introspect();
        }

        return new PackedBeanHandle<>(bean);
    }

    public static <T> BeanHandle<T> installClass(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind kind,
            Class<T> clazz, BeanHandle.Option... options) {
        requireNonNull(clazz, "clazz is null");
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys, eller void, eller xyz
        return install(operator, realm, extensionOwner, clazz, kind, BeanSourceKind.CLASS, clazz, options);
    }

    public static BeanHandle<?> installFunctional(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner,
            BeanHandle.Option... options) {
        return install(operator, realm, extensionOwner, void.class, BeanKind.FUNCTIONAL, BeanSourceKind.NONE, null, options);
    }

    public static <T> BeanHandle<T> installInstance(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, T instance,
            BeanHandle.Option... options) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Op.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }

        // Optional is also not valid
        // or Provider, Lazy, ect
        // Ved heller ikke DependencyProvided beans

        // TODO check kind
        // cannot be operation, managed or unmanaged, Functional
        return install(operator, realm, extensionOwner, instance.getClass(), BeanKind.CONTAINER, BeanSourceKind.INSTANCE, instance, options);
    }

    public static <T> BeanHandle<T> installOp(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind kind, Op<T> op,
            BeanHandle.Option... options) {
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys
        PackedOp<T> pop = PackedOp.crack(op);
        return install(operator, realm, extensionOwner, pop.type().returnType(), kind, BeanSourceKind.OP, pop, options);
    }

    // Eclipse requires permits here.. Compiler bug?
    public sealed interface InstallerOption extends BeanHandle.Option permits NonUnique, CustomIntrospector, CustomPrefix {

        static final InstallerOption NON_UNIQUE = new NonUnique();

        default void validate(BeanKind kind) {}

        public record NonUnique() implements InstallerOption {

            /** {@inheritDoc} */
            @Override
            public void validate(BeanKind kind) {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("NonUnique cannot be used with functional beans");
                }
            }
        }

        public record CustomIntrospector(BeanIntrospector introspector) implements InstallerOption {

            public CustomIntrospector {
                requireNonNull(introspector, "introspector is null");
            }

            /** {@inheritDoc} */
            @Override
            public void validate(BeanKind kind) {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("NonUnique cannot be used with functional beans");
                }
            }
        }

        public record CustomPrefix(String prefix) implements InstallerOption {

        }
    }
}
