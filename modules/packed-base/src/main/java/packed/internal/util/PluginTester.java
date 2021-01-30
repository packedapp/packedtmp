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
package packed.internal.util;

import app.packed.cli.Main;
import app.packed.container.BaseAssembly;
import app.packed.inject.ServiceLocator;
import app.packed.state.OnInitialize;

/**
 *
 */
// Foerst lav den uden Qualifier...
// Since we cannot have multiple services with the same key
// We add a qualifier to every
public class PluginTester extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
//        service().anchorAll();
//        for (Plugin p : ServiceLoader.load(Plugin.class)) {
//            String pluginName = p.getClass().getCanonicalName();
//            link(p, ServiceWirelets.to(t -> t.rekeyAllWithTag(pluginName)));
//        }
        provide(UseIt.class);
    }

    public static void main(String[] args) {
        Main.run(new PluginTester());
    }

    public static class UseIt {

        public UseIt() {
            System.out.println("NEWIT");
        }
        @OnInitialize
        public void show(ServiceLocator l) {
            l.selectWithAnyQualifiers(Runnable.class).forEachInstance((s, r) -> r.run());
        }
    }
}
//
//@Retention(RetentionPolicy.RUNTIME)
//@Qualifier
//// Visibility problemer mht til PluginType...
//// Taenker det er fint bare Packed har adgang...
//// Men altsaa saa kan vi jo instantiere annotationer
//// Maaske er det en instans metode paa ServiceExtension, BaseBundle...
//@interface PluginType {
//    Class<? extends Plugin> value();
//}
