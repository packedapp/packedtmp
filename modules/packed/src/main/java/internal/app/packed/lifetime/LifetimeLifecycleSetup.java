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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import app.packed.bean.BeanLifecycleOperationMirror;
import app.packed.bean.LifecycleOrdering;
import app.packed.framework.Nullable;
import app.packed.lifetime.RunState;
import app.packed.operation.OperationHandle;

/**
 *
 */
// Create it lazily for beans? Not all beans need it
public final class LifetimeLifecycleSetup {

    public void addInitialize(OperationHandle operation, @Nullable LifecycleOrdering ordering) {
        if (ordering == null) { // inject
            initializors.add(operation);
        } else if (ordering == LifecycleOrdering.BEFORE_DEPENDENCIES) {
            initializors.add(operation);
        } else {
            initializorsReverse.add(operation);
        }
        operationsLifetime.add(new LifetimeOperation(RunState.INITIALIZING, operation));
        operation.specializeMirror(() -> new BeanLifecycleOperationMirror());
    }

    public void addStart(OperationHandle operation, LifecycleOrdering ordering) {
        if (ordering == LifecycleOrdering.BEFORE_DEPENDENCIES) {
            start.add(operation);
        } else {
            startReverse.add(operation);
        }
        operationsLifetime.add(new LifetimeOperation(RunState.STARTING, operation));
        operation.specializeMirror(() -> new BeanLifecycleOperationMirror());

    }

    public void addStop(OperationHandle operation, LifecycleOrdering ordering) {
        if (ordering == LifecycleOrdering.AFTER_DEPENDENCIES) {
            start.add(operation);
        } else {
            startReverse.add(operation);
        }
        operationsLifetime.add(new LifetimeOperation(RunState.STOPPING, operation));
        operation.specializeMirror(() -> new BeanLifecycleOperationMirror());
    }

    /** The beans lifetime operations. */
    public final List<LifetimeOperation> operationsLifetime = new ArrayList<>();

    public final ArrayList<OperationHandle> initializors = new ArrayList<>();

    public final ArrayDeque<OperationHandle> initializorsReverse = new ArrayDeque<>();

    public final ArrayList<OperationHandle> start = new ArrayList<>();

    public final ArrayDeque<OperationHandle> startReverse = new ArrayDeque<>();

    public final ArrayList<OperationHandle> stop = new ArrayList<>();

    public final ArrayDeque<OperationHandle> stopReverse = new ArrayDeque<>();
}
