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
package zets.name.spi;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import app.packed.container.ContainerConfiguration;

/**
 *
 */
public class ContainerConfigurationTester {

    private final ContainerConfiguration cc;

    public ContainerConfigurationTester(ContainerConfiguration cc) {
        this.cc = requireNonNull(cc);
    }

    public ContainerConfigurationTester nameIs(String name) {
        assertThat(cc.getName()).isEqualTo(name);
        return this;
    }

    public ContainerConfigurationTester pathIs(String path) {
        assertThat(cc.path().toString()).isEqualTo(path);
        return this;
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
}
