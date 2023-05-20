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
package usage.container;

import app.packed.application.App;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class AssemblyFinderUsage extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {

//        assemblyFinder().paths("/Users/kaspernielsen/packed-workspace/packed-usage-on-modulepath/bin").linkOne("app.packed.usage",
//                "app.packed.application.usage.HelloWorldAssembly");
        // beanFinder
        // Maybe it should be used standalone here
        link(assemblyFinder().paths("/Users/kaspernielsen/packed-workspace/packed-usage-on-modulepath/bin").findOne("app.packed.usage",
                "app.packed.application.usage.HelloWorldAssembly"));
    }

    public static void main(String[] args) {
        App.run(new AssemblyFinderUsage());

//        App.imageOf(AssemblyFinder.onClasspath().findOne("foofs.sdfsdf"));

        // ®App.mirrorOf(new AssemblyFinderUsage()).assemblies().print();
    }
}
