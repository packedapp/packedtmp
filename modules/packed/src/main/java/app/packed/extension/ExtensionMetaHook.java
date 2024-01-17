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
package app.packed.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.context.Context;

/**
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
// or Maybe just interface BeanHooks?
// TODO I think the name is bad. I would think it was hooks I could place on my bean class.
// ExtensionHooks?? Og saa AnnotatedFieldHook -> AnnotatedBeanFieldHook
//Problemet er AssemblyHook som jo kan implementeres af users
public @interface ExtensionMetaHook {

    /**
     * <p>
     * Attempting to place multiple annotated variable hook annotations on a single field or parameter will result in a
     * {@link BeanInstallationException} being thrown at build-time.
     *
     * @see BeanIntrospector#hookOnProvidedAnnotatedVariable(java.lang.annotation.Annotation,
     *      app.packed.bindings.BindableVariable)
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtensionMetaHook
    public @interface AnnotatedBeanVariableHook {

        /** The extension this hook is a part of. Must be located in the same module as the annotated element. */
        Class<? extends Extension<?>> extension();

        /**
         * Contexts that are required in order to use the binding class or annotation.
         * <p>
         * If this binding is attempted to be used without the context being available a {@link OutOfContextException} will be
         * thrown.
         * <p>
         * If this method returns multiple contexts they will <strong>all</strong> be required.
         *
         * @return stuff
         */
        Class<? extends Context<?>>[] requiresContext() default {};

        enum Mode {

            // Tror maaske vi skal overskrive en eller anden klasse
            // Som siger hvad vi skal goere i de enkelte tilfaelde

            DECORATE, DEFAULT, PEEK;

            // Kan kun vaere en Default annotering (Default og @Nullable?)

            // Alle decorates er altid koert foerned peek.
            //// fx vil vi altid have Peek efter decorate ved validering
            //// selvom decorate annotering er efter peek
        }
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtensionMetaHook
    public @interface AnnotatedBeanClassHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        /**
         * @return
         */
        boolean allowFullPrivilegeAccess() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();

        Class<? extends Context<?>>[] requiresContext() default {};
    }

    /**
     * In order to process fields that are annotated with the target annotation,
     * {@link BeanIntrospector#hookOnProvidedVariableType(Class, app.packed.bindings.BindableWrappedVariable)} must be
     * overridden.
     *
     * @see BeanIntrospector#hookOnAnnotatedField(OperationalField)
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtensionMetaHook
    // I don't think we are going to support meta annotations that have both type use and field use
    public @interface AnnotatedBeanFieldHook {

        /** Whether or not the owning extension is allow to get the contents of the field. */
        boolean allowGet() default false;

        /** Whether or not the owning extension is allow to set the contents of the field. */
        boolean allowSet() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();

        /** {@return any contexts that are required to use the target annotation.} */
        Class<? extends Context<?>>[] requiresContext() default {};
    }

    /**
     * An annotation that indicates that the target is a method hook annotation.
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtensionMetaHook
    public @interface AnnotatedBeanMethodHook {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as idk and... will fail with {@link UnsupportedOperationException} unless the value of this attribute is
         * {@code true}.
         *
         * @return whether or not the implementation is allowed to invoke the target method
         *
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        // invocationAccess
        boolean allowInvoke() default false; // allowIntercept...

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();

        Class<? extends Context<?>>[] requiresContext() default {};

        // Async, Transactional, Maybe metric?
        boolean requiresProxy() default false;

//        // IDK, don't we just want to ignore it most of the time???
//        // Nah maybe fail. People might think it does something
//        boolean requiresVoidReturn() default false;
    }

    /**
     *
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtensionMetaHook
    public @interface BindingTypeHook {

        /** The extension this hook is a part of. Must be located in the same module as the annotated type. */
        Class<? extends Extension<?>> extension();

        /**
         * Contexts that are required in order to use the binding class.
         * <p>
         * If this binding is attempted to be used without the context being available a
         * {@link app.packed.context.NotInContextException} will be thrown.
         * <p>
         *
         * If this method returns multiple contexts they will <strong>all</strong> be required.
         *
         * @return required contexts
         */
        Class<? extends Context<?>>[] requiresContext() default {};
    }
}
