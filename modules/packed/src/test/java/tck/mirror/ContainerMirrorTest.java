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
package tck.mirror;

import org.junit.jupiter.api.Test;

import app.packed.container.ContainerMirror;

/**
 *
 */
public class ContainerMirrorTest extends AbstractMirrorTest {

    /** We cannot create a usable ContainerMirror ourselves. */
    @Test
    public void frameworkMustInitializeMirror() {
        frameworkMustInitialize(() -> new ContainerMirror().application());
    }

    @Test
    public void helloWorldApp() {
       // ContainerMirror m = HW.container();


    }
}
