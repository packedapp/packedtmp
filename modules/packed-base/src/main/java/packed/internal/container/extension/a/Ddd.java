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
package packed.internal.container.extension.a;

import app.packed.app.App;
import app.packed.app.AppBundle;
import app.packed.inject.ServiceWirelets;

/**
 *
 */
public class Ddd extends AppBundle {

    @Override
    public void configure() {
        export(provideConstant("Foo"));
        export(provideConstant(23));
        installConstant(new Ssss());
    }

    public static void main(String[] args) {
        // ArtifactImage ai = newImage(new Ddd());
        System.out.println();
        App.of(new Ddd(), ServiceWirelets.peekDownstream(e -> System.out.println("Exporting " + e.key() + " via " + e.configSite())));
    }

    public static class Ssss {

        @MyAnnotation(2132)
        public String ss = "ffdddf";

        @MyAnnotation(1254)
        public void foo() {
            System.out.println("INSTANCE FOO!");
        }
    }
}
