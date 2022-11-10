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

import app.packed.bean.BeanExtension;
import app.packed.operation.Variable;
import internal.app.packed.bean.BeanHookModel.ParameterTypeRecord;
import internal.app.packed.bean.IntrospectedBean.Contributor;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.ExtensionServiceBindingSetup;
import internal.app.packed.service.old.InternalDependency;

/**
 *
 */
final class IntrospectedBeanParameter {

    public static void resolveParameter(IntrospectedBean iBean, OperationSetup operation, int index) {
        Variable var = operation.type.parameter(index);

        // if isComposit
        // Create a new CompositeBinding
        // Create the new OperationSetup();
        // bindIt

        // Look for annotations on the parameter
        if (tryResolveAsBindingHook(iBean, var, operation, index)) {
            return;
        }

        // See if the type is something we should care about
        ParameterTypeRecord hook = iBean.hookModel.lookupParameterType(var.getType());
        if (hook != null) {
            Contributor contributor = iBean.computeContributor(hook.extensionType(), false);
            IntrospectedBeanBinding h = new IntrospectedBeanBinding(iBean, operation, index, contributor.extension(), var.getType(), var);
            contributor.introspector().onBinding(h);
            if (operation.bindings[index] != null) {
                return;
            }
        }

        // System.out.println("Resolve as service " + var + " for " + operation.operator.extensionType);
        // finally resolve as service

        // Okay we calling in here with extension services as well.
        // Need to handle it

        boolean resolveAsService = operation.operator.extensionType == BeanExtension.class;

        if (resolveAsService) {
            InternalDependency ia = InternalDependency.fromOperationType(operation.type).get(index);
            operation.bindings[index] = iBean.bean.container.sm.serviceBind(ia.key(), !ia.isOptional(), operation, index);
        } else {
            ExtensionServiceBindingSetup b = new ExtensionServiceBindingSetup(operation, index, var.getType());
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
    private static boolean tryResolveAsBindingHook(IntrospectedBean introspector, Variable var, OperationSetup os, int index) {

        Annotation[] annotations = var.getAnnotations();

        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            ParameterTypeRecord hook = introspector.hookModel.lookupParameterType(a1Type);
            if (hook != null) {
                Contributor ei = introspector.computeContributor(hook.extensionType(), false);

                IntrospectedBeanBinding h = new IntrospectedBeanBinding(introspector, os, index, ei.extension(), a1Type, var);
                ei.introspector().onBinding(h);
                return true;
            }
        }
        return false;
    }

}
