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

import java.lang.annotation.Annotation;

import app.packed.hooks.FieldHook;
import app.packed.hooks.MethodHook;
import app.packed.hooks.RealMethodSidecarBootstrap;
import app.packed.inject.Inject;

/**
 *
 */
// I sidste ende har vi bare brug for en @MetaAnnotation....

// Kan puttes paa Composer, Assembly, ComponentSource (Class) not function I think
@interface Support {

    Class<? extends Annotation>[] annotatedFieldsActivators() default {};

    FieldHook[] annotatedFields() default {};
}
// Alternativet er en Repeatable Annotation...

// Kraever extra activated by field
//@Support(annotatedFields = { @ExtensionField(activatedBy = Inject.class, allowGet = true, sidecar = FieldSidecar.class),
//        @ExtensionField(activatedBy = Inject.class, allowGet = true, sidecar = FieldSidecar.class) })
class XUsage {

}

// Kraever ikke noget.. men ser lidt underligt ud
@Support(annotatedFieldsActivators = { Inject.class, Inject.class }, annotatedFields = { @FieldHook(allowGet = true, bootstrap = FieldHook.Bootstrap.class),
        @FieldHook(allowGet = true, bootstrap = FieldHook.Bootstrap.class) })
@MethodHook(matchesAnnotation = Inject.class, bootstrap = RealMethodSidecarBootstrap.class)
class MyAssembly {

}
@interface MetaAnnotation {
    
}

@MethodHook(matchesAnnotation = Inject.class, bootstrap = RealMethodSidecarBootstrap.class)
@MethodHook(matchesAnnotation = Inject.class, bootstrap = RealMethodSidecarBootstrap.class)
@MetaAnnotation
@interface UsageAnotherAlternative {}