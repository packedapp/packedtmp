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
package packed.internal.config.site;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import app.packed.config.ConfigSite;
import app.packed.config.ConfigSiteVisitor;

/**
 *
 */
// Hmm er den brugt nogen steder???
public final class AnnotatedTypeConfigSite extends AbstractConfigSite {

    final Class<?> clazz;

    final Annotation annotation;

    AnnotatedTypeConfigSite(AbstractConfigSite parent, String operation, Class<?> clazz, Annotation annotation) {
        super(parent, operation);
        this.clazz = requireNonNull(clazz);
        this.annotation = requireNonNull(annotation);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite replaceParent(ConfigSite newParent) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(ConfigSiteVisitor visitor) {
        throw new UnsupportedOperationException();
    }
}
