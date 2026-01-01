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
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Optional;

import app.packed.bean.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.operation.OperationInstaller;
import internal.app.packed.bean.scanning.BeanTriggerModel.OnAnnotatedMethodCache;
import internal.app.packed.operation.OperationMemberTarget.OperationMethodTarget;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.util.PackedAnnotationList;

/** Internal implementation of BeanMethod. Discard after use. */
public final class IntrospectorOnMethod extends IntrospectorOnExecutable<Method> implements BeanIntrospector.OnMethod {

    IntrospectorOnMethod(BeanIntrospectorSetup participant, Method method, Annotation[] annotations, boolean allowInvoke) {
        super(participant, method, annotations);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Method> method() {
        return Optional.ofNullable(member);
    }

    /** {@inheritDoc} */
    @Override
    public OperationInstaller newOperation() {
        checkConfigurable();

        // Attempt to unreflect the method (Create a direct method handle for it)
        MethodHandle directMH = introspector.scanner.unreflectMethod(member);

        // We should be able to create this lazily
        return PackedOperationTemplate.newInstaller(introspector, directMH, new OperationMethodTarget(member), type);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        return Key.fromMethodReturnType(method().get());
    }

    /**
     * Look for hook annotations on a single method.
     *
     * @param method
     *            the method to look for annotations on
     */
    static void introspectMethodForAnnotations(BeanScanner iBean, Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();

            OnAnnotatedMethodCache am = iBean.triggerModel.testMethod(a1Type);
            if (am != null) {
                BeanIntrospectorSetup contributor = iBean.introspector(am.bim());

                IntrospectorOnMethod pbm = new IntrospectorOnMethod(contributor, method, annotations, am.isInvokable());
                PackedAnnotationList pac = new PackedAnnotationList(a1);
                contributor.introspector.onAnnotatedMethod(pac, pbm);
            }
        }
    }
}
