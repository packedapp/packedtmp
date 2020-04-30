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
package packed.internal.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.Objects;

import app.packed.base.Key;
import app.packed.base.Nullable;

/**
 *
 */
// Enten som en Builder, eller noget a.la. Bundle.
//disableComposites/ignoreComposite/failOnComposite/...

// TODO Add @AnnotationHandler...
// Add Composite
// Everything is instance for now
public final class FunctionResolver {

    private final MethodType callSiteType;

    final HashMap<Key<?>, Entry> keys = new HashMap<>();

    final HashMap<Class<? extends Annotation>, AnnoClassEntry> annoations = new HashMap<>();

    private FunctionResolver(MethodType callSiteType) {
        this.callSiteType = requireNonNull(callSiteType);
    }

    private FunctionResolver add(Key<?> key, MethodHandle transformer, int... indexes) {
        for (int i = 0; i < indexes.length; i++) {
            Objects.checkFromIndexSize(indexes[i], 0, callSiteType.parameterCount());
        }

        // Check the various types matches...
        if (keys.putIfAbsent(key, new Entry(indexes, transformer)) != null) {
            throw new IllegalArgumentException("The specified key " + key + " has already been added");
        }
        return this;
    }

    public <T> FunctionResolver addAnnoClassMapper(Class<? extends Annotation> annotationType, MethodHandle mh, int index) {
        annoations.put(annotationType, new AnnoClassEntry(annotationType, index, mh));
        return this;
    }

    public FunctionResolver addKey(Class<?> key, int index) {
        return addKey(Key.of(key), index);
    }

    public FunctionResolver addKey(Class<?> key, MethodHandle transformer, int... indexes) {
        return add(Key.of(key), transformer, indexes);
    }

    public FunctionResolver addKey(Key<?> key, int index) {
        return add(key, null, index);
    }

    public FunctionResolver addKey(Key<?> key, MethodHandle transformer, int... indexes) {
        return add(key, requireNonNull(transformer, "transformer is null"), indexes);
    }

    public MethodType callSiteType() {
        return callSiteType;
    }

    public MethodHandle resolve(OpenClass oc, Executable e) {
        return new FindMember(oc, e, this).find();
    }

    public static FunctionResolver of(Class<?> returnType, Class<?>... parameterTypes) {
        return of(MethodType.methodType(returnType, parameterTypes));
    }

    static FunctionResolver of(MethodType callSiteType) {
        return new FunctionResolver(callSiteType);
    }

    static class AnnoClassEntry {
        Class<? extends Annotation> annotationType;
        int index;
        MethodHandle mh;

        public AnnoClassEntry(Class<? extends Annotation> annotationType, int index, MethodHandle mh) {
            this.annotationType = annotationType;
            this.index = index;
            this.mh = mh;
        }
    }

    static class Entry {
        @Nullable
        int[] indexes;

        @Nullable
        MethodHandle transformer;

        Entry(int[] indexes, MethodHandle transformer) {
            this.indexes = indexes;
            this.transformer = transformer;
        }
    }
}

// Kunne godt have noget hjaelpe tekst. hvis man ikke kan finde en key..
// F.eks. ? extends Extension -> Du skal bruge UseExtension...
// Extension -> Du skal dependende paa en specific extension...
// LifecycleContext -> From the perspective of Packed "dddd" does not have a lifecycle, so no LifecycleContext is available.