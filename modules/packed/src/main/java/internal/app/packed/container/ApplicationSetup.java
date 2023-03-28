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
import java.util.HashMap;
import java.util.function.Supplier;

import app.packed.application.ApplicationMirror;
import app.packed.application.BuildGoal;
import app.packed.util.Nullable;
import internal.app.packed.bean.PackedBeanLocal;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;
import sandbox.extension.domain.ExtensionDomain;

/**
 * Internal configuration of an application.
 * <p>
 * This class is placed in {@code internal.app.packed.container} because it is so tightly integrated with containers
 * that it made sense to put it here as well.
 */
public final class ApplicationSetup {

    /** A MethodHandle for invoking {@link ApplicationMirror#initialize(ApplicationSetup)}. */
    private static final MethodHandle MH_APPLICATION_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), ApplicationMirror.class, "initialize",
            void.class, ApplicationSetup.class);

    /** This map maintains every {@link app.packed.extension.BeanLocal} for the application. */
    public final HashMap<PackedBeanLocal.PairKey, Object> beanLocals = new HashMap<>();

    /** Any (statically defined) children this application has. */
    final ArrayList<FutureApplicationSetup> children = new ArrayList<>();

    /** A list of actions that will be executed doing the code generating phase. Or null if code generation is disabled. */
    @Nullable
    private ArrayList<Runnable> codegenActions;

    /** The root container of the application. */
    public final ContainerSetup container;

    // Maybe move to container?? Or maybe a DomainManager class? IDK
    public final HashMap<PackedDomainTemplate<?>, ExtensionDomain<?>> domains = new HashMap<>();

    /**
     * All extensions used in an application has a unique instance id attached. This is used in case we have multiple
     * extension with the same canonical name. Which may happen if different containers uses the "same" extension but
     * defined in different class loaders. We then compare the extension id of the extensions as a last resort when sorting
     * them.
     */
    int extensionIdCounter;

    /** The build goal. */
    public final BuildGoal goal;

    /** Supplies mirrors for the application. */
    private final Supplier<? extends ApplicationMirror> mirrorSupplier;

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
    public ApplicationSetup(PackedContainerBuilder containerBuilder, AssemblySetup assembly) {
        this.goal = containerBuilder.goal();
        this.codegenActions = goal.isCodeGenerating() ? new ArrayList<>() : null;
        this.mirrorSupplier = containerBuilder.applicationMirrorSupplier;
        this.container = containerBuilder.newContainer(this, assembly);
    }

    /**
     * Registers an action that will be called in the code generation phase. The action ignored for build goals
     * {@link BuildGoal#MIRROR} and {@link BuildGoal#VERIFY}.
     *
     * @param action
     *            the action to run
     * @throws IllegalStateException
     *             if already in the code generating phase or if the build has finished
     */
    public void addCodegenAction(Runnable action) {
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

    /** The application has been successfully assembled. Now generate any required code. */
    public void close() {
        // Only generate code if needed
        if (codegenActions != null) { // not mirrorOf or verify
            phase = ApplicationBuildPhase.CODEGEN;

            // Run through all code generating actions
            for (Runnable r : codegenActions) {
                r.run();
            }

            // clear out the list of actions.
            codegenActions = null;
        }

        // The application was build successfully
        phase = ApplicationBuildPhase.COMPLETED;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    public ApplicationMirror mirror() {
        ApplicationMirror mirror = ClassUtil.newMirror(ApplicationMirror.class, ApplicationMirror::new, mirrorSupplier);

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
