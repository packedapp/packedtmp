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

import app.packed.application.BootstrapApp.Image;
import app.packed.build.BuildGoal;
import app.packed.component.ComponentHandle;
import app.packed.component.ComponentPath;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedBootstrapImage.ImageEager;
import internal.app.packed.application.PackedBootstrapImage.ImageNonReusable;

/**
 * An application handle represents an installed application towards the person
 * An extendable handle for an application.
 */
public non-sealed class ApplicationHandle<A, C extends ApplicationConfiguration> extends ComponentHandle implements ApplicationBuildLocal.Accessor {

    /** The handle's application. */
    final ApplicationSetup application;

    /** The lazy generated application configuration. */
    @Nullable
    private C configuration;

    /** An image if the application has been constructed using {@link BuildGoal#IMAGE}. */
    @Nullable
    private final Image<A> image;

    /** The lazy generated application mirror. */
    @Nullable
    private ApplicationMirror mirror;

    /**
     * Creates a new application handle.
     *
     * @param installer
     *            the installer for the application
     */
    public ApplicationHandle(ApplicationInstaller<?> installer) {
        PackedApplicationInstaller<?> inst = (PackedApplicationInstaller<?>) installer;
        this.application = inst.toSetup();

        // Build an image if that is the target.
        BootstrapApp.Image<A> img = null;
        if (inst.buildProcess.goal() == BuildGoal.IMAGE) {
            img = new ImageEager<>(this);
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
        checkIsOpen();
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
    // Tror jeg fjerner den her... BaseImage er kun noget man bruger ved rødder tænker jeg???
    public final BootstrapApp.Image<A> image() {
        BootstrapApp.Image<A> i = image;
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

    /** {@inheritDoc} */
    @Override
    public final boolean isOpen() {
        return application.container().assembly.isConfigurable();
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

    /** {@return the name of the application} */
    public final String name() {
        return application.container().name();
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
