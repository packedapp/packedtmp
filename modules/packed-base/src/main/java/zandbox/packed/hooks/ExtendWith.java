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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// Tror bedre jeg kan lide den end bare at smide repeatable Many paa hver hook annotering.
/**
 *
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RUNTIME)
public @interface ExtendWith {
    AccessibleFieldHook[] accessibleFields() default {};

    InjectableParameterHook[] injectableParameters() default {};
}

//Den her klasse blev droppet da vi fandt ud af at tilfoeje
//@MethodHook(annotations = .....)
//Og disse extensions nu blot er en meta annotation...
// Meta Annotations should normally be declared Inheritable
// Maybe we allow extension less these annotations
//@ExtendWith(annotation = Provide.class, field = @FieldHook(allowGet = true, bootstrap = FieldHook.Bootstrap.class), method = @MethodHook(allowInvoke = true, bootstrap = RealMethodSidecarBootstrap.class))
//@ExtendWith(annotation = Deprecated.class, method = @MethodHook(allowInvoke = true, bootstrap = RealMethodSidecarBootstrap.class))

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
