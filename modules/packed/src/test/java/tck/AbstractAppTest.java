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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import app.packed.assembly.BaseAssembly;
import app.packed.assembly.BuildableAssembly;
import app.packed.build.BuildGoal;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.operation.OperationHandle;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import tck.AbstractAppTest.InternalTestState.State1Setup;
import tck.AbstractAppTest.InternalTestState.State2Building;
import tck.AbstractAppTest.InternalTestState.State3Build;

/**
 *
 */
abstract class AbstractAppTest<A> {

    /** A thread local per test. */
    private static final ThreadLocal<TestStore> TL = new ThreadLocal<>();

    static {
        // Eclipse needs this. As it does not open the super classes of tests.
        // It only opens the actual test class
        Class<?> c = AbstractAppTest.class;
        c.getModule().addOpens(c.getPackageName(), c.getClassLoader().getUnnamedModule());
    }

    /** The state of the test. */
    private InternalTestState state;

    /** A simple boolean that can be triggered. */
    private AtomicBoolean triggered = new AtomicBoolean();

    AbstractAppTest() {}

    public final void add(OperationHandle<?> h) {
        add("main", h);
    }

    protected final void add(String name, OperationHandle<?> h) {
        configuration().use(HookTestingExtension.class).generate(name, h);
    }

    protected abstract A app();

    final ApplicationSetup applicationSetup() {
        return stateBuild().application;
    }

    protected final void assertTriggered() {
        assertTrue(triggered.get());
    }

    @AfterEach
    void clear() {
        TL.remove();
    }

    /** {@return the configuration of the container.} */
    protected final ContainerConfiguration configuration() {
        InternalTestState st = state;
        if (st instanceof State1Setup p) {
            State2Building sb = new State2Building(p);
            this.state = sb;
            return sb.cc;
        } else if (st instanceof State2Building sb) {
            return sb.cc;
        } else {
            reset();
            return configuration();
        }
    }

    public Invoker invoker() {
        return invoker("main");
    }

    public Invoker invoker(String name) {
        app();
        Map<String, Invoker> m = ts().invokers;
        requireNonNull(m);
        Invoker invoker = m.get(name);
        if (invoker == null) {
            throw new AssertionError("No such invoker '" + name + "', available" + m.keySet());
        }
        return invoker;
    }

    @BeforeEach
    protected final void reset() {
        TL.set(new TestStore());
        triggered.set(false);
        state = new State1Setup(this);
    }

    /**
     * {@return a test setup object.}. If the test is not already in the setup state it will be reset to the setup state.
     */
    protected final TestAppSetup setup() {
        InternalTestState st = state;
        if (st instanceof State1Setup s) {
            return s;
        }
        reset();
        return setup();
    }

    final State3Build stateBuild() {
        InternalTestState st = state;
        if (st instanceof State1Setup p) {
            throw new IllegalStateException("Application has already been configured, " + p);
        } else if (st instanceof State2Building sb) {
            State3Build sbb = new State3Build(sb);
            this.state = sbb;
            return sbb;
        } else {
            return (State3Build) state;
        }
    }

    protected final void trigger() {
        triggered.set(true);
    }

    static TestStore ts() {
        return TL.get();
    }

    sealed interface InternalTestState {

        final class State1Setup implements InternalTestState, TestAppSetup {

            final AbstractAppTest<?> aat;

            BuildableAssembly assembly;

            private BuildGoal goal = BuildGoal.LAUNCH;

            private List<Wirelet> wirelets = new ArrayList<>();

            State1Setup(AbstractAppTest<?> aat) {
                this.aat = requireNonNull(aat);
            }

            @Override
            public void assembleWith(BaseAssembly assembly) {
                this.assembly = requireNonNull(assembly);
                aat.configuration();
            }

            @Override
            public State1Setup goal(BuildGoal goal) {
                this.goal = requireNonNull(goal);
                return this;
            }

            @Override
            public State1Setup wirelets(Wirelet... wirelets) {
                this.wirelets.addAll(List.of(wirelets));
                return this;
            }
        }

        final class State2Building implements InternalTestState {
            static final PackedApplicationTemplate<Void> PAT = new PackedApplicationTemplate<>(Void.class,
                    new PackedContainerTemplate(PackedContainerKind.BOOTSTRAP_APPLICATION));
            final PackedApplicationInstaller<?> b;
            final AssemblySetup assembly;

            public final ContainerConfiguration cc;

            State2Building(State1Setup setup) {
                b = PAT.newInstaller(setup.goal);

                BuildableAssembly ba = setup.assembly;
                if (ba == null) {
                    ba = new BuildableAssembly() {
                        @Override
                        protected void build() {}
                    };
                }
                assembly = AssemblySetup.newSetup(b.container, ba);
                // Do
                cc = assembly.container.configuration();
                b.container.processBuildWirelets(setup.wirelets.toArray(i -> new Wirelet[i]));
            }
        }

        final class State3Build implements InternalTestState {

            Object app;

            final ApplicationSetup application;

            State3Build(State2Building sb) {
                sb.assembly.postBuild();
                this.application = sb.assembly.container.application;
            }
        }
    }

    /** A test setup object that can be used to set the build goal and wirelets. */
    public interface TestAppSetup {

        void assembleWith(BaseAssembly assembly);

        TestAppSetup goal(BuildGoal goal);

        TestAppSetup wirelets(Wirelet... wirelets);
    }

    static class TestStore {
        final Map<String, Invoker> invokers = new HashMap<>();
    }
}
