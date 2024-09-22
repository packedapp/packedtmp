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
import app.packed.extension.Extension;
import internal.app.packed.bean.BeanHookModel.AnnotatedParameterType;
import internal.app.packed.bean.BeanHookModel.ParameterType;
import internal.app.packed.binding.InternalDependency;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
final class BeanScannerBindingResolver {

    static void resolveBinding(BeanScanner scanner, OperationSetup operation, int index) {
        // Extracts the variable we want to resolve
        Variable v = operation.type.parameter(index);

        Annotation[] annotations = v.annotations().toArray();
        for (Annotation a1 : annotations) {
            Class<? extends Annotation> a1Type = a1.annotationType();
            AnnotatedParameterType hook = scanner.hookModel.testParameterAnnotation(a1Type);
            if (hook != null) {
                BeanScannerExtensionRef ei = scanner.computeContributor(hook.extensionType());

                PackedBindableVariable h = new PackedBindableVariable(scanner, operation, index, ei.extension, v);
                ei.introspector.activatedByAnnotatedVariable(a1, h);
                return;
            }
        }

        // Peel it

        // Next, see if there are any VariableTypeHooks on the variable
        ParameterType hook = scanner.hookModel.testParameterType(v.rawType());

        if (hook != null) {
            BeanScannerExtensionRef contributor = scanner.computeContributor(hook.extensionType());
            PackedBindableVariable h = new PackedBindableVariable(scanner, operation, index, contributor.extension, v);

            Class<?> cl = v.rawType();
            contributor.introspector.activatedByVariableType(cl, hook.definingIfInherited() == null ? cl : hook.definingIfInherited(),
                    new PackedBindableWrappedVariable(h));
            if (operation.bindings[index] != null) {
                return;
            }
        }

        // Finally, we resolve it as a service
        InternalDependency ia = InternalDependency.fromVariable(v);

        BeanSetup bean = operation.bean;
        AuthoritySetup owner = operation.bean.owner;

        Class<? extends Extension<?>> e = owner instanceof ExtensionSetup es ? es.extensionType : null;
        if (operation.embeddedInto != null) {
            e = operation.operator.extensionType;
        }

//        bean.sns().bind(ia.key(), !ia.isOptional(), operation, index);
        if (e == null) {
            operation.bindings[index] = bean.container.servicesMain().bind(ia.key(), !ia.isOptional(), operation, index);
        } else {
            ExtensionSetup es = operation.bean.container.extensions.get(e);
            operation.bindings[index] = es.sm().bind(ia.key(), !ia.isOptional(), operation, index);
        }
    }
}
