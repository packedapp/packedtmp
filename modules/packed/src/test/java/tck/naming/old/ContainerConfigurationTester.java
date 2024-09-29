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
package tck.naming.old;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.assembly.BaseAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.service.ProvidableBeanConfiguration;

/**
 *
 */
class ContainerConfigurationTester {

    private final ContainerConfiguration cc;

    public ContainerConfigurationTester(ContainerConfiguration cc) {
        this.cc = requireNonNull(cc);
    }

    /**
     * @return the cc
     */
    public ContainerConfiguration configuration() {
        return cc;
    }

    public Set<Class<? extends Extension<?>>> extensions() {
        return cc.extensionTypes();
    }

    public ContainerConfigurationTester getNameIs(String expected) {

        // cc.path().parent().toString();
        // assertThat(cc.getName()).isEqualTo(expected);
        return this;
    }

    public <T> ProvidableBeanConfiguration<T> stateless(Class<T> implementation) {
        ProvidableBeanConfiguration<T> conf = cc.use(BaseExtension.class).install(implementation);
        assertThat(conf).isNotNull();
        return conf;
    }

    public <T> ProvidableBeanConfiguration<T> installInstance(T instance) {
        ProvidableBeanConfiguration<T> conf = cc.use(BaseExtension.class).installInstance(instance);
        assertThat(conf).isNotNull();
        return conf;
    }

    public ContainerConfigurationTester link(String name, BaseAssembly child, Wirelet... wirelets) {
        use(BaseExtension.class).link(name, child, wirelets);
        return this;
    }

    public ContainerConfigurationTester pathIs(String expected) {
//        assertThat(cc.path().toString()).isEqualTo(expected);
        return this;
    }


    public ContainerConfigurationTester setName(String name) {
        assertThat(cc.named(name)).isEqualTo(cc);
        return this;
    }

    public <T extends Extension<T>> T use(Class<T> extensionClass) {
        return cc.use(extensionClass);
    }

    public <T extends Extension<T>> ContainerConfigurationTester use(Class<T> extensionClass, Consumer<? super T> consumer) {
        consumer.accept(cc.use(extensionClass));
        return this;
    }
}
