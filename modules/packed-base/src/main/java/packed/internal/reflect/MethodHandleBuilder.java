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
public final class MethodHandleBuilder {

    final HashMap<Class<? extends Annotation>, AnnoClassEntry> annoations = new HashMap<>();

    final HashMap<Key<?>, Entry> keys = new HashMap<>();

    private final MethodType targetType;

    private MethodHandleBuilder(MethodType targetType) {
        this.targetType = requireNonNull(targetType);
    }

    private MethodHandleBuilder add(Key<?> key, MethodHandle transformer, int... indexes) {
        for (int i = 0; i < indexes.length; i++) {
            Objects.checkFromIndexSize(indexes[i], 0, targetType.parameterCount());
        }

        // Check the various types matches...
        if (keys.putIfAbsent(key, new Entry(indexes, transformer)) != null) {
            throw new IllegalArgumentException("The specified key " + key + " has already been added");
        }
        return this;
    }

    public <T> MethodHandleBuilder addAnnoClassMapper(Class<? extends Annotation> annotationType, MethodHandle mh, int index) {
        annoations.put(annotationType, new AnnoClassEntry(annotationType, index, mh));
        return this;
    }

    public MethodHandleBuilder addKey(Class<?> key, int index) {
        return addKey(Key.of(key), index);
    }

    public MethodHandleBuilder addKey(Class<?> key, MethodHandle transformer, int... indexes) {
        return add(Key.of(key), transformer, indexes);
    }

    public MethodHandleBuilder addKey(Key<?> key, int index) {
        return add(key, null, index);
    }

    public MethodHandleBuilder addKey(Key<?> key, MethodHandle transformer, int... indexes) {
        return add(key, requireNonNull(transformer, "transformer is null"), indexes);
    }

    public MethodHandle build(OpenClass oc, Executable e) {
        return new MethodHandleBuilderHelper(oc, e, this).find();
    }

    /**
     * Returns the target type of the method handle to build. Calling {@link #build(OpenClass, Executable)} will return a
     * method handle with this exact type.
     * 
     * @return the target type of the method handle to build
     */
    public MethodType targetType() {
        return targetType;
    }

    public static MethodHandleBuilder of(Class<?> returnType, Class<?>... parameterTypes) {
        return of(MethodType.methodType(returnType, parameterTypes));
    }

    /**
     * Creates a new builder.
     * 
     * @param targetType
     *            the type of the method handle being build
     * @return a builder
     */
    static MethodHandleBuilder of(MethodType targetType) {
        return new MethodHandleBuilder(targetType);
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

    public static class AnnotationResolutionStrategy {

        Class<?> baseType;

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