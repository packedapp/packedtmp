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
package packed.internal.util.configurationsite;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import app.packed.config.ConfigurationSite;
import app.packed.config.ConfigurationSiteVisitor;

/**
 *
 */
public final class AnnotatedTypeConfigurationSite extends AbstractConfigurationSite {

    private final Class<?> clazz;

    final Annotation annotation;

    AnnotatedTypeConfigurationSite(ConfigurationSite parent, ConfigurationSiteType operation, Class<?> clazz, Annotation annotation) {
        super(parent, operation);
        this.clazz = requireNonNull(clazz);
        this.annotation = requireNonNull(annotation);
    }

    public Class<?> getAnnotatedType() {
        return clazz;
    }

    /** {@inheritDoc} */
    @Override
    public InternalConfigurationSite replaceParent(ConfigurationSite newParent) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(ConfigurationSiteVisitor visitor) {
        throw new UnsupportedOperationException();
    }
}
