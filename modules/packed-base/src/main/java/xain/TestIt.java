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
package xain;

/**
 *
 */
public class TestIt {

    public static void main(String[] args) {

        System.out.println(TestIt.class.getModule().getDescriptor().version());

        // Container co = App.of(c -> {
        // c.install("Hej");
        // c.install(132);
        // c.install(132L);
        // c.install(132L).asNone();
        // c.install(132L).asNone();
        // for (int i = 0; i < 1_000_000; i++) {
        // c.install(132L).asNone();
        // }
        // }, OtherWiringOperation.disableConfigurationSite());
        System.out.println("Done");
        // co.components().forEach(e -> {
        // System.out.println(e.configurationSite());
        // System.out.println(e.path());
        // });
        // System.out.println(co.components().count());

        // Jeg tror vi skal have et sorteret Map....

        // Og saa har vi nogle forskellige instanser....
        // Og en counter ved siden af...

        //// Children Types... Eneste problem er den write component.children collection.... Skal jo kende det...
        // Null
        // Map.of(1)
        // TreeMap
        // ConcurrentSkipMap (Det eneste map man aendrer paa runtime)

        // Eller ogsaa laver vi mappet om fra et statiks (Map.copyOf) map til et Concurrene paa foerste runtime insert...
        // Det tror jeg maaske er maaden... Det er _faa_ noder der vil lave runtime insert...

        // Vi kan endda have en meget lille

    }
}
