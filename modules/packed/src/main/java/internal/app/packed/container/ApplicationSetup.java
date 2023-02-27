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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.function.Supplier;

import app.packed.application.ApplicationMirror;
import app.packed.application.BuildGoal;
import app.packed.util.Nullable;
import internal.app.packed.jfr.CodegenEvent;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/**
 * Internal configuration of an application.
 * <p>
 * Is {@code internal.app.packed.container} because it is so tightly integrated with containers.
 * It made best to put it here as well.
 */
public final class ApplicationSetup {

    /** A MethodHandle for invoking {@link ApplicationMirror#initialize(ApplicationSetup)}. */
    private static final MethodHandle MH_APPLICATION_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), ApplicationMirror.class, "initialize",
            void.class, ApplicationSetup.class);

    /** A list of actions that will be executed doing the code generating phase. Or null if code generation is disabled. */
    @Nullable
    private ArrayList<Runnable> codegenActions;

    /** The root container of the application. */
    public final ContainerSetup container;

    /**
     * All extensions used in an application has a unique instance id attached. This is used in case we have multiple
     * extension with the same canonical name (from different class loaders). We then compare the extension id of the
     * extensions as a last resort when sorting them.
     */
    public int extensionIdCounter;

    /** The build goal. */
    public final BuildGoal goal;

    final Supplier<? extends ApplicationMirror> mirrorSupplier;

    /** The current phase of the application's build process. */
    private ApplicationBuildPhase phase = ApplicationBuildPhase.ASSEMBLE;

    /**
     * Create a new application.
     *
     * @param containerBuilder
     *            the container builder
     * @param assembly
     *            the assembly that defines the application
     */
    public ApplicationSetup(AbstractContainerBuilder containerBuilder, AssemblySetup assembly) {
        containerBuilder.application = this;

        this.goal = containerBuilder.goal();
        this.codegenActions = goal.isCodeGenerating() ? new ArrayList<>() : null;
        this.mirrorSupplier = containerBuilder.applicationMirrorSupplier;
        this.container = containerBuilder.newContainer(assembly);
    }

    /**
     * Registers an action that will be called in the code generation phase. The action is executed for goals
     * {@link BuildGoal#MIRROR} or {@link BuildGoal#VERIFY}.
     *
     * @param action
     *            the action to run
     * @throws IllegalStateException
     *             if already in the code generating phase or if the build has finished
     */
    public void addCodeGenerator(Runnable action) {
        requireNonNull(action, "action is null");
        if (phase != ApplicationBuildPhase.ASSEMBLE) {
            throw new IllegalStateException("This method must be called in the assemble phase of the application");
        }
        // Only add the action if code generation is enabled
        if (codegenActions != null) {
            codegenActions.add(action);
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
            throw new IllegalStateException("This method can only called in the codegenerating phase, current state = " + phase);
        }
    }

    public void close() {
        // Generate code if needed
        if (codegenActions != null) {
            phase = ApplicationBuildPhase.CODEGEN;

            CodegenEvent ce = new CodegenEvent();
            ce.begin();

            // Run through all code generating actions
            for (Runnable r : codegenActions) {
                r.run();
            }

            ce.commit();
            codegenActions = null;
        }

        // The application was build successfully
        phase = ApplicationBuildPhase.COMPLETED;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    public ApplicationMirror mirror() {
        ApplicationMirror mirror = ClassUtil.mirrorHelper(ApplicationMirror.class, ApplicationMirror::new, mirrorSupplier);

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
