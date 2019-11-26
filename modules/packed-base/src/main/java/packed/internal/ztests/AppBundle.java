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
package packed.internal.ztests;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.container.BaseBundle;
import app.packed.container.Bundle;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.entrypoint.StringArgs;
import app.packed.service.ServiceWirelets;

/**
 * A specialized version of {@link BaseBundle} that provide various utility methods for instantiating {@link App apps}
 * from {@link Bundle bundles}. Besides the utility methods, extending this class also clearly signals that a particular
 * bundle is meant as a source for creating an {@link App}.
 * <p>
 * Is typically used like this.
 * 
 * 
 * <p>
 * Or in case
 */
// Det der taeller imod den, er hvis folk gerne vil provide deres egne firma bundle..
// Det fungere ikke skide godt med nedarvning
abstract class AppBundle extends BaseBundle {

    /**
     * Creates a new artifact image from the specified source. Is typically used like this: <pre>
     * 
     * </pre>
     * <p>
     * Invoking this method is identical to invoking {@link ArtifactImage#build(ContainerSource, Wirelet...)}.
     * 
     * @param source
     *            the could to create the image from
     * @param wirelets
     * @return a new artifact image from the specified source
     * @see ArtifactImage#build(ContainerSource, Wirelet...)
     */
    protected static ArtifactImage newImage(ContainerSource source, Wirelet... wirelets) {
        return ArtifactImage.build(source, wirelets);
    }

    // runMain????.. maybe still so similar. Do we want to throw Exception???
    // I think so... Wirelet.throw(Exception.class); <- Argument to runThrowing...
    // executeMain
    static protected void run(ContainerSource source, String[] args, Wirelet... wirelets) {

        // CTRL-C ?? Obvious a wirelet, but default on or default off.
        // Paa Bundle syntes jeg den er paa, ikke paa App which is clean
        run(source, ServiceWirelets.provide(StringArgs.of(args)).andThen(wirelets)); // + CTRL-C
    }

    static protected void run(ContainerSource source, Wirelet... wirelets) {
        App.run(source, wirelets);
    }

}
