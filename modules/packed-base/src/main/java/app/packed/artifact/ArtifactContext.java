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

import app.packed.component.Component;
import app.packed.component.ComponentContext;
import app.packed.service.Injector;

/**
 * An artifact runtime context provides precise control over a single (top level) container. Instances of this interface
 * is normally neither exposed or used by end users. Instead it is wrapped in thin facade objects, such as {@link App}
 * or {@link Injector}. Which will delegate all call to this context.
 * <p>
 * An instance of this interface is normally acquired via {@link ArtifactDriver#instantiate(ArtifactContext)}.
 */
// Rename to ArtifactContext....
public interface ArtifactContext extends ComponentContext {

    /**
     * 
     */
    default void execute() {
        //
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

    @Override
    Injector injector();

    default Class<?> resultType() {
        // Ideen er her taenkt at vi kan bruge den samme med Job...
        //// En anden slags entry point annotering...
        return void.class;
    }

    <T> T use(Class<T> key);

    Component useComponent(CharSequence path);
}
