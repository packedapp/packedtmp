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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanSourceKind;
import app.packed.lifetime.CompositeLifetimeMirror;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.lifecycle.LifecycleOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.AbstractInitializingOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.StartOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.StopOperationHandle;
import internal.app.packed.lifecycle.lifetime.entrypoint.EntryPointManager;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.accesshelper.BeanLifetimeAccessHandler;
import sandbox.lifetime.ManagedLifetime;

/**
 * The lifetime of the root container in an application. Or a container whose lifetime is independent of its parent
 * container.
 */
public final class ContainerLifetimeSetup extends AbstractTreeNode<ContainerLifetimeSetup> implements LifetimeSetup {

    /** All beans in this container lifetime, in the order they where installed. */
    public final ArrayList<BeanSetup> beans = new ArrayList<>();

    /** The root container of the lifetime. */
    public final ContainerSetup container;

    private final LinkedHashSet<BeanSetup> dependencyOrderedBeans = new LinkedHashSet<>();

    /** An object that is shared between all entry point extensions in the same application. */
    public final EntryPointManager entryPoints = new EntryPointManager();

    public final List<AbstractInitializingOperationHandle> initializationPost = new ArrayList<>();

    public final List<IndexedOperationHandle<AbstractInitializingOperationHandle>> initializationPre = new ArrayList<>();

    // Er ikke noedvendigvis fra et entrypoint, kan ogsaa vaere en completer
    public final Class<?> resultType;

    public final List<StartOperationHandle> startersPost = new ArrayList<>();

    public final List<StartOperationHandle> startersPre = new ArrayList<>();

    public final List<StopOperationHandle> stoppersPost = new ArrayList<>();

    public final List<StopOperationHandle> stoppersPre = new ArrayList<>();

    public final LifetimeStore store = new LifetimeStore();

    /**
     * @param origin
     * @param parent
     */
    public ContainerLifetimeSetup(PackedContainerInstaller<?> installer, ContainerSetup newContainer, @Nullable ContainerLifetimeSetup parent) {
        super(parent);

        this.container = newContainer;
        this.resultType = installer.template.resultType();

        if (newContainer.isApplicationRoot()) {
            store.add(new LifetimeStoreEntry.InternalStoreEntry(ManagedLifetime.class));
        }
    }

    public LifetimeStoreIndex addBean(BeanSetup bean) {
        beans.add(bean);
        if (bean.beanKind == BeanLifetime.SINGLETON && bean.bean.beanSourceKind != BeanSourceKind.INSTANCE) {
            return store.add(bean);
        }
        return null;
    }

    public void initialize(ExtensionContext pool) {
        for (IndexedOperationHandle<AbstractInitializingOperationHandle> mh : initializationPre) {
            try {
                mh.operationHandle().methodHandle.invokeExact(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        for (AbstractInitializingOperationHandle mh : initializationPost) {
            try {
                mh.methodHandle.invokeExact(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
    }

    /** {@return a mirror that can be exposed to end-users.} */
    @Override
    public CompositeLifetimeMirror mirror() {
        return BeanLifetimeAccessHandler.instance().newRegionalLifetimeMirror(this);
    }

    public ExtensionContext newRuntimePool() {
        return store.newRuntimePool();
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
            // IO.println("Codegen " + bean.path());
            orderDependenciesBeans0(bean);
            // addFactory, that installs the bean into Object[]
            // then all initiali
        }
    }

    private void orderDependenciesBeans0(BeanSetup bean) {
        for (List<LifecycleOperationHandle> lop : bean.operations.lifecycleHandles.values()) {
            for (LifecycleOperationHandle h : lop) {
                switch (h.lifecycleKind) {
                case FACTORY, INJECT, INITIALIZE_PRE_ORDER -> initializationPre.add(new IndexedOperationHandle<>((AbstractInitializingOperationHandle) h, bean.lifetimeStoreIndex));
                case INITIALIZE_POST_ORDER -> startersPost.addFirst((StartOperationHandle) h);
                case START_PRE_ORDER -> startersPre.add((StartOperationHandle) h);
                case START_POST_ORDER -> startersPost.addFirst((StartOperationHandle) h);
                case STOP_PRE_ORDER -> stoppersPre.add((StopOperationHandle) h);
                case STOP_POST_ORDER -> stoppersPost.addFirst((StopOperationHandle) h);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable ContainerLifetimeSetup parent() {
        return treeParent;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> resultType() {
        return resultType;
    }

    // Bliver kaldt fra method handles

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
