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

import app.packed.app.App;
import app.packed.component.Component;
import app.packed.component.ComponentContext;
import app.packed.inject.Injector;

/**
 * An artifact runtime context provides.
 * <p>
 * Artifact context are usually not exposed to end users. But is instead wrapped in a thin facade object, such as
 * {@link App} or {@link Injector}. Which delegates any call to the artifact context.
 * <p>
 * An instance of this interface is normally acquired from a {@link ArtifactDriver#instantiate(ArtifactRuntimeContext)}.
 */
// Hvis vi skal have noget some helst kontrol...
// Boer vi ikke extende component, da vi risikiere
// At returnere en instand i ComponentStream.
public interface ArtifactRuntimeContext extends ComponentContext {

    /**
     * 
     */
    default void execute() {
        //
    }
    // Noget med lifecycle
    // Noget med Injector // Hvis vi har sidecars.... Er det maaske bare der...
    // Container tillader sidecar... App goer ikke. Saa kan man hvad man vil...

    // Entry point koere bare automatisk efter start....
    // Men ville vaere rart at vide

    // Distenction mellem business service or infrastructure service....
    // Hmm, problemet er vel at vi gerne vil injecte begge....

    // Noget med entrypoint?? Nej tror ikke vi har behov for at expose dett..

    default Class<?> resultType() {
        // Ideen er her taenkt at vi kan bruge den samme med Job...
        //// En anden slags entry point annotering...
        return void.class;
    }

    <T> T use(Class<T> key);

    @Override
    Injector injector();

    Component useComponent(CharSequence path);
}
