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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.application.ApplicationMirror;
import app.packed.application.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.framework.Nullable;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.lifetime.sandbox.PackedManagedLifetime;
import internal.app.packed.lifetime.sandbox2.OldLifetimeKind;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Internal configuration of an application. */
public final class ApplicationSetup {

    /** A MethodHandle for invoking {@link ApplicationMirror#initialize(ApplicationSetup)}. */
    private static final MethodHandle MH_APPLICATION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationMirror.class,
            "initialize", void.class, ApplicationSetup.class);

    /** Responsible for everything to do with code generation, is null if the application is not launchable. */
    @Nullable
    final ApplicationCodegen codegen;

    /** The root container of the application (created in the constructor of this class). */
    public final ContainerSetup container;

    /** The driver of the application. */
    public final PackedApplicationDriver<?> driver;

    /** Entry points in the application, is null if there are none. */
    @Nullable
    public EntryPointSetup entryPoints;

    /** The build goal. */
    public final BuildGoal goal;

    /**
     * A launcher for launching the application. Is not created for {@link BuildGoal#VERIFY} or
     * {@link BuildGoal#NEW_MIRROR}.
     */
    @Nullable
    PackedApplicationLauncher launcher;

    /** The current phase of the build process. */
    private ApplicationBuildPhase phase = ApplicationBuildPhase.ASSEMBLE;

    /** The index of the application's runtime in the constant pool, or -1 if the application has no runtime, */
    @Nullable
    public final DynamicAccessor runtimeAccessor;

    /**
     * Create a new application setup
     */
    public ApplicationSetup(PackedApplicationDriver<?> driver, BuildGoal goal, AssemblySetup assembly, Wirelet[] wirelets) {
        this.driver = requireNonNull(driver);
        this.goal = requireNonNull(goal);

        // Only generate code if the application can be launched (not a mirror or verify)
        this.codegen = goal.isLaunchable() ? new ApplicationCodegen() : null;

        // Create the root container of the application
        this.container = new ContainerSetup(this, assembly, null, wirelets);

        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool
        this.runtimeAccessor = driver.lifetimeKind() == OldLifetimeKind.MANAGED ? container.lifetime.pool.reserve(PackedManagedLifetime.class) : null;
    }

    public void addCodegenAction(Runnable action) {
        requireNonNull(action, "action is null");
        if (phase.ordinal() >= ApplicationBuildPhase.CODEGEN.ordinal()) {
            throw new IllegalStateException("This method must be called before any code generating phase is started");
        }
        // Ignore the action if we are not going to do any code generation.
        if (codegen != null) {
            codegen.actions.add(action);
        }
    }

    /**
     * Checks that we are in the code generating phase.
     * 
     * @throws IllegalStateException
     *             if not in the code generating phase
     */
    public void checkInCodegenPhase() {
        // Should we check that launcher is null? (codegen phase done)
        if (phase != ApplicationBuildPhase.CODEGEN) {
            // Uncommented while transitioning to new Codegen
            // throw new IllegalStateException();
        }
    }

    public void finish() {
        if (codegen != null) {
            phase = ApplicationBuildPhase.CODEGEN;
            CodegenEvent ce = new CodegenEvent();
            ce.begin();

            container.lifetime.codegen();
            for (Runnable r : codegen.actions) {
                r.run();
            }
            ce.commit();

            launcher = new PackedApplicationLauncher(this);
        }
        phase = ApplicationBuildPhase.COMPLETED;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    public ApplicationMirror mirror() {
        ApplicationMirror mirror = ClassUtil.mirrorHelper(ApplicationMirror.class, ApplicationMirror::new, driver.mirrorSupplier);

        // Initialize ApplicationMirror by calling ApplicationMirror#initialize(ApplicationSetup)
        try {
            MH_APPLICATION_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public enum ApplicationBuildPhase {
        ASSEMBLE, CLOSE, CODEGEN, COMPLETED;
    }
}
