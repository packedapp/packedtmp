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
package testutil.util;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.NamespacePath;
import app.packed.bean.BeanExtension;
import app.packed.bean.ContainerBeanConfiguration;
import app.packed.bundle.BaseAssembly;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.Wirelet;
import app.packed.extension.Extension;

/**
 *
 */
public class ContainerConfigurationTester {

    private final BundleConfiguration cc;

    public ContainerConfigurationTester(BundleConfiguration cc) {
        this.cc = requireNonNull(cc);
    }

    /**
     * @return the cc
     */
    public BundleConfiguration configuration() {
        return cc;
    }

    public Set<Class<? extends Extension>> extensions() {
        return cc.extensionsTypes();
    }

    public ContainerConfigurationTester getNameIs(String expected) {
        
        //cc.path().parent().toString();
        //assertThat(cc.getName()).isEqualTo(expected);
        return this;
    }

    public <T> ContainerBeanConfiguration<T> stateless(Class<T> implementation) {
        ContainerBeanConfiguration<T> conf = cc.use(BeanExtension.class).install(implementation);
        assertThat(conf).isNotNull();
        return conf;
    }

    public <T> ContainerBeanConfiguration<T> installInstance(T instance) {
        ContainerBeanConfiguration<T> conf = cc.use(BeanExtension.class).installInstance(instance);
        assertThat(conf).isNotNull();
        return conf;
    }

    public ContainerConfigurationTester link(BaseAssembly child, Wirelet... wirelets) {
        cc.link(child, wirelets);
        return this;
    }

    public NamespacePath path() {
        return cc.path();
    }

    public ContainerConfigurationTester pathIs(String expected) {
        assertThat(cc.path().toString()).isEqualTo(expected);
        return this;
    }

    public ContainerConfigurationTester setName(String name) {
        assertThat(cc.named(name)).isEqualTo(cc);
        return this;
    }

    public <T extends Extension> T use(Class<T> extensionClass) {
        return cc.use(extensionClass);
    }

    public <T extends Extension> ContainerConfigurationTester use(Class<T> extensionClass, Consumer<? super T> consumer) {
        consumer.accept(cc.use(extensionClass));
        return this;
    }
}