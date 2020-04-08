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
import app.packed.component.SingletonContext;
import app.packed.lifecycle.StopOption;
import app.packed.service.ServiceExtension;

/**
 * An artifact context provides control over a single (top level) container. Instances of this interface are normally
 * never exposed to end users. Instead it is wrapped in thin facade objects, such as {@link App}. This facade object
 * than delegates all calls to an instance of this context.
 * <p>
 * An instance of this interface is normally acquired via {@link ArtifactDriver#newArtifact(ArtifactContext)}.
 * 
 * @apiNote In the future, if the Java language permits, {@link ArtifactContext} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
// ArtifactContext extends ContainerContext???
public interface ArtifactContext extends SingletonContext {

    // Optional<?>??? Maybe a ResultClass
    default Object result() {
        // awaitResult()...
        // awaitResultUninterruptable()...
        // Ideen er lidt at vi kan vente paa det...
        return null;
    }

    // TypeLiteral??? Maaske returnere execute() et object...
    default Class<?> resultType() {
        // Ideen er her taenkt at vi kan bruge den samme med Job...
        //// En anden slags entry point annotering...
        return void.class;
    }

    default void start() {}

    default <T> CompletableFuture<T> startAsync(T result) {
        throw new UnsupportedOperationException();
    }

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

    void stop(StopOption... options);

    <T> CompletableFuture<T> stopAsync(T result, StopOption... options);

    /**
     * Returns a service that is registered with the specified key, if it exists. Otherwise, fails by throwing
     * {@link UnsupportedOperationException}.
     * <p>
     * If the application is not already running.
     * 
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws UnsupportedOperationException
     *             if a service with the specified key does not exist. Or if the application does not use
     *             {@link ServiceExtension}.
     */
    // If the artifact has an execution phase this method will block while starting.
    // It can safely be invoked after a container has been shutdown...
    <T> T use(Key<T> key);

    Component useComponent(CharSequence path);
}
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

///// De her skal doe
///**
//* @throws UnsupportedOperationException
//*             if the artifact is not executable
//*/
//default void execute() {
//  // Spoergmaalet er hvor kommer den vaerdi fra????
//  // return null;
//}

// has start/run/stop
//default boolean hasExecutionPhase() {
//  return false;
//}