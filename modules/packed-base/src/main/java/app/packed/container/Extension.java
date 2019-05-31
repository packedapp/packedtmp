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

import java.util.Optional;
import java.util.function.Consumer;

import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.util.AttachmentMap;
import packed.internal.container.AppPackedContainerSupport;
import packed.internal.container.DefaultContainerConfiguration;

/**
 * Container extensions are used to extend containers with functionality.
 *
 *
 * <p>
 * Subclasses of this class that are actively used should be final.
 */
// Maybe rename to ContainerExtension because we rarely need to spell it out
// Disallow registering extensions as a service???
public abstract class Extension<T extends Extension<T>> {

    static {
        AppPackedContainerSupport.Helper.init(new AppPackedContainerSupport.Helper() {

            /** {@inheritDoc} */
            @Override
            public void setExtensionConfiguration(Extension<?> extension, DefaultContainerConfiguration configuration) {
                extension.configuration = requireNonNull(configuration);
            }
        });
    }

    /** The configuration of the container in which the extension is registered. */
    @SuppressWarnings("exports")
    public DefaultContainerConfiguration configuration;

    /**
     * Checks that the container that this extension belongs to is still configurable. Throwing an
     * {@link IllegalStateException} if it is not.
     * 
     * @throws IllegalStateException
     *             if the container this extension is no longer configurable.
     */
    protected final ContainerConfiguration checkConfigurable() {
        DefaultContainerConfiguration c = configuration;
        c.checkConfigurable();
        return c;
    }

    /**
     * Returns the configuration of the container or fails with an {@link IllegalStateException}.
     * 
     * @return the configuration of the container
     */
    private DefaultContainerConfiguration configuration() {
        DefaultContainerConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This operation cannot be called from the contructor of the extension");
        }
        return c;
    }

    /**
     * Performs the given action for each of the wirelets of the specified type. Actions are performed in the registration
     * order of the wirelets. Exceptions thrown by the action are relayed to the caller.
     *
     * @param <W>
     *            the type of wirelets to process
     * @param wireletType
     *            the type of wirelets to process
     * @param action
     *            The action to be performed for each wirelet
     */
    protected final <W> void forEachWirelet(Class<W> wireletType, Consumer<? super W> action) {
        configuration().forEachWirelet(wireletType, action);
    }

    /**
     * If this extensions container is deployed into a host, returns the host.
     * 
     * @return attachments
     */
    protected final Optional<AttachmentMap> host() {
        // Ideen er at hvis man har en host som foraeldre saa....
        // Skal den vaere tilgaengelig fra ContainerConfiguration??
        // Skal vi have et egentligt interface??? Kunne jo ogsaa vaere rart med en path??
        // Maaske at kunne se kontrakterne... o.s.v.
        throw new UnsupportedOperationException();
        // host().
    }

    /**
     * 
     */
    public void onFinish() {}

    public void buildBundle(BundleDescriptor.Builder builder) {}

    protected void onFirstUse() {}

    //
    // protected final <N extends AbstractFreezableNode> N mergeOperations(Supplier<N> supplier) {
    // // Ideen er at man kalde
    // // bundleWith
    // // configuration.
    //
    // // Vi ved vi er single traadet. Saa det er vel noget med at have en counter... der tikker en op hver gang vi kalder
    // // mergeOperation
    // return mergeOperations(() -> {
    // configuration.install("foo");
    // configuration.install("foo");
    // return null;
    // });
    //
    // // Don't think we need to have a separate verify step
    // //
    // // configuration.install("foo").setName("foo)";
    // // configuration.install("foo").setName("foo)";
    // // Would both be verified ok, because we do not make structural changes in the first step when verifying
    //
    // // What we would need was a command like functionality. Where to much trouble
    //
    // }

    // protected final void newLine() {
    // checksConfigurable
    // FreezesAnyNode before

    // Checks that the bundle/configurator/... is still active
    // Freezes any previous node for modifications....
    // Which means that everything is nodes....

    // Because bind(x) followed by install(x) should work identical to
    // Because install(x) followed by bind(x) should work identical to
    // }

    // createContract(); or
    // addToContract(ContractBuilder b)
    // Failure to have two features creating the same contract type...

    /**
     * If the underlying container has parent which uses this container, returns the parents
     * 
     * @return if the underlying any parent of this container
     * @throws IllegalStateException
     *             if called from outside of {@link #onFinish()}.
     */
    protected final Optional<T> parent() {
        throw new UnsupportedOperationException();
    }

    // Skal have en eller anden form for link med...
    // Hvor man kan gemme ting. f.eks. en Foo.class
    // Det er ogsaa her man kan specificere at et bundle har en dependency paa et andet bundle
    // protected void onWireChild(@Nullable T child, BundleLink link) {}
    //
    // // onWireChikd
    // protected void onWireParent(@Nullable T parent, BundleLink link) {}

    /**
     * Returns the path of the underlying container.
     * 
     * @return the path of the underlying container
     */
    protected final ComponentPath path() {
        throw new UnsupportedOperationException();
    }

    protected void build() {
        // Maybe take an attributemap that is shared between all invocations
        // default implementation processes children..
        // So we should always call super.build();
        // sadsad()
        // super.build();
        // weweew
    }

    /**
     * Creates a new configuration site
     * 
     * <p>
     * If the gathering of a stack-based configuration is disabled. This method return {@link ConfigSite#UNKNOWN}.
     * 
     * @param name
     *            the name of the operation
     * @return the new configuration site
     */
    protected final ConfigSite spawnConfigSite(String name) {
        // Have a depth indicator.....
        // + ConfigSiteStackFilter.create("com.acme")
        // ConfigSite spawnConfigSite(ConfigSiteStackFilter f, String name) {} Den ved alt om config sites er disablet paa
        // containeren
        throw new UnsupportedOperationException();
    }
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

//// Supports Freezable and ConfigurationSite
// protected final <S extends ServiceNode<S>> S addNode(S node) {
// throw new UnsupportedOperationException();
// }

/// **
// * The configuration site of this object. The api needs to be public...
// *
// * @return this configuration site
// */
//// Det er hvor extensionen er blevet installeret...Tror vi skal vaere lidt mere complex foerend det giver mening
// public final InternalConfigurationSite configurationSite() {
// throw new UnsupportedOperationException();
// }
