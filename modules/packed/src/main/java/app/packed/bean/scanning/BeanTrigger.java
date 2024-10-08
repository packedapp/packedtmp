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
package app.packed.bean.scanning;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.context.Context;
import app.packed.extension.Extension;

/**
 * A bean trigger is
 */
//or Maybe just interface BeanHooks?
//TODO I think the name is bad. I would think it was hooks I could place on my bean class.
//ExtensionHooks?? Og saa AnnotatedFieldHook -> AnnotatedBeanFieldHook
//Problemet er AssemblyHook som jo kan implementeres af users

//ExtensionActivator in the .bean package

//Stuff on a bean that will activate an extension

//Something with Activator? ExtensionActivator

//Was BindingActivator

//BeanTriggers
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface BeanTrigger {

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
    @BeanTrigger
    public @interface AnnotatedVariableBeanTrigger {

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

        // I think it would be super nice to indicate that we simply uses a key based local namespace
        // Nothing fancy freeflowing key
        // Maybe this is actually two different annotations???
        // Should we have something about we use a KeyBasedScheme? Or a free
        // For example Codegenerated...
        boolean checkKeyRepresentation() default true;

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
    @BeanTrigger
    public @interface AnnotatedClassBeanTrigger {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        /**
         * @return
         */
        boolean allowFullPrivilegeAccess() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();

        Class<? extends Context<?>>[] requiresContext() default {};

        boolean requiresProxy() default false;
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
    @BeanTrigger
    // I don't think we are going to support meta annotations that have both type use and field use
    public @interface AnnotatedFieldBeanTrigger {

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
     * An annotation that indicates that the target annotation will trigger the associated {@link #extension()} whenever the
     * target annotation is used to annotate a method on a bean.
     * <p>
     * If the extension is not already in use by the bean's container. The extension will implicitly automatically be added
     * to the container.
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @BeanTrigger
    public @interface AnnotatedMethodBeanTrigger {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as idk and... will fail with {@link UnsupportedOperationException} unless the value of this attribute is
         * {@code true}.
         *
         * @return whether or not the implementation is allowed to invoke the target method
         *
         */
        // What are the use cases for not allowing invoke?
        // maybe just invokable = true, idk og saa Field.gettable and settable
        // invocationAccess
        boolean allowInvoke() default false; // allowIntercept...

        /** The extension that will be triggered. */
        Class<? extends Extension<?>> extension();

        Class<? extends Context<?>>[] requiresContext() default {};

        // Async, Transactional, Maybe metric?
        boolean requiresProxy() default false;

//        // IDK, don't we just want to ignore it most of the time???
//        // Nah maybe fail. People might think it does something
//        boolean requiresVoidReturn() default false;
    }
}
