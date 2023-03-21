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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.extension.ContainerContext;
import app.packed.lifetime.ContainerLifetimeMirror;
import app.packed.lifetime.LifetimeKind;
import app.packed.lifetime.RunState;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanLifecycleOperation;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedContainerBuilder;
import internal.app.packed.entrypoint.OldContainerEntryPointManager;
import internal.app.packed.lifetime.runtime.PackedContainerContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import sandbox.extension.operation.OperationTemplate;
import sandbox.lifetime.ManagedLifetime;

/**
 * The lifetime of the root container in an application. Or a container whose lifetime is independent of its parent
 * container.
 */
public final class ContainerLifetimeSetup extends AbstractTreeNode<ContainerLifetimeSetup> implements LifetimeSetup {

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    private static final MethodHandle MH_INVOKE_INITIALIZER = LookupUtil.findStaticOwn(MethodHandles.lookup(), "invokeInitializer", void.class, BeanSetup.class,
            MethodHandle.class, ContainerContext.class);

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    private static final MethodHandle MH_CONTAINER_LIFETIME_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), ContainerLifetimeMirror.class,
            "initialize", void.class, ContainerLifetimeSetup.class);

    /** Beans that have independent lifetime of all the container's in this lifetime. */
    private final ArrayList<BeanLifetimeSetup> beanLifetimes = new ArrayList<>(); // what are using this for??

    /** All beans that are in this lifetime, in order of installation. */
    public final ArrayList<BeanSetup> beans = new ArrayList<>();

    /** The root container of the lifetime. */
    public final ContainerSetup container;

    /** An object that is shared between all entry point extensions in the same application. */
    public final OldContainerEntryPointManager entryPoints = new OldContainerEntryPointManager();

    public final FuseableOperation initialization;

    public final List<FuseableOperation> lifetimes;

    LinkedHashSet<BeanSetup> orderedBeans = new LinkedHashSet<>();

    public final FuseableOperation shutdown;

    /** The size of the pool. */
    int size;

    public final FuseableOperation startup;

    // Er ikke noedvendigvis fra et entrypoint, kan ogsaa vaere en completer
    public final Class<?> resultType;
    /**
     * @param origin
     * @param parent
     */
    public ContainerLifetimeSetup(PackedContainerBuilder installer, ContainerSetup newContainer, @Nullable ContainerLifetimeSetup parent) {
        super(parent);
        this.lifetimes = FuseableOperation.of(List.of(OperationTemplate.defaults())); // obviously wrong
        this.initialization = new FuseableOperation(OperationTemplate.defaults());
        this.startup = new FuseableOperation(OperationTemplate.defaults());
        this.shutdown = new FuseableOperation(OperationTemplate.defaults());

        this.container = newContainer;
        this.resultType = installer.template.resultType();

        if (newContainer.isApplicationRoot()) {
            reserve(ManagedLifetime.class);
        }
    }

    public int addBean(BeanSetup bean) {
        beans.add(bean);
        if (bean.beanKind == BeanKind.CONTAINER && bean.beanSourceKind != BeanSourceKind.INSTANCE) {
            return reserve(bean.beanClass);
        }
        return -1;
    }

    public BeanLifetimeSetup addDetachedChildBean(BeanLifetimeSetup lifetime) {
        // I don't know what we use this method for
        beanLifetimes.add(lifetime);
        return lifetime;
    }

    /** {@inheritDoc} */
    @Override
    public LifetimeKind lifetimeKind() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public List<FuseableOperation> lifetimes() {
        return lifetimes;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    @Override
    public ContainerLifetimeMirror mirror() {
        ContainerLifetimeMirror mirror = new ContainerLifetimeMirror();

        // Initialize LifetimeMirror by calling LifetimeMirror#initialize(LifetimeSetup)
        try {
            MH_CONTAINER_LIFETIME_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public ContainerContext newRuntimePool() {
        return PackedContainerContext.create(size);
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

    public void orderDependencies() {

        for (BeanSetup bs : beans) {
            orderBeans(bs);
        }
        // generate MH
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable ContainerLifetimeSetup parent() {
        return treeParent;
    }

    // Should be fully resolved now
    public void processBean(BeanSetup bean) {
        Collections.sort(bean.lifecycleOperations); // stable sort
        if (bean.beanKind == BeanKind.CONTAINER || bean.beanKind == BeanKind.LAZY) {
            if (bean.beanSourceKind != BeanSourceKind.INSTANCE) {

                // We need a factory method

                OperationSetup os = bean.operations.get(0);

                bean.container.application.addCodegenAction(() -> {
                    MethodHandle mha = os.generateMethodHandle();

                    // We store container beans in a generic object array.
                    // Don't care about the exact type of the bean.
                    mha = mha.asType(mha.type().changeReturnType(Object.class));

                    mha = MH_INVOKE_INITIALIZER.bindTo(bean).bindTo(mha);
                    initialization.methodHandles.add(mha);
                });
            }
        }

        for (BeanLifecycleOperation lop : bean.lifecycleOperations) {
            if (lop.runOrder().runState == RunState.INITIALIZING) {
                initialization.operations.add(lop.handle());
                bean.container.application.addCodegenAction(() -> {
                    MethodHandle mh = lop.handle().generateMethodHandle();
                    initialization.methodHandles.add(mh);
                });
            } else if (lop.runOrder().runState == RunState.STARTING) {
                startup.operations.add(lop.handle());
                bean.container.application.addCodegenAction(() -> {
                    MethodHandle mh = lop.handle().generateMethodHandle();
                    startup.methodHandles.add(mh);
                });
            } else if (lop.runOrder().runState == RunState.STOPPING) {
                shutdown.operations.addFirst(lop.handle());
                bean.container.application.addCodegenAction(() -> {
                    MethodHandle mh = lop.handle().generateMethodHandle();
                    shutdown.methodHandles.addFirst(mh);
                });
            } else {
                throw new Error();
            }
        }
    }

    /**
     * Reserves room for a single object.
     *
     * @return the index to store the object in at runtime
     */
    private int reserve(Class<?> cls) {
        return size++;
    }

    public static void invokeInitializer(BeanSetup bean, MethodHandle mh, ContainerContext ec) {
        Object instance;
        try {
            instance = mh.invokeExact(ec);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        if (instance == null) {
            throw new NullPointerException(" returned null");
        }
        if (!bean.beanClass.isInstance(instance)) {
            throw new Error("Expected " + bean.beanClass + ", was " + instance.getClass());
        }
        PackedContainerContext pec = (PackedContainerContext) ec;
        pec.storeObject(bean.lifetimeStoreIndex, instance);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> resultType() {
        return resultType;
    }
}

//Vi kan sagtens folde bedste foraeldre ind ogsaa...
//Altsaa bruger man kun et enkelt object kan vi jo bare folde det ind...
//[ [GrandParent][Parent], O1, O2, O3]

//Der er faktisk 2 strategier her...
//RepeatableImage -> Har vi 2 pools taenker jeg... En shared, og en per instans
//Ikke repeatable.. Kav vi lave vi noget af array'et paa forhaand... F.eks. smide
//bean instancerne ind i det

//Saa maaske er pool og Lifetime to forskellige ting???
