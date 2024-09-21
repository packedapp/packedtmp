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
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.packed.application.ApplicationBuildHook;
import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationLocal;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.Assembly;
import app.packed.build.BuildGoal;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerLocal;
import app.packed.extension.Extension;
import app.packed.namespace.NamespaceHandle;
import app.packed.util.Nullable;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.component.Mirrorable;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.namespace.NamespaceSetup.NamespaceKey;
import internal.app.packed.util.LookupUtil;

/**
 * Internal configuration of an application.
 * <p>
 * This class is placed in {@code internal.app.packed.container} because it is so tightly integrated with containers
 * that it made sense to put it here as well.
 */
public final class ApplicationSetup implements ComponentSetup, BuildLocalSource, Mirrorable<ApplicationMirror> {

    /** A handle that can access ApplicationConfiguration#application. */
    private static final VarHandle VH_APPLICATION_CONFIGURATION_TO_HANDLE = LookupUtil.findVarHandle(MethodHandles.lookup(), ApplicationConfiguration.class,
            "handle", ApplicationHandle.class);

    /** A handle that can access {@link BeanHandleHandle#bean}. */
    private static final VarHandle VH_APPLICATION_HANDLE_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), ApplicationHandle.class, "application",
            ApplicationSetup.class);

    /** A handle that can access ApplicationMirror#application. */
    private static final VarHandle VH_APPLICATION_MIRROR_TO_HANDLE = LookupUtil.findVarHandle(MethodHandles.lookup(), ApplicationMirror.class, "handle",
            ApplicationHandle.class);

    /** Any (statically defined) children this application has. */
    public final ArrayList<BuildApplicationRepository> subChildren = new ArrayList<>();

    /**
     * A list of actions that will be executed doing the code generating phase. Or null if code generation is disabled or
     * has already been performed.
     */
    @Nullable
    private ArrayList<Runnable> codegenActions;

    /** The root container of the application, is initialized from {@link PackedApplicationInstaller}. */
    public ContainerSetup container;

    /** The deployment the application is part of. */
    public final DeploymentSetup deployment;

    /**
     * All extensions used in an application has a unique instance id attached. This is used in case we have multiple
     * extension with the same canonical name. Which may happen if different containers uses the "same" extension but
     * defined in different class loaders. We then compare the extension id of the extensions as a last resort when sorting
     * them.
     */
    // We actually have a unique name now, so maybe we can skip this counter
    public int extensionIdCounter;

    /**
     * All extensions in the application, uniquely named.
     * <p>
     * The only time where we might see collisions is if we load 2 extensions with same name, but with different class
     * loaders.
     */
    public final Map<String, Class<? extends Extension<?>>> extensions = new HashMap<>();

    ApplicationHandle<?, ?> handle;

    /** All hooks applied on the application. */
    public final ArrayList<ApplicationBuildHook> hooks = new ArrayList<>();

    /** This map maintains all locals for the entire application. */
    private final BuildLocalMap locals = new BuildLocalMap();

    // Maybe move to container?? Or maybe a DomainManager class? IDK
    public final HashMap<NamespaceKey, NamespaceHandle<?, ?>> namespaces = new HashMap<>();

    /** The current phase of the application's build process. */
    private ApplicationBuildPhase phase = ApplicationBuildPhase.ASSEMBLE;

    public final PackedApplicationTemplate<?> template;

    public final BuildGoal goal;

    public MethodHandle launch;

    /**
     * Create a new application.
     *
     * @param installer
     *            the application installer
     */
    ApplicationSetup(PackedApplicationInstaller<?> installer) {
        this.template = installer.template;
        this.deployment = new DeploymentSetup(this, installer);
        this.codegenActions = deployment.goal.isCodeGenerating() ? new ArrayList<>() : null;
        this.goal = installer.goal;
        this.launch = installer.bar == null ? null : requireNonNull(installer.bar.guest);
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

    public ApplicationSetup checkWriteToLocals() {
        return this;
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

    @Override
    public ComponentPath componentPath() {
        return ComponentKind.APPLICATION.pathNew(container.node.name);
    }

    public ContainerSetup container() {
        ContainerSetup c = container;
        if (c != null) {
            return c;
        }
        throw new IllegalStateException();
    }

    ApplicationHandle<?, ?> handle() {
        return requireNonNull(handle);
    }

    /** {@inheritDoc} */
    @Override
    public BuildLocalMap locals() {
        return locals;
    }

    /** {@return a new application mirror.} */
    @Override
    public ApplicationMirror mirror() {
        return handle().mirror();
    }

    public static ApplicationSetup crack(ApplicationConfiguration configuration) {
        ApplicationHandle<?, ?> handle = (ApplicationHandle<?, ?>) VH_APPLICATION_CONFIGURATION_TO_HANDLE.get(configuration);
        return crack(handle);
    }

    public static ApplicationSetup crack(ApplicationHandle<?, ?> handle) {
        return (ApplicationSetup) VH_APPLICATION_HANDLE_TO_SETUP.get(handle);
    }

    public static ApplicationSetup crack(ApplicationLocal.Accessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return switch (accessor) {
        case ApplicationConfiguration a -> ApplicationSetup.crack(a);
        case ApplicationMirror a -> ApplicationSetup.crack(a);
        case Assembly b -> throw new UnsupportedOperationException();
        case ContainerLocal.Accessor b -> ContainerSetup.crack(b).application;
        };
    }

    public static ApplicationSetup crack(ApplicationMirror mirror) {
        ApplicationHandle<?, ?> handle = (ApplicationHandle<?, ?>) VH_APPLICATION_MIRROR_TO_HANDLE.get(mirror);
        return crack(handle);
    }

    /** The build phase of the application. */
    private enum ApplicationBuildPhase {
        ASSEMBLE, CODEGEN, COMPLETED;
    }
}
