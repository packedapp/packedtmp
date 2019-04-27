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
package app.packed.basespi;

/**
 *
 */

// maybe a featureSet(FeatureInstaller fi) {
// } that can be overridden....

// InjectFeatyre
// public static final JAVAX_INJECT
// public static final DEFAULT (Hierachical error handling)....
// You can have services without inject, but not the other way around...
// featuresFreeze() <- used in a super class to prevent subclasses to add/change features...

// LifeFeature.withRequireDependenciesToBeFullyStarted

// Maybe put everything into an immutable feature set
public abstract class Feature<T extends FeatureConfigurator> {

    // Alternativ have en configure metoder

    protected T newConfigurator() {
        throw new UnsupportedOperationException();
    }
    // Cache Information

    // Provide Contract, Descriptor, .... (HookContract, HookDescriptor)....

    // Direction of information, @OnHook creates a dependendy TO

    // Allow filters..., incoming wiring, outgoing wiring

    // Vi har noget cached information, per metoder, eller per field, eller per entity (class + mixins)

    // Provide context to annotated method, for example ProvisionContext to @Provides

    // Annotation + FieldDescriptor -> Provided
}
// HookFeature (Factory for a Configurator, specified by user)
// HookFeatureConfigurator (A configurator that can be used at runtime
// HookFeatureDescriptor (Implementation details about features)
// HookFeatureContract - The public contract of the bundle

// Hvordan foer vi saa man ikke tager fejl af ServiceContract og tror det gaelder en service.
// ServicesContract

// For descriptors, vi kunne tage en method handle. som vi kan sende videre til noget bruger check kode.
// Vi wrapper den selvf saa brugeren ikke kan faa en brugbar reference.

@interface FeatureAnnotation {
    // detailed information
}
// Hooks annotations. Is meta