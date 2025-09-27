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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.context.Context;

/**
 * A bean trigger are simply properties of a bean that trigger some action.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface BeanTrigger {

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @BeanTrigger
    // Should we also have an Inherited version
    // I think so
    public @interface OnAnnotatedClass {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        /**
         * @return
         */
        boolean allowFullPrivilegeAccess() default false;

        /** The introspector responsible for this trigger. */
        Class<? extends BeanIntrospector<?>> introspector();

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
    public @interface OnAnnotatedField {

        /** Whether or not the owning extension is allow to get the contents of the field. */
        boolean allowGet() default false;

        /** Whether or not the owning extension is allow to set the contents of the field. */
        boolean allowSet() default false;

        /** The introspector responsible for this trigger. */
        Class<? extends BeanIntrospector<?>> introspector();

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
    public @interface OnAnnotatedMethod {

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

        /** The introspector responsible for this trigger. */
        Class<? extends BeanIntrospector<?>> introspector();

        Class<? extends Context<?>>[] requiresContext() default {};

        // Async, Transactional, Maybe metric?
        boolean requiresProxy() default false;
    }

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
    public @interface OnAnnotatedVariable {
//
//        // I think it would be super nice to indicate that we simply uses a key based local namespace
//        // Nothing fancy freeflowing key
//        // Maybe this is actually two different annotations???
//        // Should we have something about we use a KeyBasedScheme? Or a free
//        // For example Codegenerated...
//        // Hmm, men det betyder jo ogsaa vi laver peeling
//        boolean checkKeyRepresentation() default true;

        /** The introspector responsible for this trigger. */
        Class<? extends BeanIntrospector<?>> introspector();

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

    /**
     *
     * <p>
     * If the type being provider is a generic type. It will match it independent on any actual types. There is no support
     * for refining this. It must be handled in the extension.
     *
     * @see InheritableContextualServiceProvider
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @BeanTrigger
    // OnServiceVariable
    // OnContextServiceVariable
    // Was OnExtensionServiceBeanTrigger
    public @interface OnContextServiceVariable {

        /** The introspector responsible for this trigger. */
        Class<? extends BeanIntrospector<?>> introspector();

        /**
         * Any context that is needed for the service to be provided. The default is
         * {@link app.packed.service.advanced.ServiceResolver.NoContext} which indicates that the annotated class can be used
         * anywhere.
         * <p>
         * If no contexts are specified, the type can be used anywhere.
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

    /**
     * A version of the {@link ContextualServiceProvider} annotation that is {@link Inherited}. All other functionality is
     * identical.
     * <p>
     * NOTE: Remember, inherited annotations are not inherited on interfaces. So you an abstract class if you need to design
     * a inheritance hierarchy.
     *
     * @see ContextualServiceProvider
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @BeanTrigger
    @Inherited
    public @interface OnContextServiceInheritableVariable {

        /** The introspector responsible for this trigger. */
        Class<? extends BeanIntrospector<?>> introspector();

        /**
         * Contexts that are required in order to use the binding class.
         * <p>
         * If this binding is attempted to be used without the context being available an {@link UnavilableContextException}
         * will be thrown.
         * <p>
         * If this method returns multiple contexts they will <strong>all</strong> be required.
         *
         * @return required contexts
         */
        Class<? extends Context<?>>[] requiresContext() default {};
    }
}
