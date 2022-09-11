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
package internal.app.packed.bean.hooks;

import java.lang.annotation.Annotation;
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
import app.packed.bean.BeanExtensionPoint.FieldHook;
import app.packed.bean.BeanExtensionPoint.MethodHook;
import app.packed.bean.BeanExtensionPoint.ProvisionHook;
import app.packed.bean.BeanIntrospector;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.integrate.devtools.PackedDevToolsIntegration;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.OpenClass;

/**
 * This class is responsible for finding fields, methods, parameters that have hook annotations.
 */
public final class BeanIntrospectionHelper {

    /** We never process classes that are located in the {@code java.base} module. */
    private static final Module JAVA_BASE_MODULE = Class.class.getModule();

    /** The bean that is being introspected. */
    final BeanSetup bean;

    // I think we need stable iteration order... AppendOnly identity map, stable iteration order
    /** Every extension that is activated by a hook. */
    final LinkedHashMap<Class<? extends Extension<?>>, ExtensionEntry> extensions = new LinkedHashMap<>();

    // Should be made lazily??? I think
    final OpenClass oc;

    @Nullable
    final BeanIntrospector registrantIntrospector;

    public BeanIntrospectionHelper(BeanSetup bean, @Nullable BeanIntrospector registrantIntrospector) {
        this.bean = bean;
        this.registrantIntrospector = registrantIntrospector;
        this.oc = OpenClass.of(MethodHandles.lookup(), bean.beanClass());
    }

    private ExtensionEntry computeExtensionEntry(Class<? extends Extension<?>> clazz, boolean fullAccess) {
        return extensions.computeIfAbsent(clazz, c -> {
            // Get the extension (installing it if necessary)
            ExtensionSetup extension = bean.parent.useExtensionSetup(clazz, null);

            BeanIntrospector introspector;
            if (registrantIntrospector != null && bean.operator() == clazz) {
                // A special introspector has been set, don't
                introspector = registrantIntrospector;
                extension.initializeBeanIntrospector(introspector, extension, bean);
            } else {
                // Call Extension#newBeanIntrospector
                introspector = extension.newBeanIntrospector(extension, bean);
            }

            // Notify the bean introspector that is being used
            introspector.onIntrospectionBegin();
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

        Class<?> baseType = Object.class;

        Class<?> classToScan = bean.beanClass();
        HashSet<Package> packages = new HashSet<>();
        HashMap<MethodHelper, HashSet<Package>> types = new HashMap<>();

        // Hmm, skal skrive noget om de kan komme i enhver order
        // Lige nu kommer metoder foerend fields

        introspectClass(classToScan);

        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        for (Method m : classToScan.getMethods()) {
            // Filter methods whose from java.base module and bridge methods
            // TODO add check for
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                types.put(new MethodHelper(m), packages);
                introspectMethod(m);// move this to step 2???
            }
        }

        // Step 2 process all declared methods

        // Maybe some kind of detection if current type (c) switches modules.
        for (Class<?> c = classToScan; c != baseType && c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            // First process every field
            // if (reflectOnFields) {
            Field[] fields = c.getDeclaredFields();
            PackedDevToolsIntegration.INSTANCE.reflectMembers(c, fields);
            for (Field field : c.getDeclaredFields()) {
                introspectField(field);
            }
            // }

            Method[] methods = c.getDeclaredMethods();
            PackedDevToolsIntegration.INSTANCE.reflectMembers(c, methods);
            for (Method m : methods) {
                int mod = m.getModifiers();
                if (Modifier.isStatic(mod)) {
                    if (c == classToScan && !Modifier.isPublic(mod)) { // we have already processed public static methods
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
            e.introspector.onIntrospectionEnd();
        }
    }

    private void introspectClass(Class<?> clazz) {

    }

    /**
     * Introspect a single field on a bean.
     * 
     * Look for hook annotations on a single field.
     * 
     * @param field
     *            the field to introspect
     * 
     * @throws BeanDefinitionException
     *             if there are multiple {@link ProvisionHook} on the field. Or if there are both {@link FieldHook} and
     *             {@link ProvisionHook} annotations
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

            // A map that is used if have multi field hook annotations
            record MultiField(Class<? extends Extension<?>> extensionClass, boolean allowGet, boolean allowSet, Annotation... annotations) {}
            IdentityHashMap<Class<? extends Extension<?>>, MultiField> multiMatch = null;

            // Look through remaining annotations.
            //
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

                // Check if we need to create the multi match map
                if (multiMatch == null) {
                    multiMatch = new IdentityHashMap<>();
                    // Add the first match we add
                    multiMatch.put(e.extensionType, new MultiField(e.extensionType, e.isGettable, e.isSettable, annotation));
                }

                // ADd this match
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

            // See if we only had 1 match?
            if (multiMatch == null) {
                ExtensionEntry entry = computeExtensionEntry(e.extensionType, false);

                // Create the wrapped field that is exposed to the extension
                PackedBeanField f = new PackedBeanField(bean, BeanIntrospectionHelper.this, entry.extension, field, e.isGettable || entry.hasFullAccess,
                        e.isSettable || entry.hasFullAccess, new Annotation[] { annotation });

                // Call BeanIntrospection.onField
                entry.introspector.onField(f);
            } else {
                // TODO sort by extension order if we have more than one

                for (MultiField mf : multiMatch.values()) {
                    ExtensionEntry entry = computeExtensionEntry(mf.extensionClass, false);

                    // Create the wrapped field that is exposed to the extension
                    PackedBeanField f = new PackedBeanField(bean, BeanIntrospectionHelper.this, entry.extension, field, mf.allowGet || entry.hasFullAccess,
                            mf.allowSet || entry.hasFullAccess, annotations);

                    // Call BeanIntrospection.onField
                    entry.introspector.onField(f);
                }
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
            MethodHookModel fh = MethodHookModel.CACHE.get(a1Type);
            if (fh != null) {
                ExtensionEntry ei = computeExtensionEntry(fh.extensionType, false);

                PackedBeanMethod pbm = new PackedBeanMethod(BeanIntrospectionHelper.this, ei.extension, method, fh.isInvokable);

                ei.introspector.onMethod(pbm);
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
                ProvisionHook provisionHook = type.getAnnotation(ProvisionHook.class);

                if (provisionHook == fieldHook) { // check both null
                    return null;
                } else if (provisionHook == null) {
                    checkExtensionClass(type, fieldHook.extension());
                    return new FieldAnnotationCache(annotationType, fieldHook.extension(), fieldHook.allowGet(), fieldHook.allowSet(), false);
                } else if (fieldHook == null) {
                    checkExtensionClass(type, provisionHook.extension());
                    return new FieldAnnotationCache(annotationType, provisionHook.extension(), false, true, true);
                } else {
                    throw new InternalExtensionException(type + " cannot both be annotated with " + FieldHook.class + " and " + ProvisionHook.class);
                }
            }
        };
    }

    private record MethodHookModel(Class<? extends Extension<?>> extensionType, boolean isInvokable) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<MethodHookModel> CACHE = new ClassValue<>() {

            @Override
            protected MethodHookModel computeValue(Class<?> type) {
                MethodHook h = type.getAnnotation(MethodHook.class);
                if (h == null) {
                    return null;
                }
                checkExtensionClass(type, h.extension());
                return new MethodHookModel(h.extension(), h.allowInvoke());
            }
        };
    }
}
