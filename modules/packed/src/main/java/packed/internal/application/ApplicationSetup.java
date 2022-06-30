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

import app.packed.application.ApplicationInfo;
import app.packed.application.ApplicationInfo.ApplicationBuildType;
import app.packed.application.ApplicationMirror;
import app.packed.application.ExecutionWirelets;
import app.packed.base.Nullable;
import app.packed.container.ContainerMirror;
import app.packed.container.ExtensionMirror;
import app.packed.container.Wirelet;
import app.packed.lifecycle.RunState;
import app.packed.lifetime.LifetimeMirror;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedContainerDriver;
import packed.internal.container.UserRealmSetup;
import packed.internal.inject.ApplicationInjectionManager;
import packed.internal.lifetime.PoolEntryHandle;

/** Build-time configuration of an application. */
public final class ApplicationSetup {

    /** The root container of the application (created in the constructor of this class). */
    public final ContainerSetup container;

    /** The tree this service manager is a part of. */
    public final ApplicationInjectionManager injectionManager = new ApplicationInjectionManager();
    
    public final ApplicationInfo descriptor;

    /** The driver responsible for building the application. */
    public final PackedApplicationDriver<?> driver;

    /** Entry points in the application, is null if there are none. */
    @Nullable
    public EntryPointSetup entryPoints;

    /**
     * The launch mode of the application. May be updated via usage of {@link ExecutionWirelets#launchMode(RunState)} at
     * build-time. If used from an image {@link ApplicationInitializationContext#launchMode} is updated instead.
     */
    final RunState launchMode;

    /** The index of the application's runtime in the constant pool, or -1 if the application has no runtime, */
    @Nullable
    final PoolEntryHandle runtimeAccessor;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    public ApplicationSetup(PackedApplicationDriver<?> driver, ApplicationBuildType buildKind, UserRealmSetup realm, Wirelet[] wirelets) {
        this.driver = driver;
        this.launchMode = requireNonNull(driver.launchMode());
        this.descriptor = new PackedApplicationDescriptor(buildKind);

        // Create the root container of the application
        this.container = new ContainerSetup(this, realm, new PackedContainerDriver(null), null, wirelets);

        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool
        this.runtimeAccessor = driver.isExecutable() ? container.lifetime.pool.reserve(PackedApplicationRuntime.class) : null;
    }

    /** {@return an application mirror that can be exposed to end-users.} */
    public ApplicationMirror mirror() {
        return new BuildTimeApplicationMirror(this);
    }

    /** An application mirror adaptor. */
    private record BuildTimeApplicationMirror(ApplicationSetup application) implements ApplicationMirror {

        /** {@inheritDoc} */
        @Override
        public ContainerMirror container() {
            return application.container.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationInfo descriptor() {
            return application.descriptor;
        }

        /** {@inheritDoc} */
        @Override
        public <T extends ExtensionMirror<?>> T useExtension(Class<T> type) {
            return container().useExtension(type);
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeMirror lifetime() {
            return application.container.lifetime.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            return obj instanceof BuildTimeApplicationMirror m && m.application == application;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return application.hashCode();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Application";
        }
    }
}
