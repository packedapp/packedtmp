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
package app.packed.application;

import java.lang.invoke.MethodHandle;

import app.packed.build.BuildGoal;
import app.packed.component.ComponentHandle;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedBaseImage.ImageEager;
import internal.app.packed.application.PackedBaseImage.ImageNonReusable;
import internal.app.packed.container.wirelets.WireletSelectionArray;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;

/**
 * An extendable handle for an application.
 */
public non-sealed class ApplicationHandle<C extends ApplicationConfiguration, A> extends ComponentHandle implements ApplicationBuildLocal.Accessor {

    /** The handle's application. */
    final ApplicationSetup application;

    /** The lazy generated application configuration. */
    @Nullable
    private C configuration;

    /** An image if the application has been constructed using {@link BuildGoal#IMAGE}. */
    @Nullable
    private final BaseImage<?> image;

    /** The lazy generated application mirror. */
    @Nullable
    private ApplicationMirror mirror;

    /**
     * Creates a new application handle.
     *
     * @param installer
     *            the installer for the application
     */
    public ApplicationHandle(ApplicationTemplate.Installer<A> installer) {
        PackedApplicationInstaller<A> inst = (PackedApplicationInstaller<A>) installer;
        this.application = inst.toHandle();

        // Build an image if that is the target.
        BaseImage<?> img = null;
        if (inst.buildProcess.goal() == BuildGoal.IMAGE) {
            img = new ImageEager<>(application);
            if (!inst.optionBuildReusableImage) {
                img = new ImageNonReusable<>(img);
            }
        }
        this.image = img;

    }

    /** {@return the build goal that was used to build the application} */
    public final BuildGoal buildGoal() {
        return application.goal;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return application.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public final void componentTag(String... tags) {
        checkIsConfigurable();
        application.componentTags.addComponentTags(application, tags);
    }

    /**
     * { @return the user exposed configuration of the application}
     * <p>
     * This method will always return the same configuration instance.
     * <p>
     * This method relays to {@link #newApplicationConfiguration()} to create the actual configuration instance. And will
     * then cache the instance for future usage.
     */
    public final C configuration() {
        C c = configuration;
        if (c == null) {
            c = configuration = newApplicationConfiguration();
        }
        return c;
    }

    /**
     * The image
     *
     * @return the base image for the application
     * @throws IllegalStateException
     *             if the application was build with {@link BuildGoal#IMAGE}.
     */
    public final BaseImage<?> image() {
        BaseImage<?> i = image;
        if (i == null) {
            throw new IllegalStateException("The application must be installed with BuildImage, was " + application.goal);
        }
        return i;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurable() {
        return application.container().assembly.isConfigurable();
    }

    /**
     * Launches an instance of the application this handle represents.
     * <p>
     *
     * @param state
     *            the state to launch the application in
     * @param wirelets
     *            optional wirelets
     * @return the application instance
     */
    @SuppressWarnings("unchecked")
    public final A launch(RunState state, Wirelet... wirelets) {
        ApplicationLaunchContext alc = ApplicationLaunchContext.launch(state, this, WireletSelectionArray.of(wirelets));
        MethodHandle mh = application.launch;
        Object result;
        try {
            result = mh.invokeExact(alc);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return (A) result;
    }

    /** {@return a mirror for the application} */
    @Override
    public final ApplicationMirror mirror() {
        ApplicationMirror m = mirror;
        if (m == null) {
            m = mirror = newApplicationMirror();
        }
        return m;
    }

    /**
     * {@return a new configuration's object for the application}
     * <p>
     * This method can be overridden to return a subclass of {@link ApplicationConfiguration}.
     */
    @SuppressWarnings("unchecked")
    protected C newApplicationConfiguration() {
        return (C) new ApplicationConfiguration(this);
    }

    /**
     * {@return a new mirror for the application}
     * <p>
     * This method can be overridden to return a subclass of {@link ApplicationMirror}.
     */
    protected ApplicationMirror newApplicationMirror() {
        return new ApplicationMirror(this);
    }

    // Would be strange if we cannot decide the containerMirror/configuration that should be returned...
    protected ContainerHandle<?> newRootContainerHandle(ContainerTemplate.Installer installer) {
        return new ContainerHandle<>(installer);
    }

//    // Then we need to have a buildtime repository also
//    // But then we change at runtime??? Because we still have the handles..
//    // Nah, vil jeg ikke have
//    public final Optional<ApplicationRepository<?>> repository() {
//        // So a handle can be in one repository.
//        // But have instances in multiple InstanceManagers
//        // We can have removeFromRepository();
//        return Optional.empty();
//    }
}
