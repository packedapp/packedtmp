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
import java.lang.reflect.Method;
import java.util.function.Function;

import app.packed.bean.BeanElement.BeanMethod;
import app.packed.binding.Key;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.BeanHookModel.AnnotatedMethod;
import internal.app.packed.operation.OperationMemberTarget.OperationMethodTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationInstaller;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.util.PackedAnnotationList;

/** Internal implementation of BeanMethod. Discard after use. */
public final class PackedBeanMethod extends PackedBeanExecutable<Method> implements BeanMethod {

    PackedBeanMethod(BeanScannerExtensionRef contributor, Method method, Annotation[] annotations, boolean allowInvoke) {
        super(contributor, method, annotations);
    }

    /** {@inheritDoc} */
    @Override
    public Method method() {
        return member;
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate.Installer newOperation(OperationTemplate template) {
        requireNonNull(template);
        checkConfigurable();

        // We should be able to create this lazily
        MethodHandle methodHandle = extension.scanner.unreflectMethod(member);

        return new PackedOperationInstaller(((PackedOperationTemplate) template), operationType(), extension.scanner.bean, extension.extension) {

            @SuppressWarnings("unchecked")
            @Override
            public final <H extends OperationHandle<?>> H install(Function<? super OperationTemplate.Installer, H> configurationCreator) {

                OperationSetup operation = OperationSetup.newMemberOperationSetup(this, new OperationMethodTarget(member), methodHandle, configurationCreator);
//              extension.scanner.bean.operations.all.add(operation);
                extension.scanner.unBoundOperations.add(operation);
                return (H) (this.oh = operation.handle());
            }
        };
//
//        PackedOperationInstaller poi = ((PackedOperationTemplate) template).newInstaller(operationType(), extension.scanner.bean, extension.extension);
//
//        return new PackedOperationInstaller(po) {
//
//            @Override
//            public OperationHandle<?> install() {
//
//            }};

    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        return Key.fromMethodReturnType(this);
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
                BeanScannerExtensionRef contributor = iBean.computeContributor(fh.extensionType());

                PackedBeanMethod pbm = new PackedBeanMethod(contributor, method, annotations, fh.isInvokable());
                PackedAnnotationList pac = new PackedAnnotationList(new Annotation[] { a1 });
                contributor.introspector.triggeredByAnnotatedMethod(pac, pbm);
            }
        }
    }
}
