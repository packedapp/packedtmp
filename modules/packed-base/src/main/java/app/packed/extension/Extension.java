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

import app.packed.bundle.BundleLink;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.ServiceNode;

/**
 *
 *
 *
 * <p>
 * Subclasses should be final
 *
 */
public abstract class Extension<T extends Extension<T>> {

    /** The configuration site of this object. The api needs to be public... */
    public final InternalConfigurationSite configurationSite() {
        throw new UnsupportedOperationException();
    }

    // createContract(); or
    // addToContract(ContractBuilder b)
    // Failure to have two features creating the same contract type...

    // Supports Freezable and ConfigurationSite
    protected final <S extends ServiceNode<S>> S addNode(S node) {
        throw new UnsupportedOperationException();
    }

    protected final void checkConfigurable() {

    }

    // Skal have en eller anden form for link med...
    // Hvor man kan gemme ting. f.eks. en Foo.class
    // Det er ogsaa her man kan specificere at et bundle har en dependency paa et andet bundle
    protected void onWireChild(@Nullable T child, BundleLink link) {}

    // onWireChikd
    protected void onWireParent(@Nullable T parent, BundleLink link) {}
}
// If it uses other extensions... Either get them by constructor, or via use(xxx)
/// F.eks. LifecycleExtension

// We should probably have some check that an extension is not used multiple places.../
// But then again maybe it is allowed to

// maybe a featureSet(FeatureInstaller fi) {
// } that can be overridden....

// public static final JAVAX_INJECT
// public static final DEFAULT (Hierachical error handling)....
// You can have services without inject, but not the other way around...
// featuresFreeze() <- used in a super class to prevent subclasses to add/change features...

// LifeFeature.withRequireDependenciesToBeFullyStarted

// Vi bliver noedt til at gemme noget live ogsaa, Hvis vi laver en host....
// Men man kan søge rekursivt efter det...

// Cache Information

// Provide Contract, Descriptor, .... (HookContract, HookDescriptor)....

// Direction of information, @OnHook creates a dependendy TO

// Allow filters..., incoming wiring, outgoing wiring

// Vi har noget cached information, per metoder, eller per field, eller per entity (class + mixins)

// Provide context to annotated method, for example ProvisionContext to @Provides

// Annotation + FieldDescriptor -> Provided
/// Taenker maaske at vi kan implementer f.eks. hooks, helt uden interne apis.
// Isaer hvis vi cacher information omkring annoteringer separate, i grupper
// Cacher alle metoder der har en RequiresFeature... central. Og kan saa lave grupper udfra dem

// Ide
// Vi cacher alle metoder, og felter, som har en MetaAnnotering via RequiresFeature.
// Vi kan saa requeste dem for hver service...Og gemme i et eller andet form for map.
// Som API stiller til raadighed, Vi bliver noedt til at cache paa MethodLookup.
// Eller hvis aaben paa noget andet..
// Cache<ProvidesGroup>
// cache.get(FooComponent.class);