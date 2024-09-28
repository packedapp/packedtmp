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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import app.packed.application.ApplicationBuildHook;
import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationMirror;
import app.packed.build.BuildGoal;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerHandle;
import app.packed.extension.Extension;
import app.packed.namespace.NamespaceHandle;
import app.packed.util.Nullable;
import internal.app.packed.application.deployment.DeploymentSetup;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.component.ComponentTagManager;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.handlers.ApplicationHandlers;
import internal.app.packed.namespace.NamespaceSetup.NamespaceKey;

/** The internal configuration of an application. */
public final class ApplicationSetup implements BuildLocalSource, ComponentSetup {

    /**
     * A list of actions that will be executed doing the code generating phase. Or null if code generation is disabled or
     * has already been performed.
     */
    @Nullable
    private ArrayList<Runnable> codegenActions;

    /** Components tags on components in the application. */
    public final ComponentTagManager componentTagManager = new ComponentTagManager();

    /** The root container of the application, is initialized from {@link PackedApplicationInstaller}. */
    private ContainerSetup container;

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

    public final BuildGoal goal;

    /** The application's handle, instantiated by {@link #newApplication(PackedApplicationInstaller, AssemblySetup)}. */
    private ApplicationHandle<?, ?> handle;

    /** All hooks applied on the application. */
    public final ArrayList<ApplicationBuildHook> hooks = new ArrayList<>();

    public MethodHandle launch;

    /** This map maintains all locals for the entire application. */
    private final BuildLocalMap locals = new BuildLocalMap();

    // Maybe move to container?? Or maybe a DomainManager class? IDK
    public final HashMap<NamespaceKey, NamespaceHandle<?, ?>> namespaces = new HashMap<>();

    /** The current phase of the application's build process. */
    private ApplicationBuildPhase phase = ApplicationBuildPhase.ASSEMBLE;

    /** Any (statically defined) children this application has. */
    public final ArrayList<BuildApplicationRepository> subChildren = new ArrayList<>();

    /** The template used to create the application. */
    public final PackedApplicationTemplate<?> template;

    /**
     * Create a new application.
     *
     * @param installer
     *            the application installer
     */
    private ApplicationSetup(PackedApplicationInstaller<?> installer) {
        this.template = installer.template;
        this.deployment = new DeploymentSetup(this, installer);
        this.codegenActions = deployment.goal.isCodeGenerating() ? new ArrayList<>() : null;
        this.goal = installer.buildProcess.goal();
        this.launch = installer.launcher;
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

    /** {@return the component path of the application} */
    @Override
    public ComponentPath componentPath() {
        return ComponentKind.APPLICATION.pathNew(container.name());
    }

    public ContainerSetup container() {
        ContainerSetup c = container;
        if (c != null) {
            return c;
        }
        throw new IllegalStateException();
    }

    @Override
    public ApplicationHandle<?, ?> handle() {
        return requireNonNull(handle);
    }

    /** {@inheritDoc} */
    @Override
    public BuildLocalMap locals() {
        return locals;
    }

    /** {@return a mirror for the application} */
    @Override
    public ApplicationMirror mirror() {
        return handle().mirror();
    }

    public static ApplicationSetup crack(ApplicationConfiguration configuration) {
        return crack(ApplicationHandlers.getApplicationConfigurationHandle(configuration));
    }

    public static ApplicationSetup crack(ApplicationHandle<?, ?> handle) {
        return ApplicationHandlers.getApplicationHandleApplication(handle);
    }

    public static ApplicationSetup crack(ApplicationMirror mirror) {
        return crack(ApplicationHandlers.getApplicationMirrorHandle(mirror));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ApplicationSetup newApplication(PackedApplicationInstaller<?> installer, AssemblySetup assembly) {

        ApplicationSetup as = installer.install(new ApplicationSetup(installer));

        Function f = installer.handleFactory;
        as.handle = (ApplicationHandle<?, ?>) f.apply(installer);
//        if (as.handle == null) {
//            throw new InternalExtensionException(installer.operator.extensionType, handleFactory + " returned null, when creating a new OperationHandle");
//        }

        // Initialize the root container
        as.container = ContainerSetup.newContainer(installer.containerInstaller, as, assembly, ContainerHandle::new);
        return as;
    }

    /** The build phase of the application. */
    private enum ApplicationBuildPhase {
        ASSEMBLE, CODEGEN, COMPLETED;
    }
}
