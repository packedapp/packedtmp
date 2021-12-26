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
package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Set;

import app.packed.application.ApplicationDescriptor;
import app.packed.application.ApplicationDescriptor.ApplicationBuildType;
import app.packed.application.ApplicationMirror;
import app.packed.application.ExecutionWirelets;
import app.packed.base.Nullable;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.lifecycle.RunState;
import packed.internal.component.RealmSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionRealmSetup;
import packed.internal.container.PackedContainerDriver;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolAccessor;

/** Build-time configuration of an application. */
public final class ApplicationSetup {

    /** The root container of the application (created in the constructor of this class). */
    public final ContainerSetup container;

    public final ApplicationDescriptor descriptor;

    /** The driver responsible for building the application. */
    public final PackedApplicationDriver<?> driver;

    /** Entry points in the application, is null if there are none. */
    @Nullable
    public final EntryPointSetup entryPoints = new EntryPointSetup();

    /** All extensions used in the application. */
    public final LinkedHashMap<Class<? extends Extension>, ExtensionRealmSetup> extensions = new LinkedHashMap<>();

    /**
     * The launch mode of the application. May be updated via usage of {@link ExecutionWirelets#launchMode(RunState)} at
     * build-time. If used from an image {@link ApplicationInitializationContext#launchMode} is updated instead.
     */
    final RunState launchMode;

    /** The index of the application's runtime in the constant pool, or -1 if the application has no runtime, */
    @Nullable
    final PoolAccessor runtimeAccessor;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    public ApplicationSetup(PackedApplicationDriver<?> driver, ApplicationBuildType buildKind, RealmSetup realm, Wirelet[] wirelets) {
        this.driver = driver;
        this.launchMode = requireNonNull(driver.launchMode());

        this.descriptor = new PackedApplicationDescriptor(buildKind);

        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool

        this.container = new ContainerSetup(this, realm, new LifetimeSetup(null), /* fixme */ PackedContainerDriver.DEFAULT, null, wirelets);
        this.runtimeAccessor = driver.isExecutable() ? container.lifetime.pool.reserve(PackedApplicationRuntime.class) : null;
    }

    /** {@return a build-time application mirror that can be exposed to end-users} */
    public ApplicationMirror mirror() {
        return new BuildTimeApplicationMirror(this);
    }

    /** An application mirror adaptor. */
    private record BuildTimeApplicationMirror(ApplicationSetup application) implements ApplicationMirror {

        @Override
        public ContainerMirror container() {
            return application.container.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Set<Class<? extends Extension>> disabledExtensions() {
            // TODO add additional dsiabled extensions
            return application.driver.bannedExtensions();
        }

        /** {@inheritDoc} */
        @Override
        public Module module() {
            return application.container.realm.realmType().getModule();
        }

        @Override
        public ApplicationDescriptor descriptor() {
            return application.descriptor;
        }

        /** {@inheritDoc} */
        @Override
        public <T extends ExtensionMirror> T use(Class<T> type) {
            // TODO fix, is application extension mirror
            return container().useExtension(type);
        }
    }
}
