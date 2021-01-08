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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.base.Nullable;
import packed.internal.bundle.extension.AbstractHookBootstrapModel;
import packed.internal.bundle.extension.VariableSidecarModel.VariableSidecarConfiguration;

/**
 *
 */
// subtype of
// annotated with (non-qualifier)
public abstract class VariableSidecar {

    /** A sidecar configurations object. Updated by {@link AbstractHookBootstrapModel.Builder}. */
    @Nullable
    private VariableSidecarConfiguration configuration;

    private VariableSidecarConfiguration configuration() {
        return configuration;
    }

    protected void configure() {}

    protected final void debug() {
        configuration().debug();
    }

    protected final void requireAssignableTo(Class<?> type) {

    }
}

@DynamicVariable
@interface Invokable {}

// Kan ogsaa tage MethodHandle, for AnnotatedMethodSidecar?
// In which case it is bound

/**
 * Indicates that an annotation can be used to dynamically provide a variable.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// Altsaa taenker vi laver det her som en sidecar alligevel...
@interface DynamicVariable {}

// Dynamic variables are dependencies that cannot statically be expressed as a key..
// F.eks. if you want to inject a system property @SystemProperty("doobar") has infinite many possibilities
// DynamicVariable to the rescue

// Can either be used with a sidecar 
// Or without a sidecar in which the context in which it is used must provide it.

//@SomePAnnotation cannot be used in this context.
// Throw new ProvisionException
//Look at the annotation to see which contexts it can be used in.

/// Optional...
//@DynamicVariable(supportOptional = true)

//// Old Names
//Dom, DynVar, Prime, @ProvideDynamically...
//Produce instead of provide...
//ProvideSingle
//ProvidePrototype
//ProvideViaPrime() <--- is protoype
//-- Explicitly defined via a sidecar...
//-- Overriden in the some internals
//WildcardVariable <- Nah det har jo ikke noget med Generics at goere....

// >=2 Dynamic variable anntoations fail
// = 1 Dynamic variable ok
// = 0 ordinary service

//@DynVar(positional = 0, -1)

//@DynVar(@HtttpParamGet)