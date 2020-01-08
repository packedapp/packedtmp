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

import app.packed.component.Component;
import app.packed.component.SingletonContext;
import app.packed.lang.Key;
import app.packed.lifecycle.StopOption;
import app.packed.service.Injector;

/**
 * An artifact runtime context provides precise control over a single (top level) container. Instances of this interface
 * are normally never exposed to end users. Instead it is wrapped in thin facade objects, such as {@link App} or
 * {@link Injector}. Which delegates all calls to the context.
 * <p>
 * An instance of this interface is normally acquired via {@link ArtifactDriver#newArtifact(ArtifactContext)}.
 */

// Det er jo i virkeligheden ContainerText, men den kan bare ikke lukkes ned....
public interface ArtifactContext extends SingletonContext {

    /**
     * 
     */
    default void run() {}

    @Override
    Injector injector();

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

    <T> T use(Key<T> key);

    Component useComponent(CharSequence path);
}
