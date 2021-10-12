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

import java.util.ServiceLoader;

import app.packed.application.programs.Program;
import app.packed.bundle.BaseAssembly;
import app.packed.inject.service.ServiceWirelets;

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
        for (Plugin p : ServiceLoader.load(Plugin.class)) {
            String pluginName = p.getClass().getCanonicalName();
            link(p, ServiceWirelets.anchorAll(), ServiceWirelets.to(t -> t.rekeyAllWithTag(pluginName)));
        }
        exportAll();
    }

    public static void main(String[] args) {
        try (Program app = Program.start(new PluginTester())) {
            app.services().selectWithAnyQualifiers(Runnable.class).forEachInstance(Runnable::run);
        }
    }
}
// Normally a container will only retain those service that any
// of the components that belongs to the container needs.
// Discarding any other service. However, by using ServiceWirelets.anchorAll()