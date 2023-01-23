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
package internal.app.packed.lifetime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.framework.Nullable;
import app.packed.lifetime.ContainerLifetimeMirror;
import app.packed.lifetime.LifetimeMirror;
import app.packed.lifetime.RunState;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.entrypoint.EntryPointSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * The lifetime of the root container in an application. Or a container whose lifetime is independent of its parent
 * container.
 */
public final class ContainerLifetimeSetup extends AbstractTreeNode<ContainerLifetimeSetup> implements LifetimeSetup {

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    private static final MethodHandle MH_INVOKE_INITIALIZER = LookupUtil.findStaticOwn(MethodHandles.lookup(), "invokeInitializer", void.class, BeanSetup.class,
            MethodHandle.class, PackedExtensionContext.class);

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    private static final MethodHandle MH_LIFETIME_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), ContainerLifetimeMirror.class,
            "initialize", void.class, ContainerLifetimeSetup.class);

    /** Beans that have independent lifetime of the container. */
    private List<BeanLifetimeSetup> beanLifetimes;

    /** All beans that have container lifetime in order of installation. Includes both application and extension beans. */
    public final ArrayList<BeanSetup> beans = new ArrayList<>();

    /** The root container of the lifetime. */
    public final ContainerSetup container;

    /** Entry points in the application, is null if there are none. */
    @Nullable
    public EntryPointSetup entryPoints;

    public final FuseableOperation initialization;

    public final FuseableOperation startup;

    public final FuseableOperation shutdown;

    public final List<FuseableOperation> lifetimes;

    LinkedHashSet<BeanSetup> orderedBeans = new LinkedHashSet<>();

    /** The lifetime constant pool. */
    public final BeanInstancePoolSetup pool = new BeanInstancePoolSetup();


    /**
     * @param origin
     * @param parent
     */
    public ContainerLifetimeSetup(ContainerSetup container, @Nullable ContainerLifetimeSetup parent) {
        super(parent);
        this.lifetimes = FuseableOperation.of(List.of(OperationTemplate.defaults())); // obviously wrong
        this.initialization = new FuseableOperation(OperationTemplate.defaults());
        this.startup = new FuseableOperation(OperationTemplate.defaults());
        this.shutdown = new FuseableOperation(OperationTemplate.defaults());

        this.container = container;
        if (container.treeParent == null) {
            pool.reserve(PackedManagedLifetime.class);
        }
    }

    public BeanInstanceAccessor addBean(BeanSetup bean) {
        beans.add(bean);

        BeanInstanceAccessor la = null;
        if (bean.beanKind == BeanKind.CONTAINER && bean.sourceKind != BeanSourceKind.INSTANCE) {
            la = pool.reserve(bean.beanClass);
        }
        return la;
    }

    public BeanLifetimeSetup addChildBean(BeanLifetimeSetup lifetime) {
        if (beanLifetimes == null) {
            beanLifetimes = new ArrayList<>(1);
        }
        beanLifetimes.add(lifetime);
        return lifetime;
    }

    public void orderDependencies() {
        for (BeanSetup bs : beans) {
            orderBeans(bs);
        }
        // generate MH
    }

    /** {@inheritDoc} */
    @Override
    public List<FuseableOperation> lifetimes() {
        return lifetimes;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    public ContainerLifetimeMirror mirror() {
        ContainerLifetimeMirror mirror = new ContainerLifetimeMirror();

        // Initialize LifetimeMirror by calling LifetimeMirror#initialize(LifetimeSetup)
        try {
            MH_LIFETIME_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    private void orderBeans(BeanSetup bean) {
        Set<BeanSetup> dependsOn = bean.dependsOn();
        for (BeanSetup b : dependsOn) {
            orderBeans(b);
        }
        if (orderedBeans.add(bean)) {
            // System.out.println("Codegen " + bean.path());
            processBean(bean);
            // addFactory, that installs the bean into Object[]
            // then all initiali
        }
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable ContainerLifetimeSetup parent() {
        return treeParent;
    }

    // Should be fully resolved now
    public void processBean(BeanSetup bean) {
        if (bean.beanKind == BeanKind.CONTAINER || bean.beanKind == BeanKind.LAZY) {
            if (bean.sourceKind != BeanSourceKind.INSTANCE) {
                OperationSetup os = bean.operations.get(0);

                bean.container.application.addCodeGenerator(() -> {
                    MethodHandle mha = os.generateMethodHandle();
                    mha = MH_INVOKE_INITIALIZER.bindTo(bean).bindTo(mha);
                    initialization.methodHandles.add(mha);
                });
            }
        }

        for (LifetimeOperation lop : bean.operationsLifetime) {
            if (lop.state() == RunState.INITIALIZING) {
                initialization.operations.add(lop.os());
                bean.container.application.addCodeGenerator(() -> {
                    MethodHandle mh = lop.os().generateMethodHandle();
                    initialization.methodHandles.add(mh);
                });
            } else if (lop.state() == RunState.STARTING) {
                startup.operations.add(lop.os());
                bean.container.application.addCodeGenerator(() -> {
                    MethodHandle mh = lop.os().generateMethodHandle();
                    startup.methodHandles.add(mh);
                });
            } else if (lop.state() == RunState.STOPPING) {
                shutdown.operations.addFirst(lop.os());
                bean.container.application.addCodeGenerator(() -> {
                    MethodHandle mh = lop.os().generateMethodHandle();
                    shutdown.methodHandles.addFirst(mh);
                });
            } else {
                throw new Error();
            }
        }
    }

    public static void invokeInitializer(BeanSetup bean, MethodHandle mh, PackedExtensionContext pec) {
        Object instance;
        try {
            instance = mh.invoke(pec);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        if (instance == null) {
            throw new NullPointerException(" returned null");
        }
        if (!bean.beanClass.isInstance(instance)) {
            throw new Error("Expected " + bean.beanClass + ", was " + instance.getClass());
        }
        pec.storeObject(bean.lifetimePoolAccessor.index(), instance);
    }
}
