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
package app.packed.hooks.sandbox;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.packed.container.Extension;
import app.packed.container.ExtensionNest;
import app.packed.hooks.ClassHook;
import app.packed.hooks.FieldHook;
import app.packed.hooks.MethodHook;
import app.packed.hooks.RealMethodSidecarBootstrap;
import app.packed.inject.Provide;

/**
 *
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RUNTIME)
@Repeatable(ExtendWith.Many.class)
public @interface ExtendWith {

    Class<? extends Annotation> annotation() default Annotation.class;

    ClassHook[] klass() default {}; // default @ExtensionClass(bootstrap = ExtensionClass.Bootstrap.class);

    FieldHook[] field() default {}; // @ExtensionField(bootstrap = ExtensionField.Bootstrap.class);

    MethodHook[] method() default {};// @ExtensionMethod(bootstrap = RealMethodSidecarBootstrap.class);

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RUNTIME)
    @interface Many {
        ExtendWith[] value();
    }
}

// Meta Annotations should normally be declared Inheritable
@ExtensionNest(Extension.class) // <--- what if one bundle...
// Maybe we allow extension less these annotations
@ExtendWith(annotation = Provide.class, field = @FieldHook(allowGet = true, bootstrap = FieldHook.Bootstrap.class), method = @MethodHook(allowInvoke = true, bootstrap = RealMethodSidecarBootstrap.class))
@ExtendWith(annotation = Deprecated.class, method = @MethodHook(allowInvoke = true, bootstrap = RealMethodSidecarBootstrap.class))

// alternativ have en ExtensionMethod.target[] {}

// Meta data annotationer er jo tid kun taenkt til en enkelt extension...
// Saa fint at vi bare har en enkelt @ExtensionMemeber...
// Problemet er dog saa at jeg ikke rigtig ved hvordan den fungere som en metaannotation...
// Kan jo ikke bare f.eks. merge two forskellige annotatationer hvis den ene har @ExtensionMember(XExtension) og den anden har
// @ExtensionMember(YExtension)
// Maaske skal den bare bruge en anden makanism
class Usage {

    class MyX {

    }
}
