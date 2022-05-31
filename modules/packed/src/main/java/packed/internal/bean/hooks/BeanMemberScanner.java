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
package packed.internal.bean.hooks;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import app.packed.bean.hooks.BeanField;
import app.packed.bean.hooks.BeanMethod;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import packed.internal.bean.BeanSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.integrate.devtools.PackedDevToolsIntegration;
import packed.internal.util.ClassUtil;
import packed.internal.util.OpenClass;

/**
 * This class is responsible for finding fields or methods that have hook annotations.
 */
public final class BeanMemberScanner {

    /** We never process classes that are located in the java.base module. */
    private static final Module JAVA_BASE_MODULE = Class.class.getModule();

    /** The bean that is being scanned. */
    final BeanSetup bean;

    // I think we need stable iteration order... AppendOnly identity map, stable iteration order
    final LinkedHashMap<Class<? extends Extension<?>>, ExtensionSetup> extensions = new LinkedHashMap<>();

    final OpenClass oc;

    public BeanMemberScanner(BeanSetup bean) {
        this.bean = bean;
        this.oc = OpenClass.of(MethodHandles.lookup(), bean.beanClass());
    }

    private ExtensionSetup findOrCreateExtension(Module owner, Class<? extends Extension<?>> clazz) {
        ExtensionSetup extension = extensions.get(clazz);
        if (extension == null) {
            ContainerSetup container = bean.parent;
            extension = container.useExtensionSetup(clazz, null);
            extensions.put(clazz, extension);
            
            // this should probably be moved elsewhere

            // Is it per operation????? Don't think so
            // We probably need to store it in bean setup
            // I think, for example, for example class annotations
            // and functional operations are also a part of this???
            // IDK
            extension.hookOnBeanBegin(bean);
        }
        return extension;
    }

    private boolean hasFullAccess(Class<? extends Extension<?>> extension) {
        return false;
    }

    public void scan() {
        scan0(bean.beanClass(), true, Object.class);

        for (ExtensionSetup e : extensions.values()) {
            e.hookOnBeanEnd(bean);
        }
    }

    /**
     * @param reflectOnFields
     *            whether or not to iterate over fields
     * @param baseType
     *            the base type
     */
    final void scan0(Class<?> classToScan, boolean reflectOnFields, Class<?> baseType) {

        /** Processes all fields and methods on a class. */
        record Helper(int hash, String name, Class<?>[] parameterTypes) {

            Helper(Method method) {
                this(method.getName(), method.getParameterTypes());
            }

            Helper(String name, Class<?>[] parameterTypes) {
                this(name.hashCode() ^ Arrays.hashCode(parameterTypes), name, parameterTypes);
            }

            /** {@inheritDoc} */
            @Override
            public boolean equals(Object obj) {
                return obj instanceof Helper h && name == h.name() && Arrays.equals(parameterTypes, h.parameterTypes);
            }

            /** {@inheritDoc} */
            @Override
            public int hashCode() {
                return hash;
            }
        }

        HashSet<Package> packages = new HashSet<>();
        HashMap<Helper, HashSet<Package>> types = new HashMap<>();

        // Hmm, skal skrive noget om de kan komme i enhver order
        // Lige nu kommer metoder foerend fields

        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        for (Method m : classToScan.getMethods()) {
            // Filter methods whose from java.base module and bridge methods
            // TODO add check for
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                types.put(new Helper(m), packages);
                // Should we also ignore methods on base assembly class????
                scanMethod(m);// move this to step 2???
            }
        }

        // Step 2 process all declared methods

        // Maybe some kind of detection if current type (c) switches modules.
        for (Class<?> c = classToScan; c != baseType && c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            // First process every field
            if (reflectOnFields) {
                Field[] fields = c.getDeclaredFields();
                PackedDevToolsIntegration.INSTANCE.reflectMembers(c, fields);
                for (Field field : c.getDeclaredFields()) {
                    scanField(field);
                }
            }

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
                        scanMethod(m);
                    }
                } else if (!m.isBridge() && !m.isSynthetic()) { // TODO should we include synthetic methods??
                    switch (mod & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) {
                    case Modifier.PUBLIC:
                        continue; // we have already added the method in the first step
                    default: // default access
                        HashSet<Package> pkg = types.computeIfAbsent(new Helper(m), key -> new HashSet<>());
                        if (pkg != packages && pkg.add(c.getPackage())) {
                            break;
                        } else {
                            continue;
                        }
                    case Modifier.PROTECTED:
                        if (types.putIfAbsent(new Helper(m), packages) != null) {
                            continue; // method has been overridden by a super type
                        }
                        // otherwise fall-through
                    case Modifier.PRIVATE:
                        // Private methods are never overridden
                    }
                    scanMethod(m);
                }
            }
        }
    }

    /**
     * Look for hook annotations on a single field.
     * 
     * @param field
     *            the field to look for annotations on
     */
    private void scanField(Field field) {
        // iterate through every annotation on the field
        Annotation[] annotations = field.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            Class<? extends Annotation> annotationType = annotation.annotationType();

            // See if we can find a field hook model for the annotation
            FieldHookModel fhm = FieldHookModel.CACHE.get(annotationType);

            if (fhm != null) {
                ExtensionSetup ei = findOrCreateExtension(annotationType.getModule(), fhm.extensionType);

                boolean hasFullAccess = hasFullAccess(fhm.extensionType);
                PackedBeanField f = new PackedBeanField(BeanMemberScanner.this, ei, field, fhm.isGettable || hasFullAccess,
                        fhm.isSettable || hasFullAccess);

                // Call into Extension#hookOnBeanField
                ei.hookOnBeanField(f);
            }
        }
    }

    /**
     * Look for hook annotations on a single method.
     * 
     * @param method
     *            the method to look for annotations on
     */
    private void scanMethod(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            MethodHookModel fh = MethodHookModel.CACHE.get(a1Type);
            if (fh != null) {
                ExtensionSetup ei = findOrCreateExtension(a1Type.getModule(), fh.extensionType);
                ei.hookOnBeanMethod(new PackedBeanMethod(BeanMemberScanner.this, ei, method, fh.isInvokable));
            }
        }
    }

    record FieldHookModel(Class<? extends Extension<?>> extensionType, boolean isGettable, boolean isSettable) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<FieldHookModel> CACHE = new ClassValue<>() {

            @Override
            protected FieldHookModel computeValue(Class<?> type) {
                BeanField.AnnotatedWithHook h = type.getAnnotation(BeanField.AnnotatedWithHook.class);
                if (h == null) {
                    return null;
                }
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Class<? extends Extension<?>> cl = (Class) ClassUtil.checkProperSubclass(Extension.class, h.extension(),
                        s -> new InternalExtensionException(s));
                if (cl.getModule() != type.getModule()) {
                    throw new InternalExtensionException("The annotation " + type + " and the extension " + cl + " must be located in the same module");
                }
                return new FieldHookModel(cl, h.allowGet(), h.allowSet());
            }
        };
    }

    record MethodHookModel(Class<? extends Extension<?>> extensionType, boolean isInvokable) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<MethodHookModel> CACHE = new ClassValue<>() {

            @Override
            protected MethodHookModel computeValue(Class<?> type) {
                BeanMethod.AnnotatedWithHook h = type.getAnnotation(BeanMethod.AnnotatedWithHook.class);
                if (h == null) {
                    return null;
                }
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Class<? extends Extension<?>> cl = (Class) ClassUtil.checkProperSubclass(Extension.class, h.extension(),
                        s -> new InternalExtensionException(s));
                if (cl.getModule() != type.getModule()) {
                    throw new Error();
                }
                return new MethodHookModel(cl, h.allowInvoke());
            }
        };
    }
}
