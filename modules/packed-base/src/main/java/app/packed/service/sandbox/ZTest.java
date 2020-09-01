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
package app.packed.service.sandbox;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;

import app.packed.artifact.App;
import app.packed.artifact.Image;
import app.packed.base.Attribute;
import app.packed.base.AttributeProvide;
import app.packed.component.ComponentAnalyzer;
import app.packed.container.BaseBundle;

/**
 *
 */
public class ZTest extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        lookup(MethodHandles.lookup());
        installInstance("asdasd");
        provide(XX.class);
        provideInstance(123);

        export(Integer.class);
        link(new MM());
    }

    public static void main(String[] args) {
        ComponentAnalyzer.print(new ZTest());

        Image<App> a = App.newImage(new ZTest());
        System.out.println();
        ComponentAnalyzer.print(a); // <--- must have access to all code..:(

        // If we use build-plugin... we can have the experience in a side browser window...
        // Ohh wow, vi kan faktisk se hvad der bliver aendret pga en restart...
        // Packed Developer Console
        // Explorer.writeStaticSite(MethodHandles.lookup(), a, "/asdasd"); //Would be nice with a little deamon

        // Vi kan have en historik over hvad der er blevet deployet i et cluster....
        // Lokalt paa en maskine... OSV...

        App.initialize(new ZTest());
    }

    static class MM extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            install(XX.class);
            service();
            System.out.println("NICE");
        }

    }

    public static class XX {
        public static Attribute<LocalDateTime> TIME = Attribute.of(MethodHandles.lookup(), "time", LocalDateTime.class);

        XX() {
            new Exception().printStackTrace();
        }

        @AttributeProvide(by = XX.class, name = "time")
        public LocalDate now() {
            return LocalDate.now();
        }
    }
}
