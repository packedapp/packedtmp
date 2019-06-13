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
package xain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.container.Extension;
import app.packed.inject.InjectorExtension;
import app.packed.inject.ProvideHelper;

/**
 *
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
// OnBundle .....
// OnComponent
// OnMixin
// UsesExtension

// Vi har en @RequiresInjection annotation....
// Den er taenkt til at indikere at man kan injecte parameterene i @OnStart metoder...
// Saa dependencies fra dem kan blive taget med i BundleContracten....
// Uses(InjectorExtension.class)

/// Paaa container annoteringer... Skal have nogle matchene metoder????

// handleAnnotatedMethod(

public @interface UsesExtension {
    Class<? extends Extension<?>>[] value();
}

@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface MethodInjectable {

    String[] descriptions() default {};

    Class<?>[] value();
}

@MethodInjectable(ProvideHelper.class)
@UsesExtension(InjectorExtension.class)
@interface NewProvides {}
// In one @XMethod(extensions = InjectorExtension.class, injectSupported = true, injectHelperClasses =
// ProvidesHelper.class)
//// Alternativt skal

// Alternativ kan annotere helper classes???
// @AvailableFor(Provides.class)
// public ProvidesHelper
// Downside, any one can annotere deres klasses....
// Upside, ProvidesHelper can only be injected into methods annotated with @Provides

/// Aabner ogsaa op for at man Annotere tilfaeldig objecter med en annoterering.
///// Hvor man har en raekke dependencies, for at lave et nyt pbject.
///// En metoder faar saa automatisk dependencies, til at de dependencies der skal bruges til at lave objectet...
///// fe.sk

// @Special Class
// class Calculater(Math m, Stuff s)
// hvis man refererer til Calcul
/// Det bliver noedt til at vaere en fejl hvis man direkte proever at registrere den
