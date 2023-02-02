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

import app.packed.bindings.Variable;
import app.packed.extension.Extension;
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

    static void resolveBinding(BeanScanner iBean, OperationSetup operation, int index) {

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
            OperationalExtension contributor = iBean.computeContributor(hook.extensionType());
            PackedBindableVariable h = new PackedBindableVariable(iBean, operation, index, contributor.extension, v);

            contributor.introspector.hookOnProvidedVariableType(v.getRawType(), new PackedBindableBaseVariable(h));
            if (operation.bindings[index] != null) {
                return;
            }
        }

        // Finally, we resolve it as a service
        InternalDependency ia = InternalDependency.fromVariable(v);

        Class<? extends Extension<?>> e = operation.bean.ownedBy == null ? null : operation.bean.ownedBy.extensionType;// .operator.extensionType;//
                                                                                                                       // operation.bean.ownedBy.extensionType;
        if (operation.parent != null) {
            e = operation.operator.extensionType;
        }

        if (e == null) {
            operation.bindings[index] = iBean.bean.container.sm.bind(ia.key(), !ia.isOptional(), operation, index);
        } else {
            ExtensionSetup es = operation.bean.container.extensions.get(e);
            operation.bindings[index] = es.sm.bind(ia.key(), !ia.isOptional(), operation, index);
//
//            ExtensionServiceBindingSetup b = new ExtensionServiceBindingSetup(operation, index, v.getRawType());
//            operation.bindings[index] = b;
//            operation.operator.sm.bindings.add(b);
        }
    }

    /**
     * Look for hook annotations on a variable)
     *
     * @param var
     *            the method to look for annotations on
     * @return
     */
    private static boolean tryResolveWithBindingAnnotation(BeanScanner introspector, Variable var, OperationSetup os, int index) {
        Annotation[] annotations = var.getAnnotations();

        for (Annotation a1 : annotations) {
            Class<? extends Annotation> a1Type = a1.annotationType();
            AnnotatedParameterType hook = introspector.hookModel.testParameterAnnotation(a1Type);
            if (hook != null) {
                OperationalExtension ei = introspector.computeContributor(hook.extensionType());

                PackedBindableVariable h = new PackedBindableVariable(introspector, os, index, ei.extension, var);
                ei.introspector.hookOnProvidedAnnotatedVariable(a1, h);
                return true;
            }
        }
        return false;
    }
}
