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
package internal.app.packed.bean;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;

import app.packed.base.Nullable;
import app.packed.bean.BeanDefinitionException;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.BindingHook;
import app.packed.bean.BeanIntrospector.FieldHook;
import app.packed.bean.BeanIntrospector.MethodHook;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import internal.app.packed.base.devtools.PackedDevToolsIntegration;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * This class is responsible for finding fields, methods, parameters that have hook annotations.
 */
public final class Introspector {

    /** We never process classes that are located in the {@code java.base} module. */
    public static final Module JAVA_BASE_MODULE = Object.class.getModule();

    /** The bean that is being introspected. */
    public final BeanSetup bean;

    /** The class we are introspecting. */
    private final Class<?> beanClass;

    /** Non-null if a introspector was set via {@link BeanHandle.Installer#introspectWith(BeanIntrospector)}. */
    @Nullable
    private final BeanIntrospector beanHandleIntrospector;

    // I think we need stable iteration order... AppendOnly identity map, stable iteration order
    // I think we sort in BeanFields...
    /** Every extension that is activated by a hook. */
    private final LinkedHashMap<Class<? extends Extension<?>>, ExtensionEntry> extensions = new LinkedHashMap<>();

    // Should be made lazily??? I think
    final OpenClass oc;

    public Introspector(BeanSetup bean, @Nullable BeanIntrospector beanHandleIntrospector) {
        this.bean = bean;
        this.beanClass = bean.beanClass();
        this.beanHandleIntrospector = beanHandleIntrospector;
        this.oc = OpenClass.of(MethodHandles.lookup(), beanClass);
    }

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_BEAN_INTROSPECTOR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "newBeanIntrospector", BeanIntrospector.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(),
            BeanIntrospector.class, "initialize", void.class, ExtensionSetup.class, BeanSetup.class);

    private ExtensionEntry computeExtensionEntry(Class<? extends Extension<?>> extensionType, boolean fullAccess) {
        return extensions.computeIfAbsent(extensionType, c -> {
            // Get the extension (installing it if necessary)
            ExtensionSetup extension = bean.container.useExtensionSetup(extensionType, null);

            BeanIntrospector introspector;
            if (beanHandleIntrospector != null && bean.operator() == extensionType) {
                // A special introspector has been set, don't
                introspector = beanHandleIntrospector;
                try {
                    MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, extension, bean);
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }
            } else {
                try {
                    introspector = (BeanIntrospector) MH_EXTENSION_NEW_BEAN_INTROSPECTOR.invokeExact(extension.instance());
                    MH_EXTENSION_BEAN_INTROSPECTOR_INITIALIZE.invokeExact(introspector, extension, bean);
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }
            }

            // Notify the bean introspector that is being used
            introspector.onPreIntrospect();
            return new ExtensionEntry(extension, introspector, fullAccess);
        });
    }

    /**
     * @param reflectOnFields
     *            whether or not to iterate over fields
     * @param baseType
     *            the base type
     */
    public final void introspect() {
        // We start by processing annotations on the bean class
        introspectClass();

        // Then we process all bean fields
        introspectFields(beanClass);

        record MethodHelper(int hash, String name, Class<?>[] parameterTypes) {

            MethodHelper(Method method) {
                this(method.getName(), method.getParameterTypes());
            }

            MethodHelper(String name, Class<?>[] parameterTypes) {
                this(name.hashCode() ^ Arrays.hashCode(parameterTypes), name, parameterTypes);
            }

            /** {@inheritDoc} */
            @Override
            public boolean equals(Object obj) {
                return obj instanceof MethodHelper h && name == h.name() && Arrays.equals(parameterTypes, h.parameterTypes);
            }

            /** {@inheritDoc} */
            @Override
            public int hashCode() {
                return hash;
            }
        }
        HashSet<Package> packages = new HashSet<>();
        HashMap<MethodHelper, HashSet<Package>> types = new HashMap<>();

        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        for (Method m : beanClass.getMethods()) {
            // Filter methods whose from java.base module and bridge methods
            // TODO add check for
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                types.put(new MethodHelper(m), packages);
                introspectMethod(m);// move this to step 2???
            }
        }

        // Step 2 process all declared methods

        // Maybe some kind of detection if current type (c) switches modules.
        for (Class<?> c = beanClass; c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            Method[] methods = c.getDeclaredMethods();
            PackedDevToolsIntegration.INSTANCE.reflectMembers(c, methods);
            for (Method m : methods) {
                int mod = m.getModifiers();
                if (Modifier.isStatic(mod)) {
                    if (c == beanClass && !Modifier.isPublic(mod)) { // we have already processed public static methods
                        // only include static methods in the top level class
                        // We do this, because it would be strange to include
                        // static methods on any interfaces this class implements.
                        // But it would also be strange to include static methods on sub classes
                        // but not include static methods on interfaces.
                        introspectMethod(m);
                    }
                } else if (!m.isBridge() && !m.isSynthetic()) { // TODO should we include synthetic methods??
                    switch (mod & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) {
                    case Modifier.PUBLIC:
                        continue; // we have already added the method in the first step
                    default: // default access
                        HashSet<Package> pkg = types.computeIfAbsent(new MethodHelper(m), key -> new HashSet<>());
                        if (pkg != packages && pkg.add(c.getPackage())) {
                            break;
                        } else {
                            continue;
                        }
                    case Modifier.PROTECTED:
                        if (types.putIfAbsent(new MethodHelper(m), packages) != null) {
                            continue; // method has been overridden by a super type
                        }
                        // otherwise fall-through
                    case Modifier.PRIVATE:
                        // Private methods are never overridden
                    }
                    introspectMethod(m);
                }
            }
        }

        // Call into every BeanScanner and tell them its all over
        for (ExtensionEntry e : extensions.values()) {
            e.introspector.onPostIntrospect();
        }
    }

    private void introspectClass() {}

    /**
     * Introspect a single field on a bean.
     * 
     * Look for hook annotations on a single field.
     * 
     * @param field
     *            the field to introspect
     * 
     * @throws BeanDefinitionException
     *             if there are multiple {@link BindingHook} on the field. Or if there are both {@link FieldHook} and
     *             {@link BindingHook} annotations
     * 
     * @apiNote Currently we allow multiple {@link FieldHook} on a field. This might change in the future, but for now we
     *          allow it.
     */
    private void introspectField(Field field) {
        // First, we get all annotations on the field
        Annotation[] annotations = field.getAnnotations();

        // Than, we iterate through the annotations and look for usage of FieldHook or ProvisionHook meta annotations
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];

            // Look in the field annotation cache to see if the annotation is either a FieldHook or ProvisionHook annotation
            FieldAnnotationCache e = FieldAnnotationCache.CACHE.get(annotation.annotationType());

            // The annotation is neither a field or provision annotation
            if (e == null) {
                continue;
            }

            // A record + map that we use if have multi field hook annotations
            record MultiField(Class<? extends Extension<?>> extensionClass, boolean allowGet, boolean allowSet, Annotation... annotations) {}
            IdentityHashMap<Class<? extends Extension<?>>, MultiField> multiMatch = null;

            // Look through remaining annotations.
            for (int j = i; j < annotations.length; j++) {
                Annotation annotation2 = annotations[j];

                // Look in the field annotation cache to see if the annotation is either a FieldHook or ProvisionHook annotation
                FieldAnnotationCache e2 = FieldAnnotationCache.CACHE.get(annotation2.annotationType());

                // The annotation is neither a field or provision annotation
                if (e2 == null) {
                    continue;
                }

                if (e.isProvision || e2.isProvision) {
                    throw new BeanDefinitionException("Cannot use both " + annotation + " and " + annotation2);
                }

                // Okay we have more than 1 valid annotation

                // Check to see if we need to create the multi match map
                if (multiMatch == null) {
                    multiMatch = new IdentityHashMap<>();
                    // Start by adding the first match
                    multiMatch.put(e.extensionType, new MultiField(e.extensionType, e.isGettable, e.isSettable, annotation));
                }

                // Add this match
                multiMatch.compute(e2.extensionType, (Class<? extends Extension<?>> key, MultiField value) -> {
                    if (value == null) {
                        return new MultiField(key, e2.isGettable, e2.isSettable, annotation2);
                    } else {
                        Annotation[] a = new Annotation[value.annotations.length + 1];
                        for (int k = 0; k < value.annotations.length; k++) {
                            a[k] = value.annotations[k];
                        }
                        a[a.length - 1] = annotation2;
                        return new MultiField(key, e2.isGettable && value.allowGet, e2.isSettable && e2.isSettable, a);
                    }
                });
            }

            // All done. Let us see if we only had a single match, or we had multiple valid matches
            if (multiMatch == null) {
                // Get the matching extension, installing it if needed.
                ExtensionEntry entry = computeExtensionEntry(e.extensionType, false);

                // Create the wrapped field that is exposed to the extension
                IntrospectorOnField f = new IntrospectorOnField(Introspector.this, entry.extension, field, e.isGettable || entry.hasFullAccess,
                        e.isSettable || entry.hasFullAccess, new Annotation[] { annotation });

                // Call BeanIntrospection.onField
                entry.introspector.onFieldHook(f);
            } else {
                // TODO sort by extension order if we have more than one

                for (MultiField mf : multiMatch.values()) {
                    ExtensionEntry entry = computeExtensionEntry(mf.extensionClass, false);

                    // Create the wrapped field that is exposed to the extension
                    IntrospectorOnField f = new IntrospectorOnField(Introspector.this, entry.extension, field, mf.allowGet || entry.hasFullAccess,
                            mf.allowSet || entry.hasFullAccess, annotations);

                    // Call BeanIntrospection.onField
                    entry.introspector.onFieldHook(f);
                }
            }
        }
    }

    private void introspectFields(Class<?> clazz) {
        // See if the class is in the java.base module in which we never process it.
        if (clazz.getModule() != JAVA_BASE_MODULE) {
            // Recursively call into superclass, before processing own fields
            introspectFields(clazz.getSuperclass());

            // PackedDevToolsIntegration.INSTANCE.reflectMembers(c, fields);

            // Iterate over all declared fields
            for (Field field : clazz.getDeclaredFields()) {
                introspectField(field);
            }
        }
    }

    /**
     * Look for hook annotations on a single method.
     * 
     * @param method
     *            the method to look for annotations on
     */
    private void introspectMethod(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            MethodAnnotationCache fh = MethodAnnotationCache.CACHE.get(a1Type);
            if (fh != null) {
                ExtensionEntry ei = computeExtensionEntry(fh.extensionType, false);

                IntrospectorOnMethod pbm = new IntrospectorOnMethod(Introspector.this, ei.extension, method, annotations, fh.isInvokable);

                ei.introspector.onMethodHook(pbm);
            }
        }
    }

    static void checkExtensionClass(Class<?> annotationType, Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, s -> new InternalExtensionException(s));
        if (extensionType.getModule() != annotationType.getModule()) {
            throw new InternalExtensionException(
                    "The annotation " + annotationType + " and the extension " + extensionType + " must be declared in the same module");
        }
    }

    private record ExtensionEntry(ExtensionSetup extension, BeanIntrospector introspector, boolean hasFullAccess) {}

    /**
     * Cache the various annotations that are placed on field
     */
    private record FieldAnnotationCache(Class<? extends Annotation> annotationType, Class<? extends Extension<?>> extensionType, boolean isGettable,
            boolean isSettable, boolean isProvision) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<FieldAnnotationCache> CACHE = new ClassValue<>() {

            @Override
            protected FieldAnnotationCache computeValue(Class<?> type) {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> annotationType = (Class<? extends Annotation>) type;
                FieldHook fieldHook = type.getAnnotation(FieldHook.class);
                BindingHook provisionHook = type.getAnnotation(BindingHook.class);

                if (provisionHook == fieldHook) { // check both null
                    return null;
                } else if (provisionHook == null) {
                    checkExtensionClass(type, fieldHook.extension());
                    return new FieldAnnotationCache(annotationType, fieldHook.extension(), fieldHook.allowGet(), fieldHook.allowSet(), false);
                } else if (fieldHook == null) {
                    checkExtensionClass(type, provisionHook.extension());
                    return new FieldAnnotationCache(annotationType, provisionHook.extension(), false, true, true);
                } else {
                    throw new InternalExtensionException(type + " cannot both be annotated with " + FieldHook.class + " and " + BindingHook.class);
                }
            }
        };
    }

    private record MethodAnnotationCache(Class<? extends Extension<?>> extensionType, boolean isInvokable) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<MethodAnnotationCache> CACHE = new ClassValue<>() {

            @Override
            protected MethodAnnotationCache computeValue(Class<?> type) {
                MethodHook h = type.getAnnotation(MethodHook.class);
                if (h == null) {
                    return null;
                }
                checkExtensionClass(type, h.extension());
                return new MethodAnnotationCache(h.extension(), h.allowInvoke());
            }
        };
    }
}
