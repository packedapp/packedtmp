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
import internal.app.packed.operation.OperationSetup;

/** The lifetime of an independent container. */
public final class ContainerLifetimeSetup extends LifetimeSetup {

    // All eagerly instantiated beans in order
    public final ArrayList<BeanSetup> beans = new ArrayList<>();

    /** Any child lifetimes. */
    private List<LifetimeSetup> children;

    /** The root container of the lifetime. */
    public final ContainerSetup container;

    ArrayList<OperationSetup> initialize = new ArrayList<>();

    ArrayList<MethodHandle> initializeMh = new ArrayList<>();

    // Skal kopieres ind i internal lifetime launcher
    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    LinkedHashSet<BeanSetup> orderedBeans = new LinkedHashSet<>();

    /** The lifetime constant pool. */
    public final LifetimeObjectArenaSetup pool = new LifetimeObjectArenaSetup();
    ArrayList<LifetimeOperation> start = new ArrayList<>();
    ArrayList<MethodHandle> startMh = new ArrayList<>();

    ArrayList<LifetimeOperation> stop = new ArrayList<>();

    ArrayList<MethodHandle> stopMh = new ArrayList<>();

    /**
     * @param origin
     * @param parent
     */
    public ContainerLifetimeSetup(ContainerSetup container, @Nullable ContainerLifetimeSetup parent) {
        super(parent, List.of(OperationTemplate.defaults()));
        this.container = container;
    }

    public LifetimeSetup addChild(ContainerSetup component) {
        LifetimeSetup l = new ContainerLifetimeSetup(component, this);
        return addChild(l);
    }

    public LifetimeSetup addChild(LifetimeSetup lifetime) {
        if (children == null) {
            children = new ArrayList<>(1);
        }
        children.add(lifetime);
        return lifetime;
    }

    public void codegen() {
        for (BeanSetup bs : beans) {
            orderBeans(bs);
        }
        // generate MH
    }

    public ContainerLifetimeMirror mirror() {
        return (ContainerLifetimeMirror) super.mirror();
    }

    /** {@inheritDoc} */
    @Override
    LifetimeMirror mirror0() {
        return new ContainerLifetimeMirror();
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

    // Should be fully resolved now
    public void processBean(BeanSetup bs) {
        if (bs.beanKind == BeanKind.CONTAINER || bs.beanKind == BeanKind.LAZY) {
            if (bs.sourceKind != BeanSourceKind.INSTANCE) {
                initialize.add(bs.operations.get(0));
                initializeMh.add(bs.operations.get(0).generateMethodHandle());
            }
        }

        for (LifetimeOperation lop : bs.operationsLifetime) {
            if (lop.state() == RunState.INITIALIZING) {
                initialize.add(lop.os());
                initializeMh.add(lop.os().generateMethodHandle());
            } else if (lop.state() == RunState.STARTING) {
                start.add(lop);
                startMh.add(lop.os().generateMethodHandle());
            } else if (lop.state() == RunState.STOPPING) {
                stop.add(lop);
                stopMh.add(lop.os().generateMethodHandle());
            } else {
                throw new Error();
            }
        }
    }
}
