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
package zandbox.packed.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

import app.packed.base.Nullable;
import app.packed.extension.Extension;
import app.packed.inject.variable.BeanDependency;
import zandbox.internal.hooks2.bootstrap.AccessibleFieldBootstrapModel;
import zandbox.internal.hooks2.bootstrap.AccessibleFieldBootstrapModel.BootstrapContext;
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor;

/**
 * A hook that allows to read and write the contents of a single field. The hook is triggered by an annotation.
 * <p>
 * Field hooks are mainly used if you need to repeatable read or write a field. If you are simply looking to inject a
 * value into a field, {@link BeanDependency} is usually a better option.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface AccessibleFieldHook {

    /** The {@link Bootstrap} class for this hook. */
    Class<? extends AccessibleFieldHook.Bootstrap> bootstrapBean() default Bootstrap.class;

    /** The extension this hook is a part of. */
    Class<? extends Extension> extension();

    /** The annotation that triggers the hook. */
    Class<? extends Annotation> onAnnotation();

    /**
     * A bootstrap class that determines how the hook is processed.
     * <p>
     * If a field is annotated is such a way that there are multiple hooks activated at the same and athere are multiple
     * hooks that each have A single bootstrap Hvad goer vi med abstract klasser her??? Det er maaske ikke kun performance
     * at vi skal cache dem. Ellers kan vi ligesom ikke holder kontrakten om kun at aktivere det en gang...
     */
    abstract class Bootstrap {

        /**
         * A bootstrap object using by this class. Should only be read via {@link #context()}. Updated via
         * {@link AccessibleFieldBootstrapModel}.
         */
        private @Nullable BootstrapContext context;

        protected void bootstrap() {}

        /** {@return the bootstrap context object} */
        private BootstrapContext context() {
            // Maybe do like Assembly with a doBootstrap method
            BootstrapContext b = context;
            if (b == null) {
                throw new IllegalStateException("This method cannot called outside of the #bootstrap() method. Maybe you tried to call #bootstrap() directly");
            }
            return b;
        }

        /** Disables the hook, no further processing will be done. */
        public final void disable() {
            context().disable();
        }

        /** {@return the field for which this bootstrap instance has been created.} */
        public final Field field() {
            return processor().expose(getClass().getModule());
        }

        /**
         * Returns an annotated element from the method that is being bootstrapped.
         * 
         * @see AnnotatedElement#getAnnotation(Class)
         */
        public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return processor().getAnnotation(annotationClass);
        }

        public final Annotation[] getAnnotations() {
            return processor().getAnnotations();
        }

        public final <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
            return processor().getAnnotationsByType(annotationClass);
        }

        /**
         * Returns true if an annotation for the specified type is <em>present</em> on the hooked field, else false.
         * 
         * @param annotationClass
         *            the Class object corresponding to the annotation type
         * @return true if an annotation for the specified annotation type is present on the hooked field, else false
         * 
         * @see Field#isAnnotationPresent(Class)
         */
        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return processor().isAnnotationPresent(annotationClass);
        }

        /**
         * Returns a method handle that gives read access to the underlying field as specified by
         * {@link Lookup#unreflectGetter(Field)}.
         * 
         * @return a method handle getter
         */
        public final MethodHandle methodHandleGetter() {
            return processor().unreflectGetter();
        }

        /**
         * Returns a method handle that gives write access to the underlying field as specified by
         * {@link Lookup#unreflectSetter(Field)}.
         * 
         * @return a method handle setter
         */
        public final MethodHandle methodHandleSetter() {
            return processor().unreflectSetter();
        }

        private ClassBootstrapProcessor.FieldProcessor processor() {
            return context().processor;
        }

        /**
         * Returns a var handle that gives write access to the underlying field as specified by
         * {@link Lookup#unreflectVarHandle(Field)}.
         * 
         * @return the var handle
         */
        public final VarHandle varHandle() {
            return processor().unreflectVarhandle();
        }
    }

    /* sealed */ interface BuildContext {

    }
}
// I 99% af alle tilfaelde er det en annoteret klasse...
// Som extensionen ikke har adgang til hvis man bruger module systemet...
// Skal jo naesten goer vold paa module systemet for at 2 module kan laese hinanden...
// Saa det burde ikke vaere noget problem at injecte en instance...
// Det skulle da lige vaere hvis man man implementere et java interface...

// https://www.baeldung.com/spring-rest-openapi-documentation