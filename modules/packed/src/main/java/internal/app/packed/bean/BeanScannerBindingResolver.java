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

import app.packed.extension.Extension;
import app.packed.util.Variable;
import internal.app.packed.bean.BeanHookModel.AnnotatedParameterType;
import internal.app.packed.bean.BeanHookModel.ParameterType;
import internal.app.packed.binding.InternalDependency;
import internal.app.packed.binding.PackedBindableVariable;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
final class BeanScannerBindingResolver {

    static void resolveBinding(BeanReflector iBean, OperationSetup operation, int index) {

        // Extracts the variable we want to resolve
        Variable v = operation.type.parameter(index);

        // First, see if there are AnnotatedVariableHooks on the variable
        if (tryResolveWithBindingAnnotation(iBean, v, operation, index)) {
            return;
        }

        // Peel it

        // Next, see if there are any VariableTypeHooks on the variable
        ParameterType hook = iBean.hookModel.testParameterType(v.getRawType());
        if (hook != null) {
            BeanScannerExtension contributor = iBean.computeContributor(hook.extensionType());
            PackedBindableVariable h = new PackedBindableVariable(iBean, operation, index, contributor.extension, v);

            contributor.introspector.hookOnVariableType(v.getRawType(), new PackedBeanWrappedVariable(h));
            if (operation.bindings[index] != null) {
                return;
            }
        }

        // Finally, we resolve it as a service
        InternalDependency ia = InternalDependency.fromVariable(v);

        BeanSetup bean = operation.bean;
        BeanOwner owner = operation.bean.owner;

        Class<? extends Extension<?>> e = owner instanceof ExtensionSetup es ? es.extensionType : null;
        if (operation.embeddedInto != null) {
            e = operation.operator.extensionType;
        }

        if (e == null) {
            operation.bindings[index] = bean.container.sm.bind(ia.key(), !ia.isOptional(), operation, index);
        } else {
            ExtensionSetup es = operation.bean.container.extensions.get(e);
            operation.bindings[index] = es.sm.bind(ia.key(), !ia.isOptional(), operation, index);
        }
    }

    /**
     * Look for hook annotations on a variable)
     *
     * @param var
     *            the method to look for annotations on
     * @return
     */
    private static boolean tryResolveWithBindingAnnotation(BeanReflector introspector, Variable var, OperationSetup os, int index) {
        Annotation[] annotations = var.annotations().toArray();

        for (Annotation a1 : annotations) {
            Class<? extends Annotation> a1Type = a1.annotationType();
            AnnotatedParameterType hook = introspector.hookModel.testParameterAnnotation(a1Type);
            if (hook != null) {
                BeanScannerExtension ei = introspector.computeContributor(hook.extensionType());

                PackedBindableVariable h = new PackedBindableVariable(introspector, os, index, ei.extension, var);
                ei.introspector.hookOnAnnotatedVariable(a1, h);
                return true;
            }
        }
        return false;
    }
}
