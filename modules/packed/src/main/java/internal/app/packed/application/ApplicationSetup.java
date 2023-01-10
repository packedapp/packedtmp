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
import internal.app.packed.lifetime.sandbox2.OldLifetimeKind;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/** Internal configuration of an application. */
public final class ApplicationSetup {

    /** A MethodHandle for invoking {@link ApplicationMirror#initialize(ApplicationSetup)}. */
    private static final MethodHandle MH_APPLICATION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationMirror.class,
            "initialize", void.class, ApplicationSetup.class);

    /** The root container of the application. */
    public final ContainerSetup container;

    /** The driver used to create the application. */
    public final PackedApplicationDriver<?> driver;

    /** Entry points in the application, is null if there are none. */
    @Nullable // Maybe this a lifetime thingy?
    public EntryPointSetup entryPoints;

    /** The build goal. */
    public final BuildGoal goal;

    /** Responsible for everything to do with code generation, is null if the application cannot be launched. */
    @Nullable
    public final ApplicationLauncherSetup launcher;

    /** The current phase of the build process. */
    private ApplicationBuildPhase phase = ApplicationBuildPhase.ASSEMBLE;

    public final OldLifetimeKind lifetimeKind;

    /**
     * Create a new application.
     * 
     * @param driver
     *            the driver of the application
     * @param goal
     *            the build goal
     * @param assembly
     *            the assembly that defines the application
     * @param wirelets
     *            optional wirelets
     */
    public ApplicationSetup(PackedApplicationDriver<?> driver, BuildGoal goal, AssemblySetup assembly, Wirelet[] wirelets) {
        this.driver = requireNonNull(driver);
        this.lifetimeKind = driver.lifetimeKind();
        this.goal = requireNonNull(goal);
        this.container = new ContainerSetup(this, assembly, null, wirelets); // the root container of the application
        this.launcher = goal.isLaunchable() ? new ApplicationLauncherSetup(this) : null;
    }

    public void addCodegenAction(Runnable action) {
        requireNonNull(action, "action is null");
        if (phase.ordinal() >= ApplicationBuildPhase.CODEGEN.ordinal()) {
            throw new IllegalStateException("This method must be called before any code generating phase is started");
        }
        // Ignore the action if we are not going to do any code generation.
        if (launcher != null) {
            launcher.actions.add(action);
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
        if (launcher != null) {
            phase = ApplicationBuildPhase.CODEGEN;
            launcher.finish();
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

    enum ApplicationBuildPhase {
        ASSEMBLE, CLOSE, CODEGEN, COMPLETED;
    }
}
