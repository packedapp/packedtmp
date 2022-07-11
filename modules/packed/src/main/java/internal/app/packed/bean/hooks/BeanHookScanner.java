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
import java.util.LinkedHashMap;

import app.packed.bean.BeanExtensionPoint.FieldHook;
import app.packed.bean.BeanExtensionPoint.MethodHook;
import app.packed.bean.BeanProcessor;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.operation.dependency.BeanDependency;
import app.packed.operation.dependency.BeanDependency.ProvisionHook;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.integrate.devtools.PackedDevToolsIntegration;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.OpenClass;

/**
 * This class is responsible for finding fields, methods, parameters that have hook annotations.
 */
public final class BeanHookScanner {

    /** We never process classes that are located in the java.base module. */
    private static final Module JAVA_BASE_MODULE = Class.class.getModule();

    /** The bean that is being scanned. */
    final BeanSetup bean;

    // I think we need stable iteration order... AppendOnly identity map, stable iteration order
    /** Every extension that is activated by a hook. */
    final LinkedHashMap<Class<? extends Extension<?>>, ExtensionEntry> extensions = new LinkedHashMap<>();

    // Should be made lazily??? I think
    final OpenClass oc;

    public BeanHookScanner(BeanSetup bean) {
        this.bean = bean;
        this.oc = OpenClass.of(MethodHandles.lookup(), bean.beanClass());
    }

    private ExtensionEntry findOrCreateEntry(Class<? extends Extension<?>> clazz) {
        return extensions.computeIfAbsent(clazz, c -> {
            ExtensionSetup extension = bean.parent.useExtensionSetup(clazz, null);
            BeanProcessor scanner = extension.newBeanScanner(extension, bean);
            scanner.onProcessStart();
            return new ExtensionEntry(extension, scanner);
        });
    }

    private boolean hasFullAccess(Class<? extends Extension<?>> extension) {
        return false;
    }

    public void scan() {
        scan0(true, Object.class);

        // Call into every BeanScanner and tell them its all over
        for (ExtensionEntry e : extensions.values()) {
            e.scanner.onProcessStop();
        }
    }

    /**
     * @param reflectOnFields
     *            whether or not to iterate over fields
     * @param baseType
     *            the base type
     */
    final void scan0(boolean reflectOnFields, Class<?> baseType) {

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
        
        Class<?> classToScan = bean.beanClass();
        HashSet<Package> packages = new HashSet<>();
        HashMap<MethodHelper, HashSet<Package>> types = new HashMap<>();

        // Hmm, skal skrive noget om de kan komme i enhver order
        // Lige nu kommer metoder foerend fields

        scanClass(classToScan);

        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        for (Method m : classToScan.getMethods()) {
            // Filter methods whose from java.base module and bridge methods
            // TODO add check for
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                types.put(new MethodHelper(m), packages);
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
                    scanMethod(m);
                }
            }
        }
    }

    private void scanClass(Class<?> clazz) {

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
            FieldHookModel hook = FieldHookModel.CACHE.get(annotationType);

            if (hook != null) {
                ExtensionEntry entry = findOrCreateEntry(hook.extensionType);

                boolean hasFullAccess = hasFullAccess(hook.extensionType);
                PackedBeanField f = new PackedBeanField(bean, BeanHookScanner.this, entry.extension, field, hook.isGettable || hasFullAccess,
                        hook.isSettable || hasFullAccess);

                entry.scanner.onField(f); // calls into BeanScanner.onField
            }
        }
    }

    void scanField2(Field field) {
        // iterate through every annotation on the field

        // Der kan vaere en settable annotering???

        Annotation[] annotations = field.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            // See if we can find a field hook model for the annotation
            FieldHookModel model1 = FieldHookModel.CACHE.get(annotations[i].annotationType());

            if (model1 != null) {
                for (int j = i + 1; j < annotations.length; j++) {
                    FieldHookModel model2 = FieldHookModel.CACHE.get(annotations[j].annotationType());

                    if (model2 != null) {

                    }
                }

                ExtensionEntry ei = findOrCreateEntry(model1.extensionType);
                boolean hasFullAccess = hasFullAccess(model1.extensionType);
                PackedBeanField f = new PackedBeanField(bean, BeanHookScanner.this, ei.extension, field, model1.isGettable || hasFullAccess,
                        model1.isSettable || hasFullAccess);

                ei.scanner.onField(f);
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
                ExtensionEntry ei = findOrCreateEntry(fh.extensionType);

                PackedBeanMethod pbm = new PackedBeanMethod(BeanHookScanner.this, ei.extension, method, fh.isInvokable);

                ei.scanner.onMethod(pbm);
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

    private record ExtensionEntry(ExtensionSetup extension, BeanProcessor scanner) {}

    record FieldHookModel(Class<? extends Annotation> annotationType, Class<? extends Extension<?>> extensionType, boolean isGettable, boolean isSettable,
            boolean isProvision) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<FieldHookModel> CACHE = new ClassValue<>() {

            @Override
            protected FieldHookModel computeValue(Class<?> type) {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> annotationType = (Class<? extends Annotation>) type;
                FieldHook fieldHook = type.getAnnotation(FieldHook.class);
                BeanDependency.ProvisionHook provisionHook = type.getAnnotation(BeanDependency.ProvisionHook.class);

                if (provisionHook == fieldHook) { // check both null
                    return null;
                } else if (provisionHook == null) {
                    checkExtensionClass(type, fieldHook.extension());
                    return new FieldHookModel(annotationType, fieldHook.extension(), fieldHook.allowGet(), fieldHook.allowSet(), false);
                } else if (fieldHook == null) {
                    checkExtensionClass(type, provisionHook.extension());
                    return new FieldHookModel(annotationType, provisionHook.extension(), false, true, true);
                } else {
                    throw new InternalExtensionException(type + " cannot both be annotated with " + FieldHook.class + " and " + ProvisionHook.class);
                }
            }
        };
    }

    record MethodHookModel(Class<? extends Extension<?>> extensionType, boolean isInvokable) {

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
