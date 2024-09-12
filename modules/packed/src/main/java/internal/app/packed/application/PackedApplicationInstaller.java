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
import java.util.IdentityHashMap;
import java.util.function.Supplier;

import app.packed.application.ApplicationLocal;
import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationTemplate;
import app.packed.application.ApplicationTemplate.Configurator;
import app.packed.application.ApplicationTemplate.Installer;
import app.packed.assembly.Assembly;
import app.packed.bean.BeanConfiguration;
import app.packed.build.BuildGoal;
import app.packed.lifetime.LifecycleKind;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public final class PackedApplicationInstaller implements ApplicationTemplate.Installer {

    public final PackedContainerInstaller container;

    // I would like to time stuff. But I have no idea on how to do it reliable with all the laziness
    long creationNanos;

    public final BuildGoal goal;

    public final LifecycleKind lk;

    /** Application locals that the application is initialized with. */
    public final IdentityHashMap<PackedApplicationLocal<?>, Object> locals = new IdentityHashMap<>();

    /** A supplier for creating application mirrors. */
    public Supplier<? extends ApplicationMirror> mirrorSupplier;

    public boolean optionBuildApplicationLazy;
    public boolean optionBuildReusableImage;

    final PackedBuildProcess pbp;

    final ApplicationTemplate template;

    ApplicationSetup application;

    PackedApplicationInstaller(PackedApplicationTemplate template, BuildGoal goal) {
        this.template = template;
        this.goal = goal;
        this.lk = template.containerTemplate().lifecycleKind();
        this.mirrorSupplier = template.supplier();
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

    public FutureApplicationSetup buildLazy(Assembly assembly) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Configurator hostedBy(BeanConfiguration bean) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ApplicationSetup newApplication(AssemblySetup assembly) {
        ApplicationSetup as = this.application = new ApplicationSetup(this, assembly);

        // Transfer any locals that have been set in the template or installer
        // Problemet er lidt at alt i containerSetup er konfigureret foerend vi naar her
        // Vi kan ikke skrive dem i application setup constructeren, fordi vi skal bruge noeglen (application setup)
        // I CHM local mappet.
        // Tror vi maa lave ApplicationSetup.container non-final
        locals.forEach((l, v) -> as.locals().set((PackedApplicationLocal) l, as, v));

        return as;
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
    @Override
    public <T> Configurator setLocal(ApplicationLocal<T> local, T value) {
        checkNotInstalledYet();
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Installer specializeMirror(Supplier<? extends ApplicationMirror> supplier) {
        checkNotInstalledYet();
        this.mirrorSupplier = supplier;
        return this;
    }
}
