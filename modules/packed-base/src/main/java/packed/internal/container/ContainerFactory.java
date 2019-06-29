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

import app.packed.app.App;
import app.packed.container.ArtifactImage;
import app.packed.container.ArtifactType;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerBundle;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import packed.internal.support.AppPackedContainerSupport;

/**
 *
 */
public class ContainerFactory {

    public static App appOf(ContainerSource source, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        if (source instanceof PackedArtifactImage) {
            return ((PackedArtifactImage) source).newApp(wirelets);
        }
        PackedContainerConfiguration configuration = new PackedContainerConfiguration(ArtifactType.APP, InternalContainerSource.of(source), wirelets);
        return new PackedApp(configuration.buildContainer());
    }

    public static ArtifactImage createImage(ContainerSource source, Wirelet... wirelets) {
        if (source instanceof PackedArtifactImage) {
            return ((PackedArtifactImage) source).newImage(wirelets);
        }
        PackedContainerConfiguration c = new PackedContainerConfiguration(ArtifactType.ARTIFACT_IMAGE, InternalContainerSource.forImage(source), wirelets);
        return new PackedArtifactImage(c.build());
    }

    public static BundleDescriptor descriptorOf(ContainerSource source) {
        requireNonNull(source, "source is null");
        ContainerBundle bundle = (ContainerBundle) source;
        PackedContainerConfiguration conf = new PackedContainerConfiguration(ArtifactType.ANALYZE, InternalContainerSource.of(source));
        BundleDescriptor.Builder builder = new BundleDescriptor.Builder(bundle.getClass());
        conf.buildDescriptor(builder);
        return builder.build();
    }

    public static Injector injectorOf(Consumer<? super InjectorConfigurator> configurator, Wirelet... wirelets) {
        requireNonNull(configurator, "configurator is null");
        PackedContainerConfiguration configuration = new PackedContainerConfiguration(ArtifactType.INJECTOR, InternalContainerSource.ofConsumer(configurator),
                wirelets);
        configurator.accept(new InjectorConfigurator(configuration));
        return configuration.buildInjector();
    }

    public static Injector injectorOf(ContainerSource source, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        if (source instanceof PackedArtifactImage) {
            return ((PackedArtifactImage) source).newInjector(wirelets);
        }
        ContainerBundle bundle = (ContainerBundle) source;
        PackedContainerConfiguration configuration = new PackedContainerConfiguration(ArtifactType.INJECTOR, InternalContainerSource.of(source), wirelets);
        AppPackedContainerSupport.invoke().doConfigure(bundle, configuration);
        return configuration.buildInjector();
    }
}
