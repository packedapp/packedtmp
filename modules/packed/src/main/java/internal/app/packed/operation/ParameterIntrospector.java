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
package internal.app.packed.operation;

import java.lang.annotation.Annotation;

import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.container.Extension;
import app.packed.operation.Variable;
import internal.app.packed.bean.BindingIntrospector;
import internal.app.packed.bean.Introspector;
import internal.app.packed.bean.Introspector.Delegate;
import internal.app.packed.service.inject.InternalDependency;

/**
 *
 */
public class ParameterIntrospector {

    public static void bind(Introspector introspector, OperationSetup os, int index) {
        Variable var = os.type.parameter(index);

        // if isComposit
        // Create a new CompositeBinding
        // Create the new OperationSetup();
        // bindIt

        // Else look for prime annotations
        introspectForHookAnnotations(introspector, var, os, index);

        // Else look if binding
        // Maybe not look in cache for java thingies?
        if (os.bindings[index] != null) {
            return;
        }

        ParameterAnnotationCache fh = ParameterAnnotationCache.CACHE.get(var.getType());
        if (fh != null) {
            System.out.println("Got something");
            Delegate ei = introspector.computeExtensionEntry(fh.extensionType, false);
            BindingIntrospector h = new BindingIntrospector(os, index, ei.extension(), var.getType(), var);
            ei.introspector().onBinding(h);
        }
        if (os.bindings[index] != null) {
            return;
        }

        // finally resolve as service
        InternalDependency ia = InternalDependency.fromOperationType(os.type).get(index);
        os.bindings[index] = introspector.bean.container.sm.serviceBind(ia.key(), !ia.isOptional(), os, index);
    }

    /**
     * Look for hook annotations on a single method.
     * 
     * @param var
     *            the method to look for annotations on
     */
    private static void introspectForHookAnnotations(Introspector introspector, Variable var, OperationSetup os, int index) {
        Annotation[] annotations = var.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            ParameterAnnotationCache fh = ParameterAnnotationCache.CACHE.get(a1Type);
            if (fh != null) {
                Delegate ei = introspector.computeExtensionEntry(fh.extensionType, false);

                BindingIntrospector h = new BindingIntrospector(os, index, ei.extension(), a1Type, var);
                ei.introspector().onBinding(h);
            }
        }
    }

    private record ParameterAnnotationCache(Class<? extends Extension<?>> extensionType) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<ParameterAnnotationCache> CACHE = new ClassValue<>() {

            @Override
            protected ParameterAnnotationCache computeValue(Class<?> type) {
                BindingHook h = type.getAnnotation(BindingHook.class);
                if (h == null) {
                    return null;
                }
                // checkExtensionClass(type, h.extension());
                return new ParameterAnnotationCache(h.extension());
            }
        };
    }

}
