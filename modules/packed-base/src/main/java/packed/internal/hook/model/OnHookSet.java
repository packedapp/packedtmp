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
package packed.internal.hook.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;

import packed.internal.container.access.ClassProcessor;
import packed.internal.util.ThrowableFactory;

/**
 *
 */
// Ideen er at vi resolver, maaske alle paa en gang

// Vi bliver noedt til at have den her som samler alt,
public class OnHookSet {

    /** Non-base hooks */
    final IdentityHashMap<Class<?>, OnHookNodeBuilder> builders = new IdentityHashMap<>();

    final ClassProcessor cp;

    OnHookNodeBuilder rootBuilder;

    private int nextId;

    private final OnHookNodeBuilder builder;

    private final ArrayDeque<OnHookNodeBuilder> onProcessed = new ArrayDeque<>();

    final ArrayList<OnHookNodeBuilder> sorted = new ArrayList<>();

    public OnHookSet(ClassProcessor cp) {
        this.cp = requireNonNull(cp);
        this.builder = new OnHookNodeBuilder(this, 0, cp, OnHookContainerType.BUILDER);
    }

    public void process() {
        cp.findMethods(m -> builder.onMethod(m, ThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY));
        // System.out.println("Process spawned");
        for (OnHookNodeBuilder b = onProcessed.pollFirst(); b != null; b = onProcessed.pollFirst()) {
            OnHookNodeBuilder bb = b;
            b.cp.findMethods(m -> bb.onMethod(m, ThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY));
        }
        // Top search
        // int prevSize = builders.size();

    }

    OnHookNodeBuilder builderFor(Class<?> cl) {
        OnHookNodeBuilder b = builders.computeIfAbsent(cl, k -> {
            OnHookNodeBuilder newB = new OnHookNodeBuilder(this, ++nextId, cp.spawn(k), OnHookContainerType.BUILDER);
            onProcessed.addLast(newB);
            return newB;
        });
        return b;
    }
}
// if rootClass.getModule() != hook.class.getModule and hook.class.getModule() is not open...
/// Complain.