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
package zets.Extension;

import app.packed.container.ArtifactBuildContext;
import app.packed.container.ContainerExtension;
import app.packed.container.WireletList;

/**
 *
 */
public class ExtensionStubs {

    public static class TestExtension extends ContainerExtension<TestExtension> {

        public ArtifactBuildContext publicBuildContext() {
            return buildContext();
        }

        public WireletList publicWirelets() {
            return wirelets();
        }
    }

    public static class TestExtension1 extends ContainerExtension<TestExtension1> {}

    public static class TestExtension2 extends ContainerExtension<TestExtension2> {}

    public static class TestExtension3 extends ContainerExtension<TestExtension3> {}
}
