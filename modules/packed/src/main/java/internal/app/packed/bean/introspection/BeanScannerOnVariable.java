/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.bean.introspection;

import java.lang.annotation.Annotation;
import java.util.Set;

import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.context.Context;
import app.packed.context.ContextNotAvailableException;
import app.packed.service.sandbox.ServiceResolver;
import internal.app.packed.bean.introspection.BeanTriggerModel.ParameterAnnotatedCache;
import internal.app.packed.bean.introspection.BeanTriggerModel.ParameterTypeCache;
import internal.app.packed.binding.PackedDependency;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.ServiceBindingSetup;

/**
 *
 */
final class BeanScannerOnVariable {

    static void resolveVariable(BeanScanner scanner, OperationSetup operation, Variable v, int index) {
        // Extracts the variable we want to resolve

        // First look for hook annotations on the field or parameter
        Annotation[] annotations = v.annotations().toArray();
        for (Annotation a1 : annotations) {
            Class<? extends Annotation> a1Type = a1.annotationType();
            ParameterAnnotatedCache hook = scanner.triggerModel.testParameterAnnotation(a1Type);
            if (hook != null) {
                BeanIntrospectorSetup ei = scanner.introspector(hook.bim());

                IntrospectorOnVariable h = new IntrospectorOnVariable(scanner, operation, index, ei.extension(), v);
                ei.introspector.onAnnotatedVariable(a1, h);
                return;
            }
        }

        // Peel it

        // Next, see if there are any VariableTypeHooks on the variable
        ParameterTypeCache hook = scanner.triggerModel.testParameterType(v.rawType());

        if (hook != null) {
            BeanIntrospectorSetup contributor = scanner.introspector(hook.bim());
            IntrospectorOnVariable h = new IntrospectorOnVariable(scanner, operation, index, contributor.extension(), v);
            Class<?> cl = v.rawType();
            Key<?> k = h.toKey();
            Set<Class<? extends Context<?>>> contexts = Set.of();

            for (Class<? extends Context<?>> cla : hook.requiredContexts()) {
                ContextSetup context = operation.findContext(cla);
                if (context == null)  {
                    throw new ContextNotAvailableException(operation.target + " must be in context " + cla.getCanonicalName() + ". In order to resolve " + k.type());
                }
            }
            IntrospectorOnAutoService pcs = new IntrospectorOnAutoService(k, hook.definingIfInherited() == null ? cl : hook.definingIfInherited(), contexts,
                    new IntrospectorOnVariableUnwrapped(h));
            contributor.introspector.onAutoService(k, pcs);

//            contributor.introspector.onProvide(k, hook.definingIfInherited() == null ? cl : hook.definingIfInherited(), contexts,
//                    new PackedBindableWrappedVariable(h));
            if (operation.bindings[index] != null) {
                return;
            }
        }

        // Okay guys we have a service

        // Extract needed information
        PackedDependency ia = PackedDependency.fromVariable(v);

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
