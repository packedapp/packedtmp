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
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/** Internal configuration of an application. */
public final class ApplicationSetup {

    /** A MethodHandle for invoking {@link ApplicationMirror#initialize(ApplicationSetup)}. */
    private static final MethodHandle MH_APPLICATION_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), ApplicationMirror.class,
            "initialize", void.class, ApplicationSetup.class);

    /** Responsible for code generation, is null for {@link BuildGoal#NEW_MIRROR} and {@link BuildGoal#VERIFY}. */
    @Nullable
    public final ApplicationCodeGenerator codeGenerator;

    /** The root container of the application. */
    public final ContainerSetup container;

    /** The driver used to create the application. */
    public final ApplicationDriver<?> driver;

    /** Entry points in the application, is null if there are none. */
    @Nullable // Maybe this a lifetime thing?
    public EntryPointSetup entryPoints;

    /** The build goal. */
    public final BuildGoal goal;

    /** The current phase of the build process. */
    private ApplicationBuildPhase phase = ApplicationBuildPhase.ASSEMBLE;

    /**
     * Create a new application.
     * 
     * @param driver
     *            the application driver
     * @param goal
     *            the build goal
     * @param assembly
     *            the assembly that defines the application
     * @param wirelets
     *            optional wirelets
     */
    public ApplicationSetup(ApplicationDriver<?> driver, BuildGoal goal, AssemblySetup assembly, Wirelet[] wirelets) {
        this.driver = requireNonNull(driver);
        this.goal = requireNonNull(goal);
        this.codeGenerator = goal.isLaunchable() ? new ApplicationCodeGenerator(this) : null;
        this.container = new ContainerSetup(this, assembly, null, wirelets);
    }

    /**
     * Registers an action that will be called in the code generation phase. The action is executed for goals
     * {@link BuildGoal#NEW_MIRROR} or {@link BuildGoal#VERIFY}.
     * 
     * @param action
     *            the action to run
     * @throws IllegalStateException
     *             if already in the code generating phase or if the build has finished
     */
    public void addCodegenAction(Runnable action) {
        requireNonNull(action, "action is null");
        if (phase != ApplicationBuildPhase.ASSEMBLE) {
            throw new IllegalStateException("This method must be called before the code generating phase is started");
        }
        // Only add the action if code generation is enabled
        if (codeGenerator != null) {
            codeGenerator.actions.add(action);
        }
    }

    /**
     * Checks that we are in the code generating phase.
     * 
     * @throws IllegalStateException
     *             if not in the code generating phase
     */
    public void checkInCodegenPhase() {
        if (phase != ApplicationBuildPhase.CODEGEN) {
            throw new IllegalStateException("In state " + phase);
        }
    }

    public void finish() {
        if (codeGenerator != null) {
            phase = ApplicationBuildPhase.CODEGEN;
            codeGenerator.finish();
        }
        phase = ApplicationBuildPhase.COMPLETED;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    public ApplicationMirror mirror() {
        ApplicationMirror mirror = ClassUtil.mirrorHelper(ApplicationMirror.class, ApplicationMirror::new, driver.mirrorSupplier());

        // Initialize ApplicationMirror by calling ApplicationMirror#initialize(ApplicationSetup)
        try {
            MH_APPLICATION_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    /** The build phase of the application. */
    private enum ApplicationBuildPhase {
        ASSEMBLE, CODEGEN, COMPLETED;
    }
}
