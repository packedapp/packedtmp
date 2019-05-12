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
package app.packed.container;

import static java.util.Objects.requireNonNull;

import app.packed.bundle.BundleLink;
import app.packed.config.ConfigSite;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.container.AppPackedContainerSupport;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.buildtime.DefaultContainerConfiguration;

/**
 *
 *
 *
 * <p>
 * Subclasses should be final
 *
 */
// Maybe rename to ContainerExtension
public abstract class Extension<T extends Extension<T>> {

    static {
        AppPackedContainerSupport.Helper.init(new AppPackedContainerSupport.Helper() {

            @Override
            public void setExtensionConfiguration(Extension<?> e, DefaultContainerConfiguration configuration) {
                e.configuration = requireNonNull(configuration);
            }
        });
    }

    protected DefaultContainerConfiguration configuration;

    protected ConfigSite spawnSite(String name) {
        // Den ved alt om config sites er disablet paa containeren
        throw new UnsupportedOperationException();
    }

    // Supports Freezable and ConfigurationSite
    protected final <S extends ServiceNode<S>> S addNode(S node) {
        throw new UnsupportedOperationException();
    }

    protected final void checkConfigurable() {

    }

    /**
     * The configuration site of this object. The api needs to be public...
     * 
     * @return this configuration site
     */
    // Det er hvor extensionen er blevet installeret...Tror vi skal vaere lidt mere complex foerend det giver mening
    public final InternalConfigurationSite configurationSite() {
        throw new UnsupportedOperationException();
    }

    // createContract(); or
    // addToContract(ContractBuilder b)
    // Failure to have two features creating the same contract type...

    protected final void newLine() {
        // checksConfigurable
        // FreezesAnyNode before

        // Checks that the bundle/configurator/... is still active
        // Freezes any previous node for modifications....
        // Which means that everything is nodes....

        // Because bind(x) followed by install(x) should work identical to
        // Because install(x) followed by bind(x) should work identical to
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