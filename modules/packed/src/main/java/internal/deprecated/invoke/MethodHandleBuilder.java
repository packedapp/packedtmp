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
package internal.deprecated.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.List;

import app.packed.base.Key;
import internal.app.packed.util.OpenClass;

/**
 *
 */
// Enten som en Builder, eller noget a.la. Assembly.
//disableComposites/ignoreComposite/failOnComposite/...

// TODO Add @AnnotationHandler...
// Add Composite
// Everything is instance for now
final class MethodHandleBuilder {

    final HashMap<Class<? extends Annotation>, AnnoClassEntry> annoations = new HashMap<>();

    final HashMap<Key<?>, InternalInfuser.Entry> keys = new HashMap<>();

    /** Used for figuring out where the receiver is if instance method. -1 we only static methods. TODO implement */
    int receiverIndex = 0;

    private final MethodType targetType;

    private MethodHandleBuilder(MethodType targetType) {
        this.targetType = requireNonNull(targetType);
    }

    public void add(InternalInfuser infuser) {
        keys.putAll(infuser.services);
    }

    public MethodHandle build(OpenClass oc, Executable e) {
        return new MethodHandleBuilderHelper(oc, e, this).find();
    }

    MethodHandleBuilder setStatic() {
        this.receiverIndex = -1;
        return this;
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

    public static MethodHandleBuilder of(Class<?> returnType, List<Class<?>> parameterTypes) {
        return new MethodHandleBuilder(MethodType.methodType(returnType, parameterTypes));
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

}

// Kunne godt have noget hjaelpe tekst. hvis man ikke kan finde en key..
// F.eks. ? extends Extension -> Du skal bruge UseExtension...
// Extension -> Du skal dependende paa en specific extension...
// LifecycleContext -> From the perspective of Packed "dddd" does not have a lifecycle, so no LifecycleContext is available.