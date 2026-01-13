/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package usage.container;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import app.packed.application.App;
import app.packed.assembly.AssemblyModulepathFinder;
import app.packed.assembly.BaseAssembly;

/**
 *
 */
public class AssemblyFinderUsage extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        // Use AssemblyModulepathFinder to find assemblies from external module paths
        AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup())
                .withPaths(Path.of("/Users/kaspernielsen/packed-workspace/packed-usage-on-modulepath/bin"));
        link(finder.findOne("app.packed.usage", "app.packed.application.usage.HelloWorldAssembly"), "child");
    }

    public static void main(String[] args) {
        App.run(new AssemblyFinderUsage());
    }
}
