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
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BeanClassActivator.AnnotatedBeanMethodActivator;
import app.packed.util.BaseModuleConstants;

/**
 *
 * Custom activators.
 *
 * Are activators that are not placed on the activating target annotation. But on a bean,
 * {@link app.packed.assembly.Assembly assembly} or {@link Extension extension} class.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
// override, force

// foreign, external

// Masake Paa BEP alligevel
// CustomHook

//// ServiceBindingHook...
// CustomBindingHook
// CustomBindingHook

// IDK maaske hoere den til i .bean alligevel

// Flyt den til BeanHook hvis vi kommer til at supportere dem

// Alternativt require at custom hooks skal defineres paa den extension hvor de skal bruges...
// Hmm...
public @interface BeanCustomActivator {

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @Repeatable(CustomBindingHook.All.class)
    @interface CustomBindingHook {

        String className();

        String extensionClass();

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.ANNOTATION_TYPE)
        @Inherited
        @Documented
        @interface All {
            CustomBindingHook[] value();
        }
    }

    /**
     *
     * @see AnnotatedFieldHook
     */
    // Like AnnotatedFieldHook, but also has an annotationClass
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @Documented
    @Inherited
    @Repeatable(ForeignAnnotatedFieldHook.All.class)
    @interface ForeignAnnotatedFieldHook {

        String annotationClass();

        String extensionClass();

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.ANNOTATION_TYPE)
        @Inherited
        @Documented
        @interface All {
            ForeignAnnotatedFieldHook[] value();
        }
    }

    // Forskellen paa den rigtige activator
    // Vi kan have flere af disse
    // Vi kan specify hvilken annotation der skal mappes

    // Alternativ smide annotationClass paa activatoren
    // Og saa have en stor meta annotation der tager AnnotatedBeanMethodActivator[] annotatedMethods();
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @Documented
    @Inherited
    @Repeatable(ForeignAnnotatedFieldActivator.All.class)
    @interface ForeignAnnotatedFieldActivator {

        String annotationClass();

        // Nah, think just copy methods...
        AnnotatedBeanMethodActivator activator();

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.ANNOTATION_TYPE)
        @Inherited
        @Documented
        @interface All {
            ForeignAnnotatedFieldActivator[] value();
        }
    }

    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @BeanClassActivator
    // Logger, Net, File
    // Meta annotation hooks annotations does not have to live on the extension
    @ForeignAnnotatedFieldHook(annotationClass = "sdfsdf", extensionClass = BaseModuleConstants.BASE_EXTENSION_CLASS)
    @ForeignAnnotatedFieldHook(annotationClass = "sdfsdf", extensionClass = BaseModuleConstants.BASE_EXTENSION_CLASS)
    public @interface JavaBaseSupport {}
}
