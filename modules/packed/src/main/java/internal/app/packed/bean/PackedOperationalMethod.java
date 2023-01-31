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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.bean.BeanIntrospector.OperationalMethod;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.bindings.Key;
import app.packed.operation.DelegatingOperationHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.BeanHookModel.AnnotatedMethod;
import internal.app.packed.operation.OperationMemberTarget.OperationMethodTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;
import internal.app.packed.operation.PackedDelegatingOperationHandle;
import internal.app.packed.service.KeyHelper;

/** Internal implementation of BeanMethod. Discard after use. */
public final class PackedOperationalMethod extends PackedOperationalExecutable<Method> implements OperationalMethod {

    PackedOperationalMethod(OperationalExtension contributor, Method method, Annotation[] annotations, boolean allowInvoke) {
        super(contributor, method, annotations);
    }

    /** {@inheritDoc} */
    @Override
    public Method method() {
        return member;
    }

    /** {@inheritDoc} */
    @Override
    public DelegatingOperationHandle newDelegatingOperation() {
        checkConfigurable();

        // We should be able to create this lazily
        // Probably need to store the lookup mechanism on the bean...
        BeanScanner scanner = extension.scanner;
        MethodHandle methodHandle;
        Lookup lookup = scanner.oc.lookup(member);
        try {
            methodHandle = lookup.unreflect(member);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("stuff", e);
        }

        PackedDelegatingOperationHandle h = new PackedDelegatingOperationHandle(extension.extension, extension.scanner.bean, new OperationMethodTarget(member),
                operationType(), methodHandle);
        return h;
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(OperationTemplate template) {
        requireNonNull(template);
        checkConfigurable();

        // We should be able to create this lazily
        // Probably need to store the lookup mechanism on the bean...
        BeanScanner scanner = extension.scanner;
        MethodHandle methodHandle;
        Lookup lookup = scanner.oc.lookup(member);
        try {
            methodHandle = lookup.unreflect(member);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("stuff", e);
        }

        OperationSetup operation = new MemberOperationSetup(extension.extension, scanner.bean, operationType(), template, new OperationMethodTarget(member),
                methodHandle);
        scanner.bean.operations.add(operation);
        scanner.unBoundOperations.add(operation);
        return operation.toHandle();
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        return KeyHelper.convert(member.getGenericReturnType(), member.getAnnotations(), this);
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
            AnnotatedMethod fh = iBean.hookModel.testMethodAnnotation(a1Type);
            if (fh != null) {
                OperationalExtension contributor = iBean.computeContributor(fh.extensionType());

                PackedOperationalMethod pbm = new PackedOperationalMethod(contributor, method, annotations, fh.isInvokable());
                PackedAnnotationList pac = new PackedAnnotationList(new Annotation[] { a1 });
                contributor.introspector.hookOnAnnotatedMethod(pac, pbm);
            }
        }
    }
}
