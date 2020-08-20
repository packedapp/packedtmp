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
package app.packed.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.InvalidDeclarationException;
import app.packed.base.Nullable;
import app.packed.introspection.MethodDescriptor;
import packed.internal.hook.MemberUnreflector;
import packed.internal.hook.applicator.MethodOperator;
import packed.internal.hook.applicator.PackedMethodHookApplicator;
import packed.internal.util.StringFormatter;

/** A hook representing a method annotated with a specific type. */
public final class AnnotatedMethodHook<A extends Annotation> implements Hook {

    /** The annotation value. */
    private final A annotation;

    /** A method descriptor, is lazily created via {@link #method()}. */
    @Nullable
    private MethodDescriptor descriptor;

    /** The annotated method. */
    private final Method method;

    /** The builder for the component type. */
    private final MemberUnreflector processor;

    /**
     * Creates a new hook instance.
     * 
     * @param controller
     *            the builder for the component type
     * @param method
     *            the annotated method
     * @param annotation
     *            the annotation value
     */
    AnnotatedMethodHook(MemberUnreflector controller, Method method, A annotation) {
        this.processor = requireNonNull(controller);
        this.method = requireNonNull(method);
        this.annotation = requireNonNull(annotation);
    }

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    public A annotation() {
        return annotation;
    }

    public <E> HookApplicator<E> applicator(MethodOperator<E> operator) {
        processor.checkOpen();
        return new PackedMethodHookApplicator<E>(this, operator, method);
    }

    // /**
    // * Applies the specified operator to the underlying method.
    // *
    // * @param <E>
    // * the type of result from applying the operator
    // * @param operator
    // * the operator to apply
    // * @return the result from applying the operator to the static method
    // * @throws UnsupportedOperationException
    // * if the underlying method is not a static method
    // * @throws UncheckedIllegalAccessException
    // * if access checking failed while applying the operator
    // */
    // public <E> E applyStatic(MethodOperator<E> operator) {
    // if (!Modifier.isStatic(method.getModifiers())) {
    // throw new IllegalArgumentException("Cannot invoke this method on a non-static method, method = " + method);
    // }
    // processor.checkOpen();
    // return operator.apply(methodHandle());
    // }

    /**
     * Checks that the underlying method is not static. Throwing an {@link InvalidDeclarationException} if the field is
     * static.
     * 
     * @return this hook
     * @throws InvalidDeclarationException
     *             if the underlying method is static
     * 
     * @see Modifier#isStatic(int)
     */
    public AnnotatedMethodHook<A> checkNotStatic() {
        if (Modifier.isStatic(descriptor.getModifiers())) {
            processor.tf().fail(failedModifierCheck(true));
        }
        return this;
    }

    /**
     * Checks that the underlying method is static. Throwing an {@link InvalidDeclarationException} if the field is not
     * static.
     * 
     * @return this hook
     * @throws InvalidDeclarationException
     *             if the underlying method is not static
     * 
     * @see Modifier#isStatic(int)
     */
    public AnnotatedMethodHook<A> checkStatic() {
        if (!Modifier.isStatic(descriptor.getModifiers())) {
            processor.tf().fail(failedModifierCheck(false));
        }
        return this;
    }

    void fail(String msg) {
        // Skal goere det let skrive fejlmeddellser
        throw new UnsupportedOperationException();
    }

    private String failedModifierCheck(boolean isNot) {
        String msg = (isNot ? "not be " : "be ") + "static";
        return "Fields annotated with @" + annotation.annotationType().getSimpleName() + " must " + msg + ", field = " + StringFormatter.format(descriptor);
    }

    /**
     * Returns a descriptor for the underlying method.
     * 
     * @return a descriptor for the underlying method
     */
    public MethodDescriptor method() {
        MethodDescriptor d = descriptor;
        if (d == null) {
            descriptor = d = MethodDescriptor.from(method);
        }
        return d;
    }

    /**
     * Returns a {@link MethodHandle} for the underlying method.
     * <p>
     * The returned method handle is never bound to a receiver, even if the underlying method is an instance method.
     * 
     * @return a MethodHandle to the underlying method
     * @throws InaccessibleMemberException
     *             if access checking fails
     * @see Lookup#unreflect(java.lang.reflect.Method)
     */
    public MethodHandle methodHandle() {
        return processor.unreflect(method);
    }

    // public <T extends InvocationTemplate> DelayedTempalte<T> newInvoker(Class<T> templateType) {
    // throw new UnsupportedOperationException();
    // }
    //
    // public <T extends InvocationTemplate> TemplateInvoker<T> newRawInvoker(Class<T> templateType) {
    // throw new UnsupportedOperationException();
    // }
}
