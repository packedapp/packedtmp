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
package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.BaseMirror;
import app.packed.application.BuildTarget;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import packed.internal.component.NamespaceSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.PackedComponentDriver.ContainerComponentDriver;
import packed.internal.component.RealmSetup;

/** The configuration of a build. */
public final class BuildSetup implements BuildMirror {

    /** The application we are building. */
    public final ApplicationSetup application;

    public final BuildTarget buildTarget;

    /** The namespace this build belongs to. */
    public final NamespaceSetup namespace = new NamespaceSetup();

    /**
     * Creates a new build.
     * 
     * @param applicationDriver
     *            the application driver of the root (and often only) application
     * @param realm
     *            the realm of the application, has been created form the assembly or composer that describes the
     *            application
     * @param componentDriver
     *            the component driver that will create the component configuration that the assembly or composer will
     *            expose
     * @param buildTarget
     *            the build target
     * @param wirelets
     *            wirelets specified by the user. May be augmented by wirelets from the application or component driver
     */
    public BuildSetup(PackedApplicationDriver<?> applicationDriver, RealmSetup realm, PackedComponentDriver<?> componentDriver, BuildTarget buildTarget,
            Wirelet[] wirelets) {
        if (!(componentDriver instanceof ContainerComponentDriver containerDriver)) {
            throw new IllegalArgumentException("An application can only be created by a container component driver, driver = " + componentDriver);
        }
        // Creates the root application of the build
        this.application = new ApplicationSetup(this, applicationDriver, realm, containerDriver, wirelets);
        this.buildTarget = requireNonNull(buildTarget);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror application() {
        return application.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirror component() {
        return application.container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirrorStream components() {
        return application.container.mirror().stream();
    }

    @Override
    public boolean isDone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFailed() {
        throw new UnsupportedOperationException();
    }

    /** {@return whether or not we are creating the root application is part of an image}. */
    public boolean isImage() {
        return buildTarget == BuildTarget.IMAGE;
    }

    @Override
    public boolean isSuccess() {
        throw new UnsupportedOperationException();
    }

    public BaseMirror mirror() {
        return new BuildMirrorAdaptor(this);
    }

    @Override
    public BuildTarget target() {
        return buildTarget;
    }

    private record BuildMirrorAdaptor(BuildSetup build) implements BaseMirror {

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror application() {
            return build.application.mirror();
        }

        @Override
        public ApplicationMirror application(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stream<ApplicationMirror> applications() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ComponentMirror component() {
            return build.application.mirror().applicationComponent();
        }

        @Override
        public ApplicationMirror component(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ComponentMirrorStream components() {
            return component().stream();
        }

        /** {@inheritDoc} */
        @Override
        public ContainerMirror container() {
            return build.application.container.mirror();
        }

        @Override
        public ContainerMirror container(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stream<ContainerMirror> containers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BuildTarget target() {
            return build.target();
        }

    }
}
