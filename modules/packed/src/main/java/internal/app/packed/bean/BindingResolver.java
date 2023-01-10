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

import app.packed.binding.Variable;
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.BeanHookModel.AnnotatedParameterType;
import internal.app.packed.bean.BeanHookModel.ParameterType;
import internal.app.packed.bean.IntrospectedBean.Contributor;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.ExtensionServiceBindingSetup;
import internal.app.packed.operation.binding.InternalDependency;

/**
 *
 */
final class BindingResolver {

    static void resolveBinding(IntrospectedBean iBean, OperationSetup operation, int index) {

        // Extracts the variable we want to resolve
        Variable var = operation.type.parameter(index);

        // First, see if there are AnnotatedVariableHooks on the variable
        if (tryResolveWithBindingAnnotation(iBean, var, operation, index)) {
            return;
        }

        // Peel it
        
        // Next, see if there are any VariableTypeHooks on the variable
        ParameterType hook = iBean.hookModel.testParameterType(var.getRawType());
        if (hook != null) {
            Contributor contributor = iBean.computeContributor(hook.extensionType(), false);
            IntrospectedBeanVariable h = new IntrospectedBeanVariable(iBean, operation, index, contributor.extension(), var);
            contributor.introspector().hookOnAnnotatedVariable(null, h);
            if (operation.bindings[index] != null) {
                return;
            }
        }
        
        // Finally, we resolve it as a service
        boolean resolveAsService = operation.operator.extensionType == BaseExtension.class;
        InternalDependency ia = InternalDependency.fromVariable(var);
        
        if (resolveAsService) {
            operation.bindings[index] = iBean.bean.container.sm.serviceBind(ia.key(), !ia.isOptional(), operation, index);
        } else {
            ExtensionServiceBindingSetup b = new ExtensionServiceBindingSetup(operation, index, var.getRawType());
            operation.bindings[index] = b;
            operation.operator.bindings.add(b);
        }
    }

    /**
     * Look for hook annotations on a variable)
     * 
     * @param var
     *            the method to look for annotations on
     * @return
     */
    private static boolean tryResolveWithBindingAnnotation(IntrospectedBean introspector, Variable var, OperationSetup os, int index) {
        Annotation[] annotations = var.getAnnotations();

        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            AnnotatedParameterType hook = introspector.hookModel.testParameterAnnotation(a1Type);
            if (hook != null) {
                Contributor ei = introspector.computeContributor(hook.extensionType(), false);

                IntrospectedBeanVariable h = new IntrospectedBeanVariable(introspector, os, index, ei.extension(), var);
                ei.introspector().hookOnAnnotatedVariable(a1, h);
                return true;
            }
        }
        return false;
    }
}
