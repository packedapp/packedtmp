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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import app.packed.application.ApplicationBuildHook;
import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationLocal;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.Assembly;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLocal;
import app.packed.extension.Extension;
import app.packed.namespace.NamespaceTwin;
import app.packed.util.Nullable;
import internal.app.packed.build.PackedLocalMap;
import internal.app.packed.build.PackedLocalMap.KeyAndLocalMapSource;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.component.Mirrorable;
import internal.app.packed.component.PackedComponentTwin;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.MagicInitializer;
import internal.app.packed.util.types.ClassUtil;

/**
 * Internal configuration of an application.
 * <p>
 * This class is placed in {@code internal.app.packed.container} because it is so tightly integrated with containers
 * that it made sense to put it here as well.
 */
public final class ApplicationSetup extends ComponentSetup implements PackedComponentTwin , KeyAndLocalMapSource , Mirrorable<ApplicationMirror> {

    /** A magic initializer for {@link BeanMirror}. */
    public static final MagicInitializer<ApplicationSetup> MIRROR_INITIALIZER = MagicInitializer.of(ApplicationMirror.class);

    /** Any (statically defined) children this application has. */
    final ArrayList<FutureApplicationSetup> children = new ArrayList<>();

    /**
     * A list of actions that will be executed doing the code generating phase. Or null if code generation is disabled or
     * has already been performed.
     */
    @Nullable
    private ArrayList<Runnable> codegenActions;

    /** The root container of the application. */
    public final ContainerSetup container;

    /** The deployment the application is part of. */
    public final DeploymentSetup deployment;

    /** The configuration of the application. */
    public ApplicationConfiguration configuration;

    /**
     * All extensions used in an application has a unique instance id attached. This is used in case we have multiple
     * extension with the same canonical name. Which may happen if different containers uses the "same" extension but
     * defined in different class loaders. We then compare the extension id of the extensions as a last resort when sorting
     * them.
     */
    // We actually have a unique name now, so maybe we can skip this counter
    int extensionIdCounter;

    /**
     * All extensions in the application, uniquely named.
     * <p>
     * The only time where we might see collisions is if we load 2 extensions with same name, but with different class
     * loaders.
     */
    final Map<String, Class<? extends Extension<?>>> extensions = new HashMap<>();

    /** This map maintains all locals for the entire application. */
    private final PackedLocalMap locals = new PackedLocalMap();

    /** Supplies mirrors for the application. */
    private final Supplier<? extends ApplicationMirror> mirrorSupplier;

    // Maybe move to container?? Or maybe a DomainManager class? IDK
    public final HashMap<PackedNamespaceTemplate<?>, NamespaceTwin<?, ?>> namespaces = new HashMap<>();

    /** The current phase of the application's build process. */
    private ApplicationBuildPhase phase = ApplicationBuildPhase.ASSEMBLE;

    /** All hooks applied on the application. */
    public final ArrayList<ApplicationBuildHook> hooks = new ArrayList<>();

    /**
     * Create a new application.
     *
     * @param containerBuilder
     *            the container builder
     * @param assembly
     *            the assembly that defines the application
     */
    public ApplicationSetup(PackedContainerInstaller containerBuilder, AssemblySetup assembly) {
        this.deployment = new DeploymentSetup(this, containerBuilder);
        this.codegenActions = deployment.goal.isCodeGenerating() ? new ArrayList<>() : null;
        this.mirrorSupplier = containerBuilder.applicationMirrorSupplier;
        this.container = containerBuilder.newContainer(containerBuilder, this, assembly, ContainerConfiguration::new);
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

    /** {@return a mirror that can be exposed to end-users.} */
    @Override
    public ApplicationMirror mirror() {
        return MIRROR_INITIALIZER.run(() -> ClassUtil.newMirror(ApplicationMirror.class, ApplicationMirror::new, mirrorSupplier), this);
    }

    /** The build phase of the application. */
    private enum ApplicationBuildPhase {
        ASSEMBLE, CODEGEN, COMPLETED;
    }

    @Override
    public ComponentPath componentPath() {
        return ComponentKind.APPLICATION.pathNew(container.node.name);
    }

    /** {@inheritDoc} */
    @Override
    public PackedLocalMap locals() {
        return locals;
    }

    public static ApplicationSetup crack(ApplicationLocal.ApplicationLocalAccessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return switch (accessor) {
        case ApplicationConfiguration a -> ApplicationSetup.crack(a);
        case ApplicationMirror a -> ApplicationSetup.crack(a);
        case Assembly b -> throw new UnsupportedOperationException();
        case ContainerLocal.ContainerLocalAccessor b -> ContainerSetup.crack(b).application;
        };
    }

    /** A handle that can access ApplicationConfiguration#application. */
    private static final VarHandle VH_APPLICATION_CONFIGURATION_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), ApplicationConfiguration.class,
            "application", ApplicationSetup.class);

    /** A handle that can access ApplicationMirror#application. */
    private static final VarHandle VH_APPLICATION_MIRROR_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), ApplicationMirror.class, "application",
            ApplicationSetup.class);

    public static ApplicationSetup crack(ApplicationMirror mirror) {
        return (ApplicationSetup) VH_APPLICATION_MIRROR_TO_SETUP.get(mirror);
    }

    public static ApplicationSetup crack(ApplicationConfiguration configuration) {
        return (ApplicationSetup) VH_APPLICATION_CONFIGURATION_TO_SETUP.get(configuration);
    }
}
