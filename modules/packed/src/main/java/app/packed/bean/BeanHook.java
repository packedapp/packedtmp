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

import app.packed.application.ApplicationHook.ApplicationIs;
import app.packed.application.BuildGoal;
import app.packed.component.ComponentMetaHook;
import app.packed.component.ContainerHook.ContainerIs;
import app.packed.container.AssemblyHook.AssemblyIs;

/**
 *
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(BeanHook.All.class)
@ComponentMetaHook
// BeanClassHook?????

// Ellers er det && between all the iffs
// Alternative have an boolean useAndBetweenIfs default true
public @interface BeanHook {

    ApplicationIs[] ifApplication() default {};

    AssemblyIs[] ifAssembly() default {};

    ContainerIs[] ifContainer() default {};

    BeanIs[] ifBean() default {};

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

    @interface BeanIs {

        Class<? extends Annotation>[] ifBeanClassAnnotatedWith() default {};

        Class<?>[] ifBeanClassAssignableTo() default {};

        String[] ifBeanTaggedWith() default {};

        String[] taggedWithAnyOf() default {};

        // The assembly is always the application assembly
        // I don't know about this here...
        // Ideen var at man kunne sige noget om at applikations assemblyen var annoteret med X
        AssemblyIs[] applicationAssemblyIs() default {};

        // Do we have a Seperate buildIs???? Maybe I think so
        BuildGoal[] buildGoalsAnyOf() default { BuildGoal.IMAGE, BuildGoal.LAUNCH, BuildGoal.MIRROR, BuildGoal.VERIFY };
    }

    /** An annotation that allows for placing multiple {@link AssemblyHook} annotations on a single assembly. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of assembly hook declarations. */
        BeanHook[] value();
    }
}
