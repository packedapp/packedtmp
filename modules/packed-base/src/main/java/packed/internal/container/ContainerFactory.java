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
import app.packed.container.AnyBundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerImage;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;

/**
 *
 */
public class ContainerFactory {

    public static ContainerImage imageOf(ContainerSource source, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        AnyBundle bundle = (AnyBundle) source;
        DefaultContainerConfiguration configuration = new DefaultContainerConfiguration(null, WiringType.APP, bundle.getClass(), bundle, wirelets);
        return configuration.buildImage();
    }

    public static App appOf(ContainerSource source, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        if (source instanceof DefaultContainerImage) {
            DefaultContainerImage image = (DefaultContainerImage) source;
            return new DefaultApp(image.dcc.buildFromImage());
        }
        AnyBundle bundle = (AnyBundle) source;
        DefaultContainerConfiguration configuration = new DefaultContainerConfiguration(null, WiringType.APP, bundle.getClass(), bundle, wirelets);
        return new DefaultApp(configuration.buildContainer());
    }

    public static Injector injectorOf(ContainerSource source, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        AnyBundle bundle = (AnyBundle) source;
        DefaultContainerConfiguration configuration = new DefaultContainerConfiguration(null, WiringType.INJECTOR, bundle.getClass(), bundle, wirelets);
        bundle.doConfigure(configuration);
        return configuration.buildInjector();
    }

    public static Injector injectorOf(Consumer<? super InjectorConfigurator> configurator, Wirelet... wirelets) {
        requireNonNull(configurator, "configurator is null");
        // Hmm vi burde have en public version af ContainerBuilder
        // Dvs. vi naar vi lige praecis har fundet ud af hvordan det skal fungere...
        DefaultContainerConfiguration builder = new DefaultContainerConfiguration(null, WiringType.INJECTOR, configurator.getClass(), null, wirelets);
        configurator.accept(new InjectorConfigurator(builder));
        return builder.buildInjector();
    }

    public static BundleDescriptor of(ContainerSource source) {
        requireNonNull(source, "source is null");
        AnyBundle bundle = (AnyBundle) source;
        DefaultContainerConfiguration conf = new DefaultContainerConfiguration(null, WiringType.DESCRIPTOR, bundle.getClass(), bundle);
        BundleDescriptor.Builder builder = new BundleDescriptor.Builder(bundle.getClass());
        conf.buildDescriptor(builder);
        return builder.build();
    }

    public static class ConfiguratorWrapper implements ContainerSource {
        final Consumer<? super InjectorConfigurator> configurator;

        ConfiguratorWrapper(Consumer<? super InjectorConfigurator> configurator) {
            this.configurator = requireNonNull(configurator, "configurator is null");
        }

        public Class<?> getType() {
            return configurator.getClass();
        }
    }

}
