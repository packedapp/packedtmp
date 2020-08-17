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
package packed.internal.artifact;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.artifact.ArtifactDriver;
import app.packed.component.Bundle;
import app.packed.component.CustomConfigurator;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import packed.internal.component.BundleConfiguration;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedComponentDriver;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.PackedRealm;
import packed.internal.errorhandling.ErrorMessage;
import packed.internal.inject.ConfigSiteInjectOperations;

/** The default implementation of {@link AssembleContext} */
public final class PackedAssemblyContext implements AssembleContext {

    /** The build output. */
    final PackedOutput output;

    /** The thread that is assembling the system. */
    private final Thread thread = Thread.currentThread();

    /**
     * Creates a new build context object.
     * 
     * @param output
     *            the output of the build process
     */
    PackedAssemblyContext(PackedOutput output) {
        this.output = requireNonNull(output);
    }

    /** {@inheritDoc} */
    @Override
    public void addError(ErrorMessage message) {}

    /** {@inheritDoc} */
    @Override
    public boolean isInstantiating() {
        return false;
    }

    /**
     * Returns the build output.
     * 
     * @return the build output
     */
    public Thread thread() {
        return thread;
    }

    public static <C, D> ComponentNodeConfiguration configure(ArtifactDriver<?> ad, PackedComponentDriver<D> driver, Function<D, C> factory,
            CustomConfigurator<C> consumer, Wirelet... wirelets) {
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);

        ComponentNodeConfiguration node = new ComponentNodeConfiguration(new PackedAssemblyContext(PackedOutput.artifact(ad)), driver, cs,
                PackedRealm.fromConfigurator(consumer), wirelets);

        D conf = driver.forBundleConf(node);
        C cc = requireNonNull(factory.apply(conf));
        consumer.configure(cc);

        return node.closeAssembly();
    }

    public static ComponentNodeConfiguration assembleArtifact(ArtifactDriver<?> driver, Bundle<?> bundle, Wirelet[] wirelets) {
        return assemble(new PackedAssemblyContext(PackedOutput.artifact(driver)), bundle, wirelets);
    }

    public static ComponentNodeConfiguration assembleImage(Bundle<?> bundle, Wirelet[] wirelets) {
        return assemble(new PackedAssemblyContext(PackedOutput.image()), bundle, wirelets);
    }

    public static ComponentNodeConfiguration assembleDescriptor(Class<?> descriptorType, Bundle<?> bundle, Wirelet... wirelets) {
        return assemble(new PackedAssemblyContext(PackedOutput.descriptor(descriptorType)), bundle, wirelets);
    }

    public static ComponentNodeConfiguration assemble(PackedAssemblyContext assembly, Bundle<?> bundle, Wirelet... wirelets) {
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);

        PackedComponentDriver<?> driver = BundleConfiguration.driverOf(bundle);
        ComponentNodeConfiguration node = new ComponentNodeConfiguration(assembly, driver, cs, PackedRealm.fromBundle(bundle), wirelets);

        Object conf = driver.forBundleConf(node);
        BundleConfiguration.configure(bundle, conf);

        return node.closeAssembly();
    }

    static class PackedOutput {

        PackedOutput() {

        }

        public static PackedOutput image() {
            return new PackedOutput();
        }

        public static PackedOutput descriptor(Class<?> type) {
            return new PackedOutput();
        }

        public static PackedOutput artifact(ArtifactDriver<?> driver) {
            return new PackedOutput();
        }
    }
}
