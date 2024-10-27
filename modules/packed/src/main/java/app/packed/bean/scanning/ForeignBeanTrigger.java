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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.BaseModuleNames;

/**
 *
 * Custom activators.
 *
 * Are activators that are not placed on the activating target annotation. But on a bean,
 * {@link app.packed.assembly.Assembly assembly} or {@link Extension extension} class.
 */

// Forerign Bean Trigger
// WE USE STRINGS for all class names. We might define a whole JakartaEESupport.
// But all the classes may not the on the classpath.
// For example, user doesn't include Hibernate on the classpath.
// But he can still use JakartaEESupport
/// This is also why we do not reference the types defined in BeanTrigger


// Can we define a foreign Context??? IDK

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

// I think eventually they should be moved to BeanTrigger
public @interface ForeignBeanTrigger {

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
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @Documented
    @Inherited
    @Repeatable(OnForeignAnnotatedField.All.class)
    @interface OnForeignAnnotatedField {

        String annotationClass();

        String extensionClass();

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.ANNOTATION_TYPE)
        @Inherited
        @Documented
        @interface All {
            OnForeignAnnotatedField[] value();
        }
    }

    // Forskellen paa den rigtige activator
    // Vi kan have flere af disse
    // Vi kan specify hvilken annotation der skal mappes

    // Alternativ smide annotationClass paa activatoren
    // Og saa have en stor meta annotation der tager AnnotatedBeanMethodActivator[] annotatedMethods();
//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.ANNOTATION_TYPE)
//    @Documented
//    @Inherited
//    @Repeatable(ForeignAnnotatedFieldActivator.All.class)
//    @interface ForeignAnnotatedFieldActivator {
//
//        String annotationClass();
//
//        // Nah, think just copy methods...
//        OnAnnotatedMethod activator();
//
//        @Retention(RetentionPolicy.RUNTIME)
//        @Target(ElementType.ANNOTATION_TYPE)
//        @Inherited
//        @Documented
//        @interface All {
//            ForeignAnnotatedFieldActivator[] value();
//        }
//    }

    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @BeanTrigger
    // Logger, Net, File
    // Meta annotation hooks annotations does not have to live on the extension


    @OnForeignAnnotatedField(annotationClass = "sdfsdf", extensionClass = BaseModuleNames.BASE_EXTENSION_CLASS)
    // Or maybe we have a logging extension. No I think default logging
    @OnForeignAnnotatedField(annotationClass = "java.lang.system.Logger", extensionClass = BaseModuleNames.BASE_EXTENSION_CLASS)
    @OnForeignAnnotatedField(annotationClass = "java.util.logging.Logger", extensionClass = BaseModuleNames.BASE_EXTENSION_CLASS)
//    @ForeignAnnotatedFieldHook2(annotationClass = "sdfsdf", field = @AnnotatedFieldBeanTrigger(ex))

    public @interface JavaBaseSupport {}
}
