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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import app.packed.bean.hooks.BeanField;
import app.packed.bean.hooks.BeanMethod;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import packed.internal.bean.BeanSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.util.ClassUtil;
import packed.internal.util.MemberScanner;
import packed.internal.util.OpenClass;

/**
 * Scans members on a bean class.
 */
public final class BeanScanner {

    /** The bean that is being installed. */
    final BeanSetup bean;

    /** The class of the bean we are scanning. */
    private final Class<?> beanClass;

    /** The container the bean is being installed into. */
    private final ContainerSetup container;

    // I think we need stable iteration order... AppendOnly identity map, stable iteration order
    final LinkedHashMap<Class<? extends Extension<?>>, ExtensionWrapper> extensions = new LinkedHashMap<>();

    final OpenClass oc;

    public BeanScanner(BeanSetup bean, Class<?> cl) {
        this.bean = bean;
        this.container = bean.parent;
        this.beanClass = cl;
        this.oc = OpenClass.of(MethodHandles.lookup(), cl);
    }

    private void onField(Field field) {
        Annotation[] annotations = field.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            FieldHook fh = FieldHook.CACHE.get(a1Type);
            if (fh != null) {
                ExtensionWrapper ei = wrapper(a1Type.getModule(), fh.extensionType);
                ei.extension.hookOnBeanField(
                        new PackedBeanField(BeanScanner.this, ei.extension, field, fh.isGettable || ei.hasFullAccess, fh.isSettable || ei.hasFullAccess));
            }
        }
    }

    private void onMethod(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            MethodHook fh = MethodHook.CACHE.get(a1Type);
            if (fh != null) {
                ExtensionWrapper ei = wrapper(a1Type.getModule(), fh.extensionType);
                ei.extension.hookOnBeanMethod(new PackedBeanMethod(BeanScanner.this, ei.extension, method, fh.isInvokable));
            }
        }
    }

    public void scan() {
        new MemberScanner(beanClass) {

            @Override
            protected void onField(Field field) {
                BeanScanner.this.onField(field);
            }

            /** {@inheritDoc} */
            @Override
            protected void onMethod(Method method) {
                BeanScanner.this.onMethod(method);
            }
        }.scan(true, Object.class);

        for (ExtensionWrapper e : extensions.values()) {
            e.extension.hookOnBeanEnd(bean);
        }
    }

    private ExtensionWrapper wrapper(Module owner, Class<? extends Extension<?>> clazz) {
        ExtensionWrapper ei = extensions.get(clazz);
        if (ei == null) {
            ExtensionSetup es = container.useExtensionSetup(clazz, null);
            ei = new ExtensionWrapper(es);
            extensions.put(clazz, ei);
            ei.extension.hookOnBeanBegin(bean);
        }
        return ei;
    }

    private static class ExtensionWrapper {
        public final ExtensionSetup extension;
        boolean hasFullAccess;

        ExtensionWrapper(ExtensionSetup extension) {
            this.extension = requireNonNull(extension);
        }
    }

    record FieldHook(Class<? extends Extension<?>> extensionType, boolean isGettable, boolean isSettable) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<FieldHook> CACHE = new ClassValue<>() {

            @Override
            protected FieldHook computeValue(Class<?> type) {
                BeanField.AnnotatedWithHook h = type.getAnnotation(BeanField.AnnotatedWithHook.class);
                if (h == null) {
                    return null;
                }
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Class<? extends Extension<?>> cl = (Class) ClassUtil.checkProperSubclass(Extension.class, h.extension());
                if (cl.getModule() != type.getModule()) {
                    throw new InternalExtensionException("The annotation " + type + " and the extension " + cl + " must be located in the same module");
                }
                return new FieldHook(cl, h.allowGet(), h.allowSet());
            }
        };
    }

    record MethodHook(Class<? extends Extension<?>> extensionType, boolean isInvokable) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<MethodHook> CACHE = new ClassValue<>() {

            @Override
            protected MethodHook computeValue(Class<?> type) {
                BeanMethod.AnnotatedWithHook h = type.getAnnotation(BeanMethod.AnnotatedWithHook.class);
                if (h == null) {
                    return null;
                }
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Class<? extends Extension<?>> cl = (Class) ClassUtil.checkProperSubclass(Extension.class, h.extension());
                if (cl.getModule() != type.getModule()) {
                    throw new Error();
                }
                return new MethodHook(cl, h.allowInvoke());
            }
        };
    }
}
