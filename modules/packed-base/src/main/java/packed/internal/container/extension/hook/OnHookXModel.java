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
package packed.internal.container.extension.hook;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.IdentityHashMap;

import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.Extension;
import app.packed.container.extension.OnHookGroup;
import app.packed.util.InvalidDeclarationException;

/** This class contains information about {@link OnHookGroup} methods for an extension type. */
public final class OnHookXModel {

    /** A cache of descriptors for a particular extension type. */
    private static final ClassValue<OnHookXModel> CACHE = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected OnHookXModel computeValue(Class<?> type) {
            return new Builder((Class<? extends Extension>) type).build();
        }
    };

    /** A map of all methods that take a aggregator result object. Is always located on the actual extension. */
    final IdentityHashMap<Class<?>, MethodHandle> aggregators;

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    /** The extension type we manage information for. */
    private final Class<? extends Extension> extensionType;

    /**
     * Creates a new manager from the specified builder.
     * 
     * @param builder
     *            the builder to create the manager from
     */
    @SuppressWarnings("unchecked")
    private OnHookXModel(Builder builder) {
        this.extensionType = (Class<? extends Extension>) builder.actualType;
        this.aggregators = builder.groups;
        this.annotatedFields = builder.annotatedFields;
        this.annotatedMethods = builder.annotatedMethods;
        this.annotatedTypes = builder.annotatedTypes;
    }

    MethodHandle findMethodHandleForAnnotatedField(PackedAnnotatedFieldHook<?> paf) {
        MethodHandle mh = annotatedFields.get(paf.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException(
                    "Extension " + extensionType + " does not know how to process fields annotated with " + paf.annotation().annotationType());
        }
        return mh;
    }

    MethodHandle findMethodHandleForAnnotatedMethod(PackedAnnotatedMethodHook<?> paf) {
        MethodHandle mh = annotatedMethods.get(paf.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException();
        }
        return mh;
    }

    /**
     * Returns a descriptor for the specified extensionType
     * 
     * @param extensionType
     *            the extension type to return a descriptor for
     * @return the descriptor
     * @throws InvalidDeclarationException
     *             if the usage of {@link OnHookGroup} on the extension does not adhere to contract
     */
    public static OnHookXModel get(Class<? extends Extension> extensionType) {
        return CACHE.get(extensionType);
    }

    /** A builder for {@link OnHookXModel}. */
    private static class Builder extends OnHookMemberProcessor {

        private Builder(Class<? extends Extension> extensionType) {
            super(Extension.class, extensionType, false);
        }

        private OnHookXModel build() {
            findMethods();
            return new OnHookXModel(this);
        }
    }

}
