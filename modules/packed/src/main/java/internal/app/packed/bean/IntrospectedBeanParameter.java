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
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.BeanHookModel.ParameterTypeRecord;
import internal.app.packed.bean.IntrospectedBean.Contributor;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.ExtensionServiceBindingSetup;
import internal.app.packed.operation.binding.InternalDependency;

/**
 *
 */
final class IntrospectedBeanParameter {

    public static void resolveParameter(IntrospectedBean iBean, OperationSetup operation, int index) {
        // Extracts the variable we want to resolve
        Variable var = operation.type.parameter(index);

        // First, try and see if there are any binding annotations we can use
        if (tryResolveAsAnnotatedBindingHook(iBean, var, operation, index)) {
            return;
        }

        // Next, let us see if the parameter type is a binding class 
        ParameterTypeRecord hook = iBean.hookModel.lookupParameterType(var.getRawType());
        if (hook != null) {
            Contributor contributor = iBean.computeContributor(hook.extensionType(), false);
            IntrospectedBeanVariable h = new IntrospectedBeanVariable(iBean, operation, index, contributor.extension(), var.getRawType(), var);
            contributor.introspector().onVariableProvideRaw(h);
            if (operation.bindings[index] != null) {
                return;
            }
        }

        // Finally, we resolve it as a service
        boolean resolveAsService = operation.operator.extensionType == BaseExtension.class;

        if (resolveAsService) {
            InternalDependency ia = InternalDependency.fromOperationType(operation.type).get(index);
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
    private static boolean tryResolveAsAnnotatedBindingHook(IntrospectedBean introspector, Variable var, OperationSetup os, int index) {
        Annotation[] annotations = var.getAnnotations();

        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            ParameterTypeRecord hook = introspector.hookModel.lookupParameterType(a1Type);
            if (hook != null) {
                Contributor ei = introspector.computeContributor(hook.extensionType(), false);

                IntrospectedBeanVariable h = new IntrospectedBeanVariable(introspector, os, index, ei.extension(), a1Type, var);
                ei.introspector().onVariableProvideRaw(h);
                return true;
            }
        }
        return false;
    }
}
