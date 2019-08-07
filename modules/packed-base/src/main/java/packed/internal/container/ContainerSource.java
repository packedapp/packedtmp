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

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.artifact.ArtifactSource;
import app.packed.container.Bundle;
import packed.internal.componentcache.ContainerConfiguratorCache;

/**
 *
 */

// Ideen er vi wrapper container sourcen.
// Saa vi kan returner. Den klasse vi skal bruge som standard for lookups.
// Default name

// ??? Do we want to include wirelets???

// T what we want to return????

// SourceDescriptor?

// I virkeligheden er den vel en Configurator...
// ContainerConfigurator
public class ContainerSource {

    final Class<?> configuratorType;

    public final ArtifactSource source;

    ContainerSource(ArtifactSource source, Class<?> configuratorType) {
        this.source = source;
        this.configuratorType = requireNonNull(configuratorType);
    }

    ContainerConfiguratorCache cache() {
        return ContainerConfiguratorCache.of(configuratorType);
    }

    public static ContainerSource forImage(ArtifactSource source) {
        requireNonNull(source, "source is null");
        Bundle b = (Bundle) source;
        return new ContainerSource(b, source.getClass());
    }

    public static ContainerSource of(ArtifactSource source) {
        Bundle b = (Bundle) source;
        return new ContainerSource(b, source.getClass());
    }

    public static ContainerSource forApp(ArtifactSource source) {
        requireNonNull(source, "source is null");
        return new ContainerSource(source, source.getClass());
    }

    public static ContainerSource ofConsumer(Consumer<?> consumer) {
        return new ContainerSource(null, consumer.getClass());
    }

    public ContainerSource link(Bundle bundle) {
        throw new UnsupportedOperationException();
    }
}
