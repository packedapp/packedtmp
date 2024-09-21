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
import java.util.IdentityHashMap;
import java.util.function.Function;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationLocal;
import app.packed.application.ApplicationTemplate;
import app.packed.application.ApplicationTemplate.Installer;
import app.packed.assembly.Assembly;
import app.packed.build.BuildGoal;
import app.packed.container.ContainerHandle;
import app.packed.container.Wirelet;
import app.packed.lifetime.LifecycleKind;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public final class PackedApplicationInstaller<A> implements ApplicationTemplate.Installer<A> {

    public ApplicationSetup application;

    public BuildApplicationRepository bar;

    public final PackedContainerInstaller container;

    // I would like to time stuff. But I have no idea on how to do it reliable with all the laziness
    long creationNanos;

    public final BuildGoal goal;

    /** The function used to create a new application handle, when the application has been installed. */
    private Function<? super ApplicationTemplate.Installer<A>, ?> handleFactory = ApplicationHandle::new;

    public MethodHandle launcher;

    public final LifecycleKind lk;

    /** Application locals that the application is initialized with. */
    private final IdentityHashMap<PackedApplicationLocal<?>, Object> locals = new IdentityHashMap<>();

    public boolean optionBuildApplicationLazy;

    public boolean optionBuildReusableImage;

    final PackedBuildProcess pbp;

    final PackedApplicationTemplate<?> template;

    PackedApplicationInstaller(PackedApplicationTemplate<?> template, BuildGoal goal) {
        this.template = template;
        this.goal = goal;
        this.lk = template.containerTemplate().lifecycleKind();
        this.container = new PackedContainerInstaller(template.containerTemplate(), this, null, null);
        this.pbp = new PackedBuildProcess(this);
    }

    public ApplicationSetup buildApplication(Assembly assembly) {
        requireNonNull(assembly, "assembly is null");

        // Prepare the ScopedValue.Carrier that sets the for setting the build process for the build thread
        Carrier c = ScopedValue.where(PackedBuildProcess.VAR, pbp);

        try {
            return c.call(() -> container.invokeAssemblyBuild(assembly)).application;
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        } finally {
            pbp.thread = null;
        }
    }

    /**
     * Checks that the installer has not already been used to create a new bean.
     * <p>
     * There is technically no reason to not allow this installer to be reused. But we will need to make a copy of the
     * locals if we want to support this.
     */
    private void checkNotInstalledYet() {
        if (application != null) {
            throw new IllegalStateException("A bean has already been created from this installer");
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <H extends ApplicationHandle<?, A>> H install(Assembly assembly, Function<? super Installer<A>, H> handleFactory, Wirelet... wirelets) {
        checkNotInstalledYet();
        requireNonNull(assembly, "assembly is null");
        this.handleFactory = requireNonNull(handleFactory, "handleFactory is null");

        // Prepare the ScopedValue.Carrier that sets the for setting the build process for the build thread
        Carrier c = ScopedValue.where(PackedBuildProcess.VAR, pbp);

        try {
            // will set this.application
            c.call(() -> container.invokeAssemblyBuild(assembly));
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        } finally {
            pbp.thread = null;
        }
        return (H) requireNonNull(application.handle);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ApplicationSetup newApplication(AssemblySetup assembly) {
        ApplicationSetup as = this.application = new ApplicationSetup(this);

        as.handle = (ApplicationHandle<?, ?>) handleFactory.apply(this);
        locals.forEach((l, v) -> as.locals().set((PackedApplicationLocal) l, as, v));

        // Initialize the root container
        as.container = container.newContainer(as, assembly, ContainerHandle::new);
        return as;
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedApplicationInstaller<A> setLocal(ApplicationLocal<T> local, T value) {
        checkNotInstalledYet();
        locals.put((PackedApplicationLocal<?>) local, value);
        return this;
    }
}
