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
package internal.app.packed.lifecycle.lifetime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.extension.ExtensionContext;
import app.packed.lifetime.ContainerLifetimeMirror;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationInitializeHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOnStartHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle;
import internal.app.packed.lifecycle.lifetime.entrypoint.EntryPointManager;
import internal.app.packed.lifecycle.lifetime.runtime.PackedExtensionContext;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.handlers.BeanLifetimeHandlers;
import sandbox.lifetime.ManagedLifetime;

/**
 * The lifetime of the root container in an application. Or a container whose lifetime is independent of its parent
 * container.
 */
public final class ContainerLifetimeSetup extends AbstractTreeNode<ContainerLifetimeSetup> implements LifetimeSetup {

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    public static final MethodHandle MH_INVOKE_INITIALIZER = LookupUtil.findStaticOwn(MethodHandles.lookup(), "invokeInitializer", void.class, BeanSetup.class,
            MethodHandle.class, ExtensionContext.class);

    /** All beans in this container lifetime, in the order they where installed. */
    public final ArrayList<BeanSetup> beans = new ArrayList<>();

    /** The root container of the lifetime. */
    public final ContainerSetup container;

    private final LinkedHashSet<BeanSetup> dependencyOrderedBeans = new LinkedHashSet<>();

    /** An object that is shared between all entry point extensions in the same application. */
    public final EntryPointManager entryPoints = new EntryPointManager();

    public final List<LifecycleOperationInitializeHandle> initializationPost = new ArrayList<>();

    public final List<LifecycleOperationInitializeHandle> initializationPre = new ArrayList<>();


    // Er ikke noedvendigvis fra et entrypoint, kan ogsaa vaere en completer
    public final Class<?> resultType;

    /** The size of the pool. */
    int size;

    public final List<LifecycleOnStartHandle> startersPost = new ArrayList<>();

    public final List<LifecycleOnStartHandle> startersPre = new ArrayList<>();

    public final List<LifecycleOperationStopHandle> stoppersPost = new ArrayList<>();

    public final List<LifecycleOperationStopHandle> stoppersPre = new ArrayList<>();

    /**
     * @param origin
     * @param parent
     */
    public ContainerLifetimeSetup(PackedContainerInstaller<?> installer, ContainerSetup newContainer, @Nullable ContainerLifetimeSetup parent) {
        super(parent);

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

    public void initialize(ExtensionContext pool) {
        for (LifecycleOperationInitializeHandle mh : initializationPre) {
            try {
                mh.methodHandle.invokeExact(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        for (LifecycleOperationInitializeHandle mh : initializationPost) {
            try {
                mh.methodHandle.invokeExact(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
    }

    /** {@return a mirror that can be exposed to end-users.} */
    @Override
    public ContainerLifetimeMirror mirror() {
        return BeanLifetimeHandlers.newRegionalLifetimeMirror(this);
    }

    public ExtensionContext newRuntimePool() {
        return PackedExtensionContext.create(size);
    }

    public void orderDependencies() {
        for (BeanSetup bs : beans) {
            orderDependenciesBeans(bs);
        }
    }

    private void orderDependenciesBeans(BeanSetup bean) {
        Set<BeanSetup> dependsOn = bean.dependsOn();
        for (BeanSetup b : dependsOn) {
            if (b != bean) { // bug, don't think it should be here
                orderDependenciesBeans(b);
            }
        }

        if (dependencyOrderedBeans.add(bean)) {
            // System.out.println("Codegen " + bean.path());
            orderDependenciesBeans0(bean);
            // addFactory, that installs the bean into Object[]
            // then all initiali
        }
    }

    private void orderDependenciesBeans0(BeanSetup bean) {
        for (List<BeanLifecycleOperationHandle> lop : bean.operations.lifecycleHandles.values()) {
            for (BeanLifecycleOperationHandle h : lop) {
                switch (h.lifecycleKind) {
                case FACTORY, INJECT, INITIALIZE_PRE_ORDER -> initializationPre.add((LifecycleOperationInitializeHandle) h);
                case INITIALIZE_POST_ORDER -> startersPost.addFirst((LifecycleOnStartHandle) h);
                case START_PRE_ORDER -> startersPre.add((LifecycleOnStartHandle) h);
                case START_POST_ORDER -> startersPost.addFirst((LifecycleOnStartHandle) h);
                case STOP_PRE_ORDER -> stoppersPre.add((LifecycleOperationStopHandle) h);
                case STOP_POST_ORDER -> stoppersPost.addFirst((LifecycleOperationStopHandle) h);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable ContainerLifetimeSetup parent() {
        return treeParent;
    }

    /**
     * Reserves room for a single object.
     *
     * @return the index to store the object in at runtime
     */
    private int reserve(Class<?> cls) {
        return size++;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> resultType() {
        return resultType;
    }

    // Bliver kaldt fra method handles
    public static void invokeInitializer(BeanSetup bean, MethodHandle mh, ExtensionContext ec) {
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

        // Store the new bean in the context
        PackedExtensionContext pec = (PackedExtensionContext) ec;
        pec.storeObject(bean.lifetimeStoreIndex, instance);
    }
}

///** Beans that have independent lifetime of all the container's in this lifetime. */
//private final ArrayList<BeanLifetimeSetup> beanLifetimes = new ArrayList<>(); // what are using this for??

//Vi kan sagtens folde bedste foraeldre ind ogsaa...
//Altsaa bruger man kun et enkelt object kan vi jo bare folde det ind...
//[ [GrandParent][Parent], O1, O2, O3]

//Der er faktisk 2 strategier her...
//RepeatableImage -> Har vi 2 pools taenker jeg... En shared, og en per instans
//Ikke repeatable.. Kav vi lave vi noget af array'et paa forhaand... F.eks. smide
//bean instancerne ind i det

//Saa maaske er pool og Lifetime to forskellige ting???
