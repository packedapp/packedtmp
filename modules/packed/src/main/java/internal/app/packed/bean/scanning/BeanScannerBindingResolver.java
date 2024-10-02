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
package internal.app.packed.bean.scanning;

import java.lang.annotation.Annotation;
import java.util.Set;

import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.context.Context;
import app.packed.service.advanced.ServiceResolver;
import internal.app.packed.bean.scanning.BeanHookModel.AnnotatedParameterType;
import internal.app.packed.bean.scanning.BeanHookModel.ParameterType;
import internal.app.packed.binding.InternalDependency;
import internal.app.packed.binding.PackedBindableVariable;
import internal.app.packed.binding.PackedBindableWrappedVariable;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.ServiceBindingSetup;

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
                BeanScannerParticipant ei = scanner.computeContributor(hook.extensionType());

                PackedBindableVariable h = new PackedBindableVariable(scanner, operation, index, ei.extension, v);
                ei.introspector.onAnnotatedVariable(a1, h);
                return;
            }
        }

        // Peel it

        // Next, see if there are any VariableTypeHooks on the variable
        ParameterType hook = scanner.hookModel.testParameterType(v.rawType());

        if (hook != null) {
            BeanScannerParticipant contributor = scanner.computeContributor(hook.extensionType());
            PackedBindableVariable h = new PackedBindableVariable(scanner, operation, index, contributor.extension, v);

            Class<?> cl = v.rawType();
            Key<?> k = h.toKey();
            Set<Class<? extends Context<?>>> contexts = Set.of();
            contributor.introspector.onContextualServiceProvision(k, hook.definingIfInherited() == null ? cl : hook.definingIfInherited(),
                    contexts,
                    new PackedBindableWrappedVariable(h));
            if (operation.bindings[index] != null) {
                return;
            }
        }

        // Okay guys we have a service

        // Extract needed information
        InternalDependency ia = InternalDependency.fromVariable(v);

        // Let's see if we have specified a special service resolver on the binding
        ServiceResolver sr = v.annotations().read(ServiceResolver.class).orElse(ServiceResolver.DEFAULT);

        Key<?> key = ia.key();
        boolean isRequired = !ia.isOptional();

        // Create the new binding, bind it to the operation, and register it for resolution later
        ServiceBindingSetup binding = new ServiceBindingSetup(key, operation, index, isRequired, sr);
        operation.bindings[index] = binding;
        operation.bean.owner.servicesToResolve.add(binding);
    }
}
