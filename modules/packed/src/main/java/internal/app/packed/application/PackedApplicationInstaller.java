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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.lang.ScopedValue.Carrier;
import java.lang.invoke.MethodHandle;
import java.util.UUID;

import app.packed.application.ApplicationBuildLocal;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.assembly.Assembly;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup.ApplicationBuildPhase;
import internal.app.packed.build.PackedBuildProcess;
import internal.app.packed.component.AbstractComponentInstaller;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.util.ThrowableUtil;

/** Implementation of {@link ApplicationInstaller}. */
public final class PackedApplicationInstaller<H extends ApplicationHandle<?, ?>>
        extends AbstractComponentInstaller<ApplicationSetup, PackedApplicationInstaller<H>> implements ApplicationInstaller<H> {

    /** The build process the application is part of. */
    public final PackedBuildProcess buildProcess;

    /** The installer for the root container of the application. */
    public final PackedContainerInstaller<?> containerInstaller;

    // I would like to time stuff. But I have no idea on how to do it reliable with all the laziness
    long creationNanos;

    public final boolean isManaged;

    // This is the guest bean that is created
    @Nullable
    public final MethodHandle launcher;

    public String name = UUID.randomUUID().toString();

    public boolean optionBuildApplicationLazy;

    public boolean optionBuildReusableImage;

    /** The template of the application. */
    final PackedApplicationTemplate<?> template;

    PackedApplicationInstaller(PackedApplicationTemplate<?> template, @Nullable MethodHandle launcher, BuildGoal goal) {
        this.template = template;
        this.isManaged = template.rootContainer().isManaged();
        this.containerInstaller = new PackedContainerInstaller<>(template.rootContainer(), this, null, null);
        this.buildProcess = new PackedBuildProcess(this, goal);
        this.launcher = launcher;
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationSetup application(ApplicationSetup component) {
        return component;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public H install(Assembly assembly, Wirelet... wirelets) {
        checkNotUsed();
        requireNonNull(assembly, "assembly is null");

        // Prepare the ScopedValue.Carrier that sets the build process for the build thread
        Carrier c = buildProcess.carrier();

        ContainerSetup container;
        try {
            container = c.call(() -> {
                return containerInstaller.invokeAssemblyBuild(assembly);
            });
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        } finally {
            buildProcess.finished();
        }
        container.application.phase = ApplicationBuildPhase.COMPLETED;
        return (H) container.application.handle();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationInstaller<H> named(String name) {
        this.name = requireNonNull(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedApplicationInstaller<H> setLocal(ApplicationBuildLocal<T> local, T value) {
        return super.setLocal(local, value);
    }
}