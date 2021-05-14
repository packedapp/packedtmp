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

import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.BaseMirror;
import app.packed.application.BuildTarget;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import app.packed.component.ComponentModifier;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import packed.internal.component.NamespaceSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.PackedComponentDriver.ContainerComponentDriver;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.container.ContainerSetup;

/** The configuration of a build. */
public final class BuildSetup implements BuildMirror {

    /** The application we are building. */
    public final ApplicationSetup application;

    /** The root container in the application we are building. */
    public final ContainerSetup container;

    /** Modifiers of the build. */
    // Hmm hvad er disse i forhold til component modifiers???
    public final int modifiers;

    /** The namespace this build belongs to. */
    public final NamespaceSetup namespace = new NamespaceSetup();

    // Ideen er at vi validere per built... F.eks Foo bruger @Inject paa et field... // Assembly = sdd, Source = DDD,
    // ruleBroken = FFF
    // Man kan kun validere assemblies...
    // Maaske er det exposed paa BuildInfo...
    // Giver det mening at returnere en component hvis det er fejlet??? InjectionGraph er det eneste jeg kan taenke...
    // Object validationErrors;

    /**
     * Creates a new build setup.
     * 
     * @param modifiers
     *            the output of the build process
     */
    public BuildSetup(PackedApplicationDriver<?> applicationDriver, RealmSetup realm, PackedComponentDriver<?> componentDriver, int modifiers,
            Wirelet[] wirelets) {
        if (!(componentDriver instanceof ContainerComponentDriver containerDriver)) {
            throw new IllegalArgumentException("An application can only be created by a container component driver, driver = " + componentDriver);
        }

        this.modifiers = applicationDriver.modifiers + containerDriver.modifiers + modifiers;
        this.application = new ApplicationSetup(this, applicationDriver, realm, containerDriver, modifiers, wirelets);
        this.container = application.container;
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror application() {
        return application.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirror component() {
        return container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirrorStream components() {
        return container.mirror().stream();
    }

    public BaseMirror mirror() {
        return new BuildMirrorAdaptor(this);
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
        return PackedComponentModifierSet.isSet(modifiers, ComponentModifier.IMAGE);
    }

    @Override
    public boolean isSuccess() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BuildTarget target() {
        if (PackedComponentModifierSet.isImage(modifiers)) {
            return BuildTarget.IMAGE;
        }
        return PackedComponentModifierSet.isAnalysis(modifiers) ? BuildTarget.MIRROR : BuildTarget.INSTANCE;
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
            return build.container.mirror();
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
