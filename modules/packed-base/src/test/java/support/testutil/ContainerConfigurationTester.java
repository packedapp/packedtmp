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
package support.testutil;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;

/**
 *
 */
public class ContainerConfigurationTester {

    private final ContainerConfiguration cc;

    public ContainerConfigurationTester(ContainerConfiguration cc) {
        this.cc = requireNonNull(cc);
    }

    public ArtifactBuildContext buildContext() {
        return cc.buildContext();
    }

    public ConfigSite configSite() {
        return cc.configSite();
    }

    /**
     * @return the cc
     */
    public ContainerConfiguration configuration() {
        return cc;
    }

    public Set<Class<? extends Extension>> extensions() {
        return cc.extensions();
    }

    public ContainerConfigurationTester getNameIs(String expected) {
        assertThat(cc.getName()).isEqualTo(expected);
        return this;
    }

    public ComponentConfiguration installHelper(Class<?> implementation) {
        ComponentConfiguration conf = cc.use(ComponentExtension.class).installHelper(implementation);
        assertThat(conf).isNotNull();
        return conf;
    }

    public ContainerConfigurationTester isConfigurable() {
        try {
            cc.checkConfigurable();
            return this;
        } catch (IllegalStateException e) {
            throw new AssertionError("Expected to be configurable, but was not", e);
        }
    }

    public ContainerConfigurationTester isNotConfigurable() {
        try {
            cc.checkConfigurable();
            throw new AssertionError("Expected to be not configurable, but was");
        } catch (IllegalStateException e) {
            // Check name???
            return this;
        }
    }

    public ContainerConfigurationTester link(Bundle child, Wirelet... wirelets) {
        cc.link(child, wirelets);
        return this;
    }

    public ComponentPath path() {
        return cc.path();
    }

    public ContainerConfigurationTester pathIs(String expected) {
        assertThat(cc.path().toString()).isEqualTo(expected);
        return this;
    }

    public ContainerConfigurationTester setName(String name) {
        assertThat(cc.setName(name)).isEqualTo(cc);
        return this;
    }

    public <T extends Extension> T use(Class<T> extensionType) {
        return cc.use(extensionType);
    }

    public <T extends Extension> ContainerConfigurationTester use(Class<T> extensionType, Consumer<? super T> consumer) {
        consumer.accept(cc.use(extensionType));
        return this;
    }

    public WireletList wirelets() {
        return cc.wirelets();
    }
}
