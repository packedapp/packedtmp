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
package app.packed.bean;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanHook3.BeanIs;

/**
 *
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(BeanHook3.All.class)
@BeanHook3(beanIs = @BeanIs(annotatedWith = Deprecated.class), value = BeanClassTransformer.class)
@interface BeanHook3 {

    BeanIs[] beanIs() default {};

    String ifContainerInPath() default "*"; // Regexp on container path
    // Would it be nice to be able to do it with beans as well??
    // For example, if I cannot modify the bean. So like a " full path
    // Nah, I think just specifying the class would be fine

    String[] ifContainerTaggedWith() default {};

    // ifExtensionPresent?

    boolean useAndBetweenIfs() default true;

    /**
     * {@return the transformer that should be applied to the container(s) defined by the assembly}
     * <p>
     * Implementations must be visible and instantiable to the framework. If using the module system this means that the
     * implementation should be accessible to the module of the framework and have a public constructor. Or the package in
     * which the implementation is located must be open to the framework.
     */
    Class<? extends BeanClassTransformer>[] value();

    /** An annotation that allows for placing multiple {@link AssemblyHook} annotations on a single assembly. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        @SuppressWarnings("exports")
        /** An array of assembly hook declarations. */
        BeanHook3[] value();
    }

    // And
    // @BeanHook3(beanIs = {@BeanIs(annotatedWith = Deprecated.class), @BeanIs(annotatedWith = X.class)})
    // Or (maybe name to annotatedWithAny
    // @BeanHook3(beanIs = {@BeanIs(annotatedWith = Deprecated.class, X.class)})

    @interface BeanIs {
        Class<? extends Annotation>[] annotatedWith() default {};

        Class<?>[] assignableTo() default {};

        String[] taggedWith() default {};
    }
}
