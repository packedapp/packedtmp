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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.application.ApplicationDescriptor.ApplicationBuildType;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class AssemblyRealmSetup extends RealmSetup {

    /** A handle that can invoke {@link Assembly#doBuild()}. Is here because I have no better place to put it. */
    private static final MethodHandle MH_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Assembly.class, "doBuild", void.class,
            ContainerConfiguration.class);

    final Assembly assembly;

    private final ContainerConfiguration configuration;

    public final ApplicationSetup application;

    // Den giver kun mening for assemblies...
    /** The root component of this realm. */
    public final ContainerSetup container;
    
    /**
     * Builds an application using the specified assembly and optional wirelets.
     * 
     * @param buildTarget
     *            the build target
     * @param assembly
     *            the assembly of the application
     * @param wirelets
     *            optional wirelets
     * @return the application
     */
    public AssemblyRealmSetup(PackedApplicationDriver<?> applicationDriver, ApplicationBuildType buildTarget, Assembly assembly, Wirelet[] wirelets) {
        this.assembly = requireNonNull(assembly, "assembly is null");
        this.application = new ApplicationSetup(applicationDriver, buildTarget, this, wirelets);
        this.container = application.container;
        this.configuration = applicationDriver.containerDriver.toConfiguration(container);
        wireCommit(container);
    }

    public AssemblyRealmSetup(PackedContainerDriver driver, ContainerSetup linkTo, Assembly assembly, Wirelet[] wirelets) {
        this.application = linkTo.application;
        this.assembly = requireNonNull(assembly, "assembly is null");
        // if embed do xxx
        // else create new container
        this.container = new ContainerSetup(application, this, application.container.lifetime, driver, linkTo, wirelets);
        this.configuration = driver.toConfiguration(container);
    }

    public void build() {
        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        // This will recursively call down through any sub-containers that are linked
        try {
            MH_ASSEMBLY_DO_BUILD.invokeExact(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the realm, if the application has been built successfully (no exception was thrown)
        close();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> realmType() {
        return assembly.getClass();
    }
}
