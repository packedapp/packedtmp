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
package packed.internal.container;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import app.packed.container.Extension;
import app.packed.sidecar.Packlet;

/**
 *
 */
public class TarjanTest {

    @Test
    public void simple() {
        assertThat(ExtensionDependencyValidator.dependenciesOf(Tarj.class)).isEmpty();
        assertThat(ExtensionDependencyValidator.dependenciesOf(TarjNoAnnotation.class)).isEmpty();
        assertThat(ExtensionDependencyValidator.dependenciesOf(Tarj1.class)).containsExactly(Tarj.class);
        assertThat(ExtensionDependencyValidator.dependenciesOf(Tarj2.class)).containsExactly(Tarj.class);
        assertThat(ExtensionDependencyValidator.dependenciesOf(Tarj12.class)).containsExactly(Tarj1.class, Tarj2.class);
    }

    private class TarjNoAnnotation extends Extension {}

    @Packlet(extension = {})
    private class Tarj extends Extension {}

    @Packlet(extension = Tarj.class)
    private class Tarj1 extends Extension {}

    @Packlet(extension = Tarj.class)
    private class Tarj2 extends Extension {}

    @Packlet(extension = { Tarj1.class, Tarj2.class })
    private class Tarj12 extends Extension {}
}
