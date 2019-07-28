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
package app.packed.app;

import app.packed.artifact.ArtifactImage;
import app.packed.artifact.ArtifactSource;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;
import app.packed.inject.ServiceWirelets;
import app.packed.lifecycle.StringArgs;

/**
 *
 */
public abstract class AppBundle extends Bundle {

    protected static ArtifactImage newImage(ArtifactSource source, Wirelet... wirelets) {
        return ArtifactImage.of(source, wirelets);
    }

    // runMain????.. maybe still so similar. Do we want to throw Exception???
    // I think so... Wirelet.throw(Exception.class); <- Argument to runThrowing...
    // executeMain
    static protected void run(ArtifactSource source, String[] args, Wirelet... wirelets) {

        // CTRL-C ?? Obvious a wirelet, but default on or default off.
        // Paa Bundle syntes jeg den er paa, ikke paa App which is clean
        run(source, ServiceWirelets.provide(StringArgs.of(args)).andThen(wirelets)); // + CTRL-C
    }

    static protected void run(ArtifactSource source, Wirelet... wirelets) {
        App.run(source, wirelets);
    }
}

class MyBundle extends AppBundle {

    private static final ArtifactImage IMAGE = newImage(new MyBundle());

    @Override
    protected void configure() {
        lifecycle().main(() -> System.out.println("HelloWorld"));
    }

    public static void main(String[] args) {
        run(IMAGE);
    }
}