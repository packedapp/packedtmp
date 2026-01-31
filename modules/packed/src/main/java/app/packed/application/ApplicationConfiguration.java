/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.application;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import app.packed.assembly.Assembly;
import app.packed.build.BuildException;
import app.packed.build.BuildProcess;
import app.packed.component.ComponentConfiguration;
import app.packed.lifecycle.sandbox.StopOption;

/**
 * The configuration of an application.
 */
// By default it is configuration everywhere..
// Maybe have a freeze()/protect() operation/

// isConfigurable?? Models

// Root assembly defines this, and is sharable between all assemblies

// Per assembly, requires that we can create new application configurations.
// when needed
public non-sealed class ApplicationConfiguration extends ComponentConfiguration implements ApplicationBuildLocal.Accessor {

    List<Class<? extends Assembly>> allowedAssemblies = List.of();
    // matcher

    /** The application's handle. */
    private final ApplicationHandle<?, ?> handle;

    /**
     * Create a new application configuration.
     *
     * @param handle
     *            the application's handle
     */
    public ApplicationConfiguration(ApplicationHandle<?, ?> handle) {
        this.handle = requireNonNull(handle);
    }

    /**
     * Checks that the is updatable
     *
     * @throws BuildException
     *             if the operation
     */
    protected final void checkUpdatable() {
        checkIsConfigurable();
        Optional<Class<? extends Assembly>> current = BuildProcess.current().currentAssembly();

        if (current.isEmpty()) {
            return;
        }
        Class<? extends Assembly> cl = current.get();
        if (allowedAssemblies.isEmpty()) {
            return;
        }
        for (Class<? extends Assembly> c : allowedAssemblies) {
            if (c.isAssignableFrom(cl)) {
                return;
            }
        }
        throw new BuildException("This operation can only be called from assemblies of type " + allowedAssemblies + ", current assembly = " + cl);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationConfiguration tag(String... tags) {
        checkUpdatable();
        handle.componentTag(tags);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return handle.componentTags();
    }

    /** {@inheritDoc} */
    @Override
    protected final ApplicationHandle<?, ?> handle() {
        return handle;
    }

    /**
     * If no name is set, uses the name of the root container
     *
     * @param name
     */
    public void named(String name) {
        checkUpdatable();
        IO.println("Setting name");
    }

    @SafeVarargs
    public final void restrictUpdatesTo(Class<? extends Assembly>... assemblies) {
        checkUpdatable();
        this.allowedAssemblies = List.of(assemblies);
    }

    // Hmm, Bad naming with updates, It is so common
    public final void restrictUpdatesToThisAssembly() {
        allowedAssemblies = List.of(BuildProcess.current().currentAssembly().get());
    }

    /**
     * Installs a shutdown hook similar to {@link #shutdownHook(StopOption...)} exact that the thread passed to
     * {@link Runtime#addShutdownHook(Thread)} can be customized.
     *
     * @param threadFactory
     *            a factory that is used to create the shutdown hook thread
     * @param options
     *            stop options
     * @return a shutdown hook wirelet
     * @see Runtime#addShutdownHook(Thread)
     * @throws UnsupportedOperationException
     *             if the application is unmanaged
     */
    // Why shouldn't I be able to use this on runtime???
    public void shutdownHook(Function<Runnable, Thread> threadFactory, StopOption... options) {}

    /**
     * Returns a wirelet that will install a shutdown hook for an application.
     * <p>
     * As shutting down the root will automatically shutdown all of its child applications. Attempting to specify a shutdown
     * hook wirelet when launching a non-root application will fail with an exception.
     * <p>
     * Attempting to use more than one shutdown hook wirelet on an application will fail
     *
     * Attempting to use it anywhere else than on a root application will fail
     *
     * Attempting to use this wirelet for an application in an {@link LifetimeKind#UNMANAGED} will fail
     *
     * @return a shutdown hook wirelet
     * @see #shutdownHook(Function, app.packed.lifetime.sandbox.StopOption...)
     * @see Runtime#addShutdownHook(Thread)
     * @throws UnsupportedOperationException
     *             if the application is unmanaged
     */
    // Ogsaa skrive noget om hvad der sker hvis vi stopper
    // Multiple shutdown hooks? I don't think we should do any checks.
    // Problem is if we have ApplicationConfiguration.installShutdownHook()
    // And CliApp uses a wirelet at the same time
    // Maybe only ApplicationConfiguration,
    public void shutdownHook(StopOption... options) {}
}
