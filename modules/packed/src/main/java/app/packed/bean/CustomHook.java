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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanExtensionPoint.FieldHook;

/**
 *
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

//Kan man redirecte til extensions der ikke er i meta annoteringens module??? I don't really think so
// Vil mene Meta annotering skal vaere i samme module som extensionen...

public @interface CustomHook {

    /**
     *
     * @see FieldHook
     */
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @Repeatable(CustomHook.CustomFieldHook.All.class)
    @interface CustomFieldHook {

        String annotation();

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
        @Inherited
        @Documented
        @interface All {
            CustomFieldHook[] value();
        }
    }
    
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @Repeatable(CustomHook.CustomBindingHook.All.class)
    @interface CustomBindingHook {

        String className();

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
        @Inherited
        @Documented
        @interface All {
            CustomBindingHook[] value();
        }
    }
}
