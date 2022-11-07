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

import app.packed.operation.Variable;
import internal.app.packed.bean.AssemblyMetaModel.ParameterTypeRecord;
import internal.app.packed.bean.IntrospectedBean.Contributor;
import internal.app.packed.oldservice.inject.InternalDependency;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class IntrospectedBeanParameter {

    public static void bind(IntrospectedBean introspector, OperationSetup os, int index) {
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
        ParameterTypeRecord fh = introspector.assemblyMetaModel.lookupParameterType(var.getType());// .lookupParameterCache(var.getType());
        if (fh != null) {
            Contributor ei = introspector.computeContributor(fh.extensionType(), false);
            IntrospectedBeanBinding h = new IntrospectedBeanBinding(os, index, ei.extension(), var.getType(), var);
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
    private static void introspectForHookAnnotations(IntrospectedBean introspector, Variable var, OperationSetup os, int index) {
        Annotation[] annotations = var.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            ParameterTypeRecord fh = introspector.assemblyMetaModel.lookupParameterType(var.getType());
            if (fh != null) {
                Contributor ei = introspector.computeContributor(fh.extensionType(), false);

                IntrospectedBeanBinding h = new IntrospectedBeanBinding(os, index, ei.extension(), a1Type, var);
                ei.introspector().onBinding(h);
            }
        }
    }

}
