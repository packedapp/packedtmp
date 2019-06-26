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

import app.packed.container.AnyBundle;
import app.packed.container.BuildContext;
import app.packed.container.BundleDescriptor;
import app.packed.container.ContainerImage;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import packed.internal.support.AppPackedContainerSupport;

/**
 *
 */
public class ContainerFactory {

    // Flyt default App til .app
    // Image skal vide om vi kan lave en Injector...
    public static DefaultApp appOf(ContainerSource source, Wirelet... wirelets) {
        DefaultContainerConfiguration configuration = new DefaultContainerConfiguration(null, BuildContext.OutputType.APP, InternalContainerSource.of(source),
                wirelets);
        return new DefaultApp(configuration.buildContainer());
    }

    public static BundleDescriptor descriptorOf(ContainerSource source) {
        requireNonNull(source, "source is null");
        AnyBundle bundle = (AnyBundle) source;
        DefaultContainerConfiguration conf = new DefaultContainerConfiguration(null, BuildContext.OutputType.ANALYZE, InternalContainerSource.of(source));
        BundleDescriptor.Builder builder = new BundleDescriptor.Builder(bundle.getClass());
        conf.buildDescriptor(builder);
        return builder.build();
    }

    public static ContainerImage imageOf(ContainerSource source, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        DefaultContainerConfiguration configuration = new DefaultContainerConfiguration(null, BuildContext.OutputType.CONTAINER_IMAGE,
                InternalContainerSource.of(source), wirelets);
        return configuration.buildImage();
    }

    public static Injector injectorOf(Consumer<? super InjectorConfigurator> configurator, Wirelet... wirelets) {
        requireNonNull(configurator, "configurator is null");
        // Hmm vi burde have en public version af ContainerBuilder
        // Dvs. vi naar vi lige praecis har fundet ud af hvordan det skal fungere...
        DefaultContainerConfiguration builder = new DefaultContainerConfiguration(null, BuildContext.OutputType.INJECTOR,
                InternalContainerSource.ofConsumer(configurator), wirelets);
        configurator.accept(new InjectorConfigurator(builder));
        return builder.buildInjector();
    }

    public static Injector injectorOf(ContainerSource source, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        AnyBundle bundle = (AnyBundle) source;
        DefaultContainerConfiguration configuration = new DefaultContainerConfiguration(null, BuildContext.OutputType.INJECTOR,
                InternalContainerSource.of(source), wirelets);
        AppPackedContainerSupport.invoke().doConfigure(bundle, configuration);
        return configuration.buildInjector();
    }
}
