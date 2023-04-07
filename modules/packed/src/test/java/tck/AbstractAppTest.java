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
package tck;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;

import app.packed.application.BuildGoal;
import app.packed.container.BuildableAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.lifetime.LifetimeKind;
import internal.app.packed.container.ApplicationSetup;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.PackedContainerBuilder;
import internal.app.packed.container.PackedContainerHandle;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import tck.AbstractAppTest.State.State1Prepping;
import tck.AbstractAppTest.State.State2Building;
import tck.AbstractAppTest.State.State3Build;

/**
 *
 */
abstract class AbstractAppTest<A> {

    static {
        // Eclipse needs this as it does not open super classes of tests.
        Class<?> c = AbstractAppTest.class;
        c.getModule().addOpens(c.getPackageName(), c.getClassLoader().getUnnamedModule());
    }
    private AtomicBoolean B = new AtomicBoolean();
    private State state;

    protected final void trigger() {
        B.set(true);
    }

    AbstractAppTest() {}

    final ApplicationSetup appSetup() {
        return stateBuild().application;
    }

    protected final void assertTriggered() {
        assertTrue(B.get());
    }

    /** {@return the configuration of the container.} */
    protected final ContainerConfiguration configuration() {
        State st = state;
        if (st instanceof State1Prepping p) {
            State2Building sb = new State2Building(p);
            this.state = sb;
            return sb.b.cc;
        } else if (st instanceof State2Building sb) {
            return sb.b.cc;
        } else {
            reset();
            return configuration();
        }
    }

    /**
     * {@return a prepping object.}
     */
    protected final Prep prep() {
        State st = state;
        if (st instanceof State1Prepping p) {
            return p;
        }
        reset();
        return prep();
    }

    @BeforeEach
    protected final void reset() {
        B.set(false);
        state = new State1Prepping();
    }

    final State3Build stateBuild() {
        State st = state;
        if (st instanceof State1Prepping p) {
            throw new IllegalStateException("Application has already been configured yet");
        } else if (st instanceof State2Building sb) {
            State3Build sbb = new State3Build(sb);
            this.state = sbb;
            return sbb;
        } else {
            return (State3Build) state;
        }
    }

    public interface Prep {

        Prep goal(BuildGoal goal);

        default Prep image() {
            return goal(BuildGoal.IMAGE);
        }

        Prep wirelets(Wirelet... wirelets);
    }

    private static class StandaloneContainerBuilder extends PackedContainerBuilder {

        final AssemblySetup assembly;

        public final ContainerConfiguration cc;

        private final BuildGoal goal;

        /**
         * @param template
         */
        protected StandaloneContainerBuilder(BuildGoal goal) {
            super(new PackedContainerTemplate(PackedContainerKind.BOOTSTRAP_APPLICATION));
            this.goal = goal;

            assembly = new AssemblySetup(this, new BuildableAssembly() {

                @Override
                protected void build() {}
            });
            cc = new ContainerConfiguration(new PackedContainerHandle(assembly.container));

        }

        /** {@inheritDoc} */
        @Override
        public BuildGoal goal() {
            return goal;
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeKind lifetimeKind() {
            return LifetimeKind.UNMANAGED;
        }
    }

    sealed interface State {

        final class State1Prepping implements State , Prep {

            private BuildGoal goal = BuildGoal.LAUNCH;

            private List<Wirelet> wirelets = new ArrayList<>();

            @Override
            public State1Prepping goal(BuildGoal goal) {
                this.goal = requireNonNull(goal);
                return this;
            }

            @Override
            public State1Prepping wirelets(Wirelet... wirelets) {
                this.wirelets.addAll(List.of(wirelets));
                return this;
            }
        }

        final class State2Building implements State {

            final AbstractAppTest.StandaloneContainerBuilder b;

            State2Building(State1Prepping prep) {
                b = new StandaloneContainerBuilder(prep.goal);
                b.processBuildWirelet(prep.wirelets.toArray(i -> new Wirelet[i]));
            }
        }

        final class State3Build implements State {

            final ApplicationSetup application;

            Object app;
            State3Build(State2Building sb) {
                sb.b.assembly.postBuild();
                this.application = sb.b.assembly.container.application;
            }
        }
    }

    static class TestStore {

    }
}
