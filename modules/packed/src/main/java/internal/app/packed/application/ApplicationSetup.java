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
import java.util.ArrayList;

import app.packed.application.ApplicationMirror;
import app.packed.application.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.framework.Nullable;
import app.packed.lifetime.sandbox.OldLifetimeKind;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.pool.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.lifetime.sandbox.PackedManagedLifetime;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Internal configuration of an application. */
public final class ApplicationSetup {

    /** A MethodHandle for invoking {@link ApplicationMirror#initialize(ApplicationSetup)}. */
    private static final MethodHandle MH_APPLICATION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationMirror.class,
            "initialize", void.class, ApplicationSetup.class);

    /** A list of actions that should be run doing the code generating phase. */
    private final ArrayList<Runnable> codegenActions = new ArrayList<>();

    /** The root container of the application (created in the constructor of this class). */
    public final ContainerSetup container;

    /** The driver responsible for building the application. */
    public final PackedApplicationDriver<?> driver;

    /** Entry points in the application, is null if there are none. */
    @Nullable
    public EntryPointSetup entryPoints;

    /** The build goal. */
    public final BuildGoal goal;

    /** Whether or not we are in the code generating phase. */
    private boolean isInCodegenPhase;

    /**
     * A launcher for launching the application. Is not created for {@link BuildGoal#VERIFY} or
     * {@link BuildGoal#NEW_MIRROR}.
     */
    @Nullable
    PackedApplicationLauncher launcher;

    /** The index of the application's runtime in the constant pool, or -1 if the application has no runtime, */
    @Nullable
    public
    final DynamicAccessor runtimeAccessor;

    public final CodegenHelper codegenHelper = new CodegenHelper();

    /**
     * Create a new application setup
     */
    public ApplicationSetup(PackedApplicationDriver<?> driver, BuildGoal goal, AssemblySetup assembly, Wirelet[] wirelets) {
        this.driver = requireNonNull(driver);
        this.goal = requireNonNull(goal);

        // Create the root container of the application
        this.container = new ContainerSetup(this, assembly, null, wirelets);

        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool
        this.runtimeAccessor = driver.lifetimeKind() == OldLifetimeKind.MANAGED ? container.lifetime.pool.reserve(PackedManagedLifetime.class) : null;
    }

    public void addCodegenAction(Runnable action) {
        requireNonNull(action, "action is null");
        if (isInCodegenPhase) {
            throw new IllegalStateException("This method must be called before the code generating phase is started");
        }
        codegenActions.add(action);
    }

    /**
     * Checks that we are in the code generating phase.
     * 
     * @throws IllegalStateException
     *             if not in the code generating phase
     */
    public void checkInCodegenPhase() {
        // Should we check that launcher is null? (codegen phase done)
        if (!isInCodegenPhase) {
            // Uncommented while transitioning to new Codegen
          //  throw new IllegalStateException();
        }
    }

    /** This method is responsible for generating code to run the application. */
    public void codegen() {
        isInCodegenPhase = true;

        // Vi bliver noedt til at have noget med order de bliver genereret i...
        /// Fx .overrideServiceDelayed der skal vi jo resolve Object[] efter de er genereret...

        // Saa reverse extension order tror jeg
        // Nah burde stadig virke

        // Do we need callbacks on extensions???

        // For each lifetime create lifetime operations
        //// And install them in the lifetime

        // Packed relies on lazily created codegeneration

        // If this fails it is always a bug in either Packed or one of its extensions.
        // It is never a user error.

        // Codegeneration for the root lifetime
        container.lifetime.codegen();

        for (Runnable r : codegenActions) {
            r.run();
        }
        launcher = new PackedApplicationLauncher(this);
        isInCodegenPhase = false;
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
}
