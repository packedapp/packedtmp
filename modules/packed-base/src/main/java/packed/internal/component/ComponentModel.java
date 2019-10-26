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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.IdentityHashMap;
import java.util.Set;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerSource;
import app.packed.container.Extension;
import app.packed.container.UseExtension;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.container.ContainerSourceModel;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.container.extension.LazyExtensionActivationMap;
import packed.internal.hook.HookRequest;
import packed.internal.hook.HookRequestBuilder;
import packed.internal.hook.HookTargetProcessor;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/**
 * A model of a container, a cached instance of this class is acquired via
 * {@link ContainerSourceModel#componentModelOf(Class)}.
 */
public final class ComponentModel {

    /** The type of component this is a model for. */
    private final Class<?> componentType;

    /** An array of any extensions with relevant {@link OnHook} methods. */
    private final ExtensionRequestPair[] extensionHooks;

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    private String simpleName;

    /** Any methods annotated with {@link OnHook} on the container source. */
    @Nullable
    private final HookRequest sourceHook;

    /**
     * Creates a new descriptor.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private ComponentModel(ComponentModel.Builder builder) {
        this.componentType = requireNonNull(builder.cp.clazz());

        try {
            this.sourceHook = builder.csb == null ? null : builder.csb.build();
        } catch (Throwable ee) {
            ThrowableUtil.rethrowErrorOrRuntimeException(ee);
            throw new UndeclaredThrowableException(ee);
        }

        // There should probably be some order we call extensions in....
        /// Other first, packed lasts?
        /// Think they need an order id....
        // Boer vaere lowest dependency id first...
        // Preferable deterministic

        this.extensionHooks = builder.extensionBuilders.entrySet().stream().map(e -> {
            HookRequest r;
            try {
                r = e.getValue().build();
            } catch (Throwable ee) {
                ThrowableUtil.rethrowErrorOrRuntimeException(ee);
                throw new UndeclaredThrowableException(ee);
            }
            return new ExtensionRequestPair(e.getKey(), r);
        }).toArray(i -> new ExtensionRequestPair[i]);
    }

    /**
     * Returns the default prefix for the component, if no name is explicitly set by the user.
     * 
     * @return the default prefix for the component, if no name is explicitly set by the user
     */
    String defaultPrefix() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = componentType.getSimpleName();
        }
        return s;
    }

    <T> ComponentConfiguration<T> invokeOnHookOnInstall(ContainerSource cs, AbstractComponentConfiguration<T> acc) {
        try {
            // First invoke any OnHook methods on the container source (bundle)
            if (sourceHook != null) {
                sourceHook.invokeIt(cs, acc);
            }

            // Next, invoke any OnHook methods on relevant extensions.
            for (ExtensionRequestPair he : extensionHooks) {
                // Finds (possible installing) the extension with @OnHook methods
                Extension extension = acc.container.use(he.extensionType);

                // Invoke each method annotated with @OnHook on the extension instance
                he.request.invokeIt(extension, acc);
            }
        } catch (Throwable t) {
            ThrowableUtil.rethrowErrorOrRuntimeException(t);
            throw new UndeclaredThrowableException(t);
        }
        return acc;
    }

    /**
     * Creates a new component model instance.
     * 
     * @param csm
     *            a model of the container source that is trying to install the component
     * @param cp
     *            a class processor usable by hooks
     * @return a model of the component
     */
    public static ComponentModel newInstance(ContainerSourceModel csm, ClassProcessor cp) {
        return new Builder(csm, cp).build();
    }

    /** A builder object for a component model. */
    private static final class Builder {

        private final ClassProcessor cp;

        HookRequestBuilder csb;

        final ContainerSourceModel csm;

        /** A map of builders for every activated extension. */
        private final IdentityHashMap<Class<? extends Extension>, HookRequestBuilder> extensionBuilders = new IdentityHashMap<>();

        /**
         * Creates a new component model builder
         * 
         * @param cp
         *            a class processor usable by hooks
         * 
         */
        private Builder(ContainerSourceModel csm, ClassProcessor cp) {
            this.csm = requireNonNull(csm);
            this.cp = requireNonNull(cp);
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        private ComponentModel build() {
            final LazyExtensionActivationMap activatorMap = csm.activatorMap;
            Class<?> componentType = cp.clazz();

            try (HookTargetProcessor htp = new HookTargetProcessor(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
                this.csb = csm.onHookModel == null ? null : new HookRequestBuilder(csm.onHookModel, htp);

                findAssinableTo(htp, activatorMap, componentType);
                findAnnotatedTypes(htp, activatorMap, componentType);
                // Inherited annotations???
                cp.findMethodsAndFields(method -> findAnnotatedMethods(htp, activatorMap, method), field -> findAnnotatedFields(htp, activatorMap, field));
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
            return new ComponentModel(this);
        }

        private void findAssinableTo(HookTargetProcessor htp, LazyExtensionActivationMap activatorMap, Class<?> componentType) throws Throwable {

            IdentityHashMap<Class<?>, Class<? extends Extension>> into = new IdentityHashMap<>();
            for (Class<?> current = componentType; current != Object.class; current = current.getSuperclass()) {

                putInto(into, current);
                UseExtension ue = current.getAnnotation(UseExtension.class);
                if (ue != null) {
                    for (Class<? extends Extension> eType : ue.value()) {
                        into.put(current, eType);
                    }
                }
            }
            for (var e : into.entrySet()) {
                extensionBuilders.computeIfAbsent(e.getValue(), etype -> new HookRequestBuilder(ExtensionModel.onHookModelOf(etype), htp))
                        .onAssignableTo(e.getKey(), componentType);
            }
        }

        public static void putInto(IdentityHashMap<Class<?>, Class<? extends Extension>> into, Class<?> clazz) {
            for (Class<?> cl : clazz.getInterfaces()) {

                UseExtension ue = cl.getAnnotation(UseExtension.class);

                if (ue != null) {
                    for (Class<? extends Extension> eType : ue.value()) {
                        into.put(cl, eType);
                    }
                }
                putInto(into, cl);
            }
        }

        private void findAnnotatedFields(HookTargetProcessor htp, LazyExtensionActivationMap activatorMap, Field field) throws Throwable {
            for (Annotation a : field.getAnnotations()) {
                findAnnotatedFields0(htp, a, field, LazyExtensionActivationMap.EXTENSION_ACTIVATORS.get(a.annotationType()));
                if (activatorMap != null) {
                    findAnnotatedFields0(htp, a, field, activatorMap.onAnnotatedMethod(a.annotationType()));
                }
                if (csb != null) {
                    csb.onAnnotatedField(field, a);
                }
            }
        }

        private void findAnnotatedFields0(HookTargetProcessor hookProcessor, Annotation a, Field field, Set<Class<? extends Extension>> extensionTypes)
                throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new HookRequestBuilder(ExtensionModel.onHookModelOf(etype), hookProcessor))
                            .onAnnotatedField(field, a);
                }
            }
        }

        private void findAnnotatedMethods(HookTargetProcessor htp, LazyExtensionActivationMap activatorMap, Method method) throws Throwable {
            for (Annotation a : method.getAnnotations()) {
                findAnnotatedMethods0(htp, a, method, LazyExtensionActivationMap.EXTENSION_ACTIVATORS.get(a.annotationType()));
                if (activatorMap != null) {
                    findAnnotatedMethods0(htp, a, method, activatorMap.onAnnotatedMethod(a.annotationType()));
                }
                if (csb != null) {
                    csb.onAnnotatedMethod(method, a);
                }
            }
        }

        private void findAnnotatedMethods0(HookTargetProcessor hookProcessor, Annotation a, Method method, Set<Class<? extends Extension>> extensionTypes)
                throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new HookRequestBuilder(ExtensionModel.onHookModelOf(etype), hookProcessor))
                            .onAnnotatedMethod(method, a);
                }
            }
        }

        private void findAnnotatedTypes(HookTargetProcessor htp, LazyExtensionActivationMap activatorMap, Class<?> componentType) throws Throwable {
            for (Annotation a : componentType.getAnnotations()) {
                findAnnotatedTypes0(htp, componentType, a, LazyExtensionActivationMap.EXTENSION_ACTIVATORS.get(a.annotationType()));
                if (activatorMap != null) {
                    findAnnotatedTypes0(htp, componentType, a, activatorMap.onAnnotatedType(a.annotationType()));
                }
                if (csb != null) {
                    csb.onAnnotatedType(componentType, a);
                }
            }
        }

        private void findAnnotatedTypes0(HookTargetProcessor hookProcessor, Class<?> componentType, Annotation a,
                Set<Class<? extends Extension>> extensionTypes) throws Throwable {
            if (extensionTypes != null) {
                for (Class<? extends Extension> eType : extensionTypes) {
                    extensionBuilders.computeIfAbsent(eType, etype -> new HookRequestBuilder(ExtensionModel.onHookModelOf(etype), hookProcessor))
                            .onAnnotatedType(componentType, a);
                }
            }
        }
    }

    private static final class ExtensionRequestPair {

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        private final HookRequest request;

        private ExtensionRequestPair(Class<? extends Extension> extensionType, HookRequest request) {
            this.extensionType = requireNonNull(extensionType);
            this.request = requireNonNull(request);
        }
    }
}
