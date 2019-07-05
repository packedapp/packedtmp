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

import java.util.Collection;
import java.util.Optional;

/**
 *
 */
// Ideen med artifact controlleren er at en extension
// kan lave en per en extensio artifact controller per artifact.
// Eller dvs, runtimemen laver en...

// Har den en sidecar???
// En extension can installere noget per container. En artifactController kan installere per artifact...

// Vi bliver naesten noedt til monitorere Extension Dependencies for all containere...
//// Fordi hvis vi har sidecar per artifact saa vil nok gerne koere nogle finalizers
// i en raekkefoelge....
/// Dvs. Alle extensions har en

// TODO tror vi kan droppe extens Extension nu...
// ExtensionTree

// Ligesom Extension bliver der ogsaa noedt til at vaere en opdeling her... i build time, instantiation time, og runtime
public abstract class ExtensionArtifactController<E extends Extension, S extends ArtifactSidecar> {

    /**
     * Returns the first sidecar (if any) from the immediately parent artifact
     * 
     * @return the sidecar
     */
    // Eneste er at, maaske vil en evt host gerne have noget af sige..
    protected Optional<S> parent() {
        // Finds the first sidecar from a parent..
        throw new UnsupportedOperationException();
    }

    protected void onStart() {
        // If we have parent() method.. this means we need to initialize some field.
        // Because it wants to call stuff....
    }

    protected Collection<E> roots() {
        // Den er lidt for hvis vi har forskellige roots...
        /// F.eks. ind der bruger port(40) og en der bruger port 80
        // all the root containers the extension is used in
        throw new UnsupportedOperationException();
    }

    protected void onExtensionAdded(E extension) {

    }

    // Okay to extensions der saetter hver sin port....HMMMM. Saa skal vi vel have to forskellige sidecards????
    // Saa man kan indsaette sidecards for

    protected void onExtensionConfigured(E extension) {
        // We never expose the controller to the actual artifact.
    }

    protected void onFinished() {

    }

    protected S configure(ExtensionGraph<E> g) {
        return null;
    }

    // Hvad er lifecyclen af saadan en faetter????
    /// Taenker vi laver en foerste gang vi instantiere en extension af en specific type?????
    // Vi finder jo ud af det allerede i constructoren....

    // Den ting der er med det, er at de kan bliver tilfoejet in order order
    // use(XExtention) then link(SomeBundle) vs
    // link(SomeBundle) the use(XExtention)

}
/// **
// * If the underlying container has parent which uses this container, returns the parents
// *
// * @return if the underlying any parent of this container
// * @throws IllegalStateException
// * if called from outside of {@link #onConfigured()}.
// */
// protected final Optional<T> parent() {
// throw new UnsupportedOperationException();
// }

// Skal have en eller anden form for link med...
// Hvor man kan gemme ting. f.eks. en Foo.class
// Det er ogsaa her man kan specificere at et bundle har en dependency paa et andet bundle
// protected void onWireChild(@Nullable T child, BundleLink link) {}
//
//// onWireChikd
// protected void onWireParent(@Nullable T parent, BundleLink link) {}

/// **
// * Creates a new configuration site
// *
// * <p>
// * If the gathering of a stack-based configuration is disabled. This method return {@link ConfigSite#UNKNOWN}.
// *
// * @param name
// * the name of the operation
// * @return the new configuration site
// */
// private final ConfigSite spawnConfigSite(String name) {
//// Have a depth indicator.....
//// + ConfigSiteStackFilter.create("com.acme")
//// ConfigSite spawnConfigSite(ConfigSiteStackFilter f, String name) {} Den ved alt om config sites er disablet paa
//// containeren
// throw new UnsupportedOperationException();
// }

// createContract(); or
// addToContract(ContractBuilder b)
// Failure to have two features creating the same contract type...

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
/// **
// * If this extensions container is deployed into a host, returns the host.
// *
// * @return attachments
// */
// protected final Optional<AttachmentMap> host() {
// // Ideen er at hvis man har en host som foraeldre saa....
// // Skal den vaere tilgaengelig fra ContainerConfiguration??
// // Skal vi have et egentligt interface??? Kunne jo ogsaa vaere rart med en path??
// // Maaske at kunne se kontrakterne... o.s.v.
// throw new UnsupportedOperationException();
// // host().
// }
