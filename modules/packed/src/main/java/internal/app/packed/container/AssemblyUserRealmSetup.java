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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.application.ApplicationInfo.ApplicationBuildType;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationDriver;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * A component installer created from an {@link Assembly} instance.
 */
public final class AssemblyUserRealmSetup extends UserRealmSetup {

    /** A handle that can invoke {@link Assembly#doBuild()}. */
    private static final MethodHandle MH_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Assembly.class, "doBuild", void.class,
            AssemblyUserRealmSetup.class, ContainerConfiguration.class);

    public final ApplicationSetup application;

    /** The assembly used to create this installer. */
    final Assembly assembly;

    /** Or model of the assembly. */
    private final AssemblyModel assemblyModel;

    /** The root component of this realm. */
    private final ContainerSetup container;

    // Naar vi har faaet styr paa container drivers osv.
    // Flytter vi dem ned i UserRealm
    private final PackedContainerDriver driver;

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
    public AssemblyUserRealmSetup(PackedApplicationDriver<?> applicationDriver, ApplicationBuildType buildTarget, Assembly assembly, Wirelet[] wirelets) {
        this.assembly = requireNonNull(assembly, "assembly is null");
        this.application = new ApplicationSetup(applicationDriver, buildTarget, this, wirelets);
        this.assemblyModel = AssemblyModel.of(assembly.getClass());

        this.container = application.container;
        this.driver = new PackedContainerDriver(container);
    }

    public AssemblyUserRealmSetup(PackedContainerDriver driver, ContainerSetup linkTo, Assembly assembly, Wirelet[] wirelets) {
        this.application = linkTo.application;
        this.assembly = requireNonNull(assembly, "assembly is null");
        this.assemblyModel = AssemblyModel.of(assembly.getClass());
        // if embed do xxx
        // else create new container
        this.container = new ContainerSetup(application, this, driver, linkTo, wirelets);
        this.driver = driver;
    }

    public void build() {
        // Invoke Assembly::doBuild
        // which in turn will invoke Assembly::build
        ContainerConfiguration configuration = driver.toConfiguration(container);
        try {
            MH_ASSEMBLY_DO_BUILD.invokeExact(assembly, this, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the realm, if the application has been built successfully (no exception was thrown)
        close();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup container() {
        return container;
    }

    public void postBuild(ContainerConfiguration configuration) {
        assemblyModel.postBuild(configuration);
    }

    public void preBuild(ContainerConfiguration configuration) {
        assemblyModel.preBuild(configuration);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> realmType() {
        return assembly.getClass();
    }
}
