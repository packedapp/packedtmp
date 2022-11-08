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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.base.Nullable;
import app.packed.bean.BeanIntrospector.AnnotationReader;
import app.packed.bean.BeanIntrospector.OnMethod;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanHookModel.AnnotatedMethod;
import internal.app.packed.bean.IntrospectedBean.Contributor;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationTarget.MethodOperationTarget;

/** Internal implementation of BeanMethod. Discard after use. */
public final class IntrospectedBeanMethod implements OnMethod {

    /** Annotations on the method read via {@link Method#getAnnotations()}. */
    private final Annotation[] annotations;

    /** The extension that will operate any operations. */
    public final Contributor contributor;

    /** The internal introspector */
    public final IntrospectedBean introspectedBean;

    /** The underlying method. */
    private final Method method;

    /** The operation type (lazily created). */
    @Nullable
    private OperationType type;

    IntrospectedBeanMethod(IntrospectedBean analyzer, Contributor contributor, Method method, Annotation[] annotations, boolean allowInvoke) {
        this.introspectedBean = analyzer;
        this.contributor = contributor;
        this.method = method;
        this.annotations = annotations;
    }
    

    /**
     * Look for hook annotations on a single method.
     * 
     * @param method
     *            the method to look for annotations on
     */
    static void introspectMethodForAnnotations(IntrospectedBean iBean, Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            Class<? extends Annotation> a1Type = a1.annotationType();
            AnnotatedMethod fh = iBean.hookModel.lookupAnnotationOnMethod(a1Type);
            if (fh != null) {
                Contributor contributor = iBean.computeContributor(fh.extensionType(), false);

                IntrospectedBeanMethod pbm = new IntrospectedBeanMethod(iBean, contributor, method, annotations, fh.isInvokable());

                contributor.introspector().onMethod(pbm);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationReader annotations() {
        return new BeanAnnotationReader(annotations);
    }

    /** {@return modifiers for the member.} */
    public int getModifiers() {
        return method.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInvokeAccess() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Method method() {
        return method;
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation() {
        // TODO check that we are still introspecting? Or maybe on bean.addOperation

        MethodHandle methodHandle;
        Lookup lookup = introspectedBean.oc.lookup(method);
        try {
            methodHandle = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("stuff", e);
        }

        MethodOperationTarget mot = new MethodOperationTarget(methodHandle, method);
        OperationSetup bos = new OperationSetup(introspectedBean.bean, operationType(), contributor.extension(), mot, null);
        introspectedBean.bean.operations.add(bos);
        introspectedBean.unBoundOperations.add(bos);
        return bos.toHandle();
    }

    /** {@inheritDoc} */
    @Override
    public OperationType operationType() {
        OperationType t = type;
        if (t == null) {
            t = type = OperationType.ofExecutable(method);
        }
        return t;
    }
}
