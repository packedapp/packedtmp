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
package app.packed.bean.sandbox;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger;
import app.packed.bean.BeanTrigger.OnAnnotatedField;
import app.packed.binding.Key;
import app.packed.extension.BaseExtension;

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

        String beanIntrospectorClass();

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
     * @see app.packed.bean.scanning.BeanTrigger.OnAnnotatedField
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @Documented
    @Inherited
    @Repeatable(OnForeignAnnotatedField.All.class)
    @interface OnForeignAnnotatedField {

        String annotationClass();

        String beanIntrospectorClass();

        // we need allow get, allow set, ect
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.ANNOTATION_TYPE)
        @Inherited
        @Documented
        @interface All {
            OnForeignAnnotatedField[] value();
        }
    }

    /**
     *
     * @see app.packed.bean.scanning.BeanTrigger.OnAnnotatedField
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @Documented
    @Inherited
    @Repeatable(OnForeignAnnotatedFieldAlt.All.class)
    // Will force loading accordingly to ChatGPT
    @interface OnForeignAnnotatedFieldAlt {

        String annotationClass();

        OnAnnotatedField onAnnotation();

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.ANNOTATION_TYPE)
        @Inherited
        @Documented
        @interface All {
            OnForeignAnnotatedFieldAlt[] value();
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

    @OnForeignAnnotatedField(annotationClass = "sdfsdf", beanIntrospectorClass = "..sdfsdfBeanIntrospector")
    // Or maybe we have a logging extension. No I think default logging

    @OnForeignAnnotatedField(annotationClass = "java.lang.system.Logger", beanIntrospectorClass = "..LoggerBeanIntrospector")
    @OnForeignAnnotatedFieldAlt(annotationClass = "java.lang.system.Logger", onAnnotation = @OnAnnotatedField(introspector = LoggerBeanIntrospector.class))

    @OnForeignAnnotatedField(annotationClass = "java.util.logging.Logger", beanIntrospectorClass = "..LoggerBeanIntrospector")
//    @ForeignAnnotatedFieldHook2(annotationClass = "sdfsdf", field = @AnnotatedFieldBeanTrigger(ex))
    public @interface JavaBaseSupport {}

    static class LoggerBeanIntrospector extends BeanIntrospector<BaseExtension> {

        @Override
        public void onExtensionService(Key<?> key, OnContextService service) {
            assert key.equals(Key.of(java.lang.System.Logger.class));
            // inject
        }

    }
}
