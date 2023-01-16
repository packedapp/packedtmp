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
package internal.app.packed.application;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.framework.Nullable;
import app.packed.operation.OperationHandle;
import internal.app.packed.jfr.CodegenEvent;
import internal.app.packed.lifetime.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.lifetime.sandbox.PackedManagedLifetime;
import internal.app.packed.lifetime.sandbox2.OldLifetimeKind;

/**
 * This class is responsible for creating an application launcher that can be used to create application instances.
 */
// Alt bliver koert recursivt... Der er ingen order

// Vi supportere de her ting tror
//// * Extension.registerCodeGenerator(Runnable r); 

// Den her IBC kan du injecte en supplier// Altid en service binding... (contextulized intialization)

//// * ?.registerCodegenResource(IBC<?> ibc, Class, Supplier<?>>);
////* ?.registerCodegenResource(IBC<?> ibc, Key, Supplier<?>>);

// Strategies
//// * Single MethodHandle from a single OperationHandle
//// * MethodHandle[] from multiple operation handles 
//// * X by combining multiple operation handles. (Kan man selv lave via regCodRes)
//// * Classifier

public final class ApplicationCodeGenerator {

    /** A list of actions that will be executed doing the code generating phase. */
    final ArrayList<Runnable> actions = new ArrayList<>();

    private final ApplicationSetup application;
    private final HashMap<InstanceBeanConfiguration<?>, MethodHandleArray> arrayInvokers = new HashMap<>();

    /** The application launcher that is being built. */
    @Nullable
    public RuntimeApplicationLauncher launcher;

    /** The index of the application's runtime in the constant pool, or -1 if the application has no runtime, */
    @Nullable
    public final DynamicAccessor runtimeAccessor;

    ApplicationCodeGenerator(ApplicationSetup application) {
        this.application = application;
        this.runtimeAccessor = application.lifetimeKind == OldLifetimeKind.MANAGED
                ? application.container.lifetime.pool.reserve(PackedManagedLifetime.class)
                : null;
    }

    public int addArray(InstanceBeanConfiguration<?> beanConfiguration, OperationHandle operation) {
        arrayInvokers.compute(beanConfiguration, (k, v) -> {
            if (v == null) {
                v = new MethodHandleArray();
                // beanConfiguration.initializeAtCodegen(null, null)
            }
            v.handles.add(operation);
            return v;
        });
        return 0;
    }

    static class MethodHandleArray implements Supplier<MethodHandle[]> {

        private final List<OperationHandle> handles = new ArrayList<>();

        /** {@inheritDoc} */
        @Override
        public MethodHandle[] get() {
            MethodHandle[] mh = new MethodHandle[handles.size()];
            for (int i = 0; i < mh.length; i++) {
                mh[i] = handles.get(i).generateMethodHandle();
            }
            return mh; // freeze
        }
    }

    /**
     * 
     */
    void finish() {
        CodegenEvent ce = new CodegenEvent();
        ce.begin();

        application.container.lifetime.codegen();
        for (Runnable r : actions) {
            r.run();
        }
        ce.commit();

        launcher = new RuntimeApplicationLauncher(application);
    }
}
