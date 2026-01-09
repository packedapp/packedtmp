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
package internal.app.packed.bean;

import java.util.function.Function;

import app.packed.bean.BeanLifetime;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.PackedOperationTemplate;

/** Implementation of {@link BeanTemplate}. */
public record PackedBeanTemplate(BeanLifetime beanKind, PackedOperationTemplate initializationTemplate) {

    public static PackedBuilder builder(BeanLifetime kind) {
        return new PackedBuilder(kind);
    }

    /**
     * Create a new bean installer from this template.
     *
     * @param installingExtension
     *            the extension that is installing the bean
     * @param owner
     *            the owner of the bean
     * @return the new bean installer
     */
    public PackedBeanInstaller newInstaller(ExtensionSetup installingExtension, AuthoritySetup<?> owner) {
        return new PackedBeanInstaller(this, installingExtension, owner);
    }

    public static final class PackedBuilder {
        private final BeanLifetime beanKind;
        private PackedOperationTemplate initializationTemplate = PackedOperationTemplate.DEFAULTS;

        PackedBuilder(BeanLifetime beanKind) {
            this.beanKind = beanKind;
        }

        public PackedBuilder initialization(PackedOperationTemplate initialization) {
            this.initializationTemplate = initialization;
            return this;
        }

        public PackedBuilder initialization(Function<PackedOperationTemplate, PackedOperationTemplate> configure) {
            this.initializationTemplate = configure.apply(this.initializationTemplate);
            return this;
        }

        public PackedBeanTemplate build() {
            return new PackedBeanTemplate(beanKind,  initializationTemplate);
        }
    }
}
