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
package packed.internal.componentcache;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import app.packed.container.ActivateExtension;
import app.packed.container.Extension;
import app.packed.container.ExtensionHookGroup;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.MethodDescriptor;
import packed.internal.container.AppPackedContainerSupport;
import packed.internal.container.NativeImageSupport;
import packed.internal.util.StringFormatter;
import packed.internal.util.TypeVariableExtractorUtil;

/**
 *
 */
public class ExtensionHookGroupConfiguration {

    static final ClassValue<ExtensionHookGroupConfiguration> FOR_CLASS = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionHookGroupConfiguration computeValue(Class<?> type) {
            return new ExtensionHookGroupConfiguration.Builder((Class<? extends ExtensionHookGroup<?, ?>>) type).build();
        }
    };

    final ExtensionHookGroup<?, ?> egc;

    final Class<? extends Extension<?>> extensionClass;

    final List<Object> list;

    private ExtensionHookGroupConfiguration(Builder builder) {
        egc = requireNonNull(builder.egc);
        extensionClass = builder.extensionClass;
        this.list = builder.list;
    }

    public static class Builder {
        final ArrayList<Object> list = new ArrayList<>();
        ExtensionHookGroup<?, ?> egc;

        final Class<? extends ExtensionHookGroup<?, ?>> type;

        final Class<? extends Extension<?>> extensionClass;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Builder(Class<? extends ExtensionHookGroup<?, ?>> type) {
            this.type = requireNonNull(type);
            extensionClass = (Class) TypeVariableExtractorUtil.findTypeParameterFromSuperClass(type, ExtensionHookGroup.class, 0);
        }

        @SuppressWarnings("rawtypes")
        ExtensionHookGroupConfiguration build() {
            if ((Class) type == ExtensionHookGroup.class) {
                throw new IllegalArgumentException();
            }
            // TODO check not abstract...
            Constructor<?> constructor;
            try {
                constructor = type.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("The extension " + StringFormatter.format(type) + " must have a no-argument constructor to be installed.");
            }

            Lookup lookup = MethodHandles.lookup();
            MethodHandle mh;
            try {
                constructor.setAccessible(true);
                mh = lookup.unreflectConstructor(constructor);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new IllegalAccessRuntimeException("In order to use the extension " + StringFormatter.format(type) + ", the module '"
                        + type.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
            }

            NativeImageSupport.registerConstructor(constructor);
            try {
                egc = (ExtensionHookGroup<?, ?>) mh.invoke();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            AppPackedContainerSupport.invoke().configureExtensionGroup(egc, this);
            return new ExtensionHookGroupConfiguration(this);
        }

        public <A extends Annotation> void onAnnotatedMethod(Class<A> annotationType,
                BiConsumer<? extends ExtensionHookGroup.Builder<?>, AnnotatedMethodHook<A>> consumer) {}

        /**
         * @param annotationType
         * @param consumer
         */
        public void onAnnotatedMethodDescription(Class<?> annotationType, BiConsumer<? extends ExtensionHookGroup.Builder<?>, MethodDescriptor> consumer) {
            if (ComponentClassDescriptor.Builder.METHOD_ANNOTATION_ACTIVATOR.get(annotationType) != type) {
                throw new IllegalStateException("Annotation @" + annotationType.getSimpleName() + " must be annotated with @"
                        + ActivateExtension.class.getSimpleName() + "(" + extensionClass.getSimpleName() + ".class) to be used with this method");
            }
            list.add(new OnMethodDescription(annotationType, consumer));
            // TODO Auto-generated method stub
        }
    }

    static class OnMethodDescription {
        final Class<?> annotationType;
        final BiConsumer<? extends ExtensionHookGroup.Builder<?>, MethodDescriptor> consumer;

        public OnMethodDescription(Class<?> annotationType,
                BiConsumer<? extends app.packed.container.ExtensionHookGroup.Builder<?>, MethodDescriptor> consumer) {
            this.annotationType = annotationType;
            this.consumer = consumer;
        }
    }
}
