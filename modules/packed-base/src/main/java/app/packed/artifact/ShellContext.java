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
package app.packed.artifact;

import java.util.concurrent.CompletableFuture;

import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.component.ComponentDelegate;
import app.packed.config.ConfigSite;
import app.packed.lifecycleold.StopOption;
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;

/**
 * An artifact context provides control over a single container. Instances of this interface are normally never exposed
 * to end users. Instead it is wrapped in thin facade objects, such as {@link App}. This facade object than delegates
 * all calls to an instance of this context.
 * <p>
 * An instance of this interface is normally acquired via stuff.
 * <p>
 * Unless otherwise specified, implementations of this interface are safe for use by multiple concurrent threads.
 * 
 * @apiNote In the future, if the Java language permits, {@link ShellContext} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
// ArtifactContext does not extends ContainerContext??? Fordi ContainerContext er privat
// til en container. Og en artifact er mere udefar
public interface ShellContext extends ComponentDelegate {

    /**
     * Returns the config site of this artifact.
     * 
     * @return the config site of this artifact
     */
    default ConfigSite configSite() {
        return component().configSite();
    }

    /**
     * Returns the component representation of this guest.
     * 
     * @return the component representation of this guest
     */
    @Override
    Component component();

    /**
     * Returns an injector with any services that the underlying container has exported. If the underlying does not export
     * any services or not does not use the {@link ServiceExtension} at all. The injector will be empty.
     * 
     * @return an injector for the underlying container
     */
    Injector injector();

    // start() osv smider UnsupportedOperationException hvis LifeycleExtension ikke er installeret???
    // Naeh syntes bare man returnere oejeblikligt

    // Noget med lifecycle
    // Noget med Injector // Hvis vi har sidecars.... Er det maaske bare der...
    // Container tillader sidecar... App goer ikke. Saa kan man hvad man vil...

    // Entry point koere bare automatisk efter start....
    // Men ville vaere rart at vide

    // Distenction mellem business service or infrastructure service....
    // Hmm, problemet er vel at vi gerne vil injecte begge....

    // Noget med entrypoint?? Nej tror ikke vi har behov for at expose dett..

    // TypeLiteral??? Maaske returnere execute() et object...

    // Optional<?>??? Maybe a ResultClass
    default Object result() {
        // awaitResult()...
        // awaitResultUninterruptable()...
        // Ideen er lidt at vi kan vente paa det...
        return null;
    }

    // En Attribute????
    default Class<?> resultType() {
        // Ideen er her taenkt at vi kan bruge den samme med Job...
        //// En anden slags entry point annotering...
        return void.class;
    }

    default void start() {}

    default <T> CompletableFuture<T> startAsync(T result) {
        throw new UnsupportedOperationException();
    }

    void stop(StopOption... options);

    <T> CompletableFuture<T> stopAsync(T result, StopOption... options);

    default <T> T use(Class<T> key) {
        return injector().use(key);
    }

    /**
     * Returns a service registered with the specified key, if it exists. Otherwise, fails by throwing
     * {@link UnsupportedOperationException}.
     * <p>
     * If the underlying container has not already been started. Invoking this method will first invoke {@link #start()} and
     * wait.
     * <p>
     * If the underlying container failed to start, every invocation of this method will fail with
     * {@link IllegalStateException} or UnavailableContainerExtension?
     * 
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws UnsupportedOperationException
     *             if a service with the specified key does not exist. Or if the application does not use
     *             {@link ServiceExtension}.
     * @see #injector()
     */
    // If the artifact has an execution phase this method will block while starting.
    // It can safely be invoked after a container has been shutdown...

    // Det her er ting der er tilgaengelig til componenten med en ren noegle...
    // Det ser ud som om det er containeren der forspoerger??? Eller udefra???
    /// Maaske 2 metoder...
    default <T> T use(Key<T> key) {
        return injector().use(key);
    }
}

//
///**
// * Returns a set of all the extension that are available to the top component. If the top component is a container it is
// * all the extensions that are registered with container when it is assembled. If it is not a container. The set
// * contains all extensions that are registered with the container to which the top container belongs to.
// * 
// * @return the extensions that are used
// */
//// Maaske en attribute istedet for..
//// Altsaa extensions har jo noget med en container at goere og ikke en guest...
//// Du maa lede recursivt op...
//default ExtensionSet extensions() {
//    return ExtensionSet.empty();
//}

//// Det er jo i virkeligheden ContainerContext uden lifecycle... + lidt ekstra
//
//// Er det tilgaengelig paa runtime?????? Nej det syntes jeg ikke...
//// Maaske fra en BundleDescriptor.... Men ikke paa runtime...
//// Taenker ikke i foerste omgang...
//default Set<Class<? extends Extension>> extensionTypes() {
//  throw new UnsupportedOperationException();
//}
//
//default boolean isExtensionPresent(Class<? extends Extension> extensionType) {
//  // Hmmmm.. if (isExtensionPresent(ServiceExtension.class) ( use(Stuff.class))
//  return false;
//}

// has start/run/stop
//default boolean hasExecutionPhase() {
//  return false;
//}