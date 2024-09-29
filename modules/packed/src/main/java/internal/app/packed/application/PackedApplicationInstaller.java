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
import java.util.function.Function;

import app.packed.application.ApplicationBuildLocal;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationTemplate;
import app.packed.application.ApplicationTemplate.Installer;
import app.packed.assembly.Assembly;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.lifecycle.LifecycleKind;
import internal.app.packed.build.PackedBuildProcess;
import internal.app.packed.component.PackedComponentInstaller;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public final class PackedApplicationInstaller<A> extends PackedComponentInstaller<ApplicationSetup, PackedApplicationInstaller<A>>
        implements ApplicationTemplate.Installer<A> {

    public BuildApplicationRepository bar;

    /** The build process the application is part of. */
    public final PackedBuildProcess buildProcess;

    /** The installer for the root container of the application. */
    public final PackedContainerInstaller containerInstaller;

    // I would like to time stuff. But I have no idea on how to do it reliable with all the laziness
    long creationNanos;

    /** The function used to create a new application handle, when the application has been installed. */
    Function<? super ApplicationTemplate.Installer<A>, ?> handleFactory = ApplicationHandle::new;

    public MethodHandle launcher;

    public final LifecycleKind lk;

    public boolean optionBuildApplicationLazy;

    public boolean optionBuildReusableImage;

    /** The template of the application. */
    final PackedApplicationTemplate<?> template;

    PackedApplicationInstaller(PackedApplicationTemplate<?> template, BuildGoal goal) {
        this.template = template;
        this.lk = template.containerTemplate().lifecycleKind();
        this.containerInstaller = new PackedContainerInstaller(template.containerTemplate(), this, null, null);
        this.buildProcess = new PackedBuildProcess(this, goal);
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationSetup application(ApplicationSetup component) {
        return component;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <H extends ApplicationHandle<?, A>> H install(Assembly assembly, Function<? super Installer<A>, H> handleFactory, Wirelet... wirelets) {
        checkNotInstalledYet();
        requireNonNull(assembly, "assembly is null");
        this.handleFactory = requireNonNull(handleFactory, "handleFactory is null");

        // Prepare the ScopedValue.Carrier that sets the for setting the build process for the build thread
        Carrier c = buildProcess.carrier();

        ContainerSetup container;
        try {
            container = c.call(() -> containerInstaller.invokeAssemblyBuild(assembly));
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        } finally {
            buildProcess.finished();
        }
        return (H) container.application.handle();
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedApplicationInstaller<A> setLocal(ApplicationBuildLocal<T> local, T value) {
        return super.setLocal(local, value);
    }
}