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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.function.Executable;

import app.packed.application.ApplicationMirror;
import app.packed.application.BootstrapApp;
import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanMirror;
import app.packed.build.BuildActor;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.operation.Op;
import app.packed.operation.OperationMirror;
import app.packed.runtime.RunState;
import app.packed.service.ProvidableBeanConfiguration;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.lifecycle.lifetime.runtime.ApplicationLaunchContext;
import tck.AbstractAppTest.InternalTestState.State3Build;

/**
 * A test that uses a bootstrap app.
 */
public abstract class AbstractBootstrapedAppTest<A> extends AbstractAppTest<A> {

    protected AbstractBootstrapedAppTest(BootstrapApp<A> app) {
        super(app);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final A app() {
        State3Build b = stateBuild();
        if (b.app != null) {
            return (A) b.app;
        }
        return launch();
    }

    /** {@return the base extension.} */
    protected final BaseExtension base() {
        return configuration().use(BaseExtension.class);
    }

    /** { @return a special hook extension used for testing.} */
    public final HookTestingExtension hooks() {
        return configuration().use(HookTestingExtension.class);
    }

    protected final <T> ProvidableBeanConfiguration<T> install(Class<T> implementation) {
        return base().install(implementation);
    }

    protected final <T> ProvidableBeanConfiguration<T> install(Op<T> implementation) {
        return base().install(implementation);
    }

    protected final <T> ProvidableBeanConfiguration<T> installInstance(T instance) {
        return base().installInstance(instance);
    }

    /** {@return launches and returns the application.} */
    protected final A launch() {
        return launch(new Wirelet[0]);
    }

    @SuppressWarnings("unchecked")
    protected final A launch(Wirelet... wirelets) {
        State3Build b = stateBuild();
        ApplicationSetup as = b.application;

        A app = (A) ApplicationLaunchContext.launch(as.handle(), RunState.TERMINATED, wirelets);
        // Right now we do not support runtime wirelets
//        ApplicationLaunchContext alc = ApplicationLaunchContext.launch(RunState.TERMINATED, as, WireletSelectionArray.of(wirelets));
//
//        A app = (A) alc.newHolder(internals.launcher);
        b.app = app;
        return app;
//        return requireNonNull(app);
    }

    protected final void link(String name, Assembly assembly, Wirelet... wirelets) {
        base().link(name, assembly, wirelets);
    }

    /** {@return a mirror for the application.} */
    protected final Mirrors mirrors() {
        return new Mirrors();
    }

    /**
     * There are certain classes (typically mirrors) that can only be instantiated within a context provided by the
     * framework.
     *
     * @param e
     *            the test executable
     */
    protected static final void assertFrameworkInitializes(Executable e) {
        assertThrows(IllegalStateException.class, e);
    }

    public final class Mirrors {

        /** {@return a mirror for the application.} */
        public ApplicationMirror application() {
            return applicationSetup().mirror();
        }

        /** {@return a mirror for the application.} */
        public AssemblyMirror assembly() {
            return application().assemblies().root();
        }

        public void assertIdentical(Object expected, Object actual) {
            assertEquals(expected, actual);
        }

        /** {@return a bean mirror for the a single application bean.} */
        public BeanMirror bean() {
            return findSingleBean(container());
        }

        /** {@return a mirror for the root container in the application.} */
        public ContainerMirror container() {
            return application().container();
        }

        public BeanMirror findSingleBean(ContainerMirror c) {
            List<BeanMirror> beans = c.beans().filter(b -> b.owner() == BuildActor.application()).toList();
            assertThat(beans).hasSize(1);
            return beans.get(0);
        }

        public OperationMirror findSingleOperation(BeanMirror c) {
            List<OperationMirror> mirrors = c.operations().toList();
            assertThat(mirrors).hasSize(1);
            return mirrors.get(0);
        }
    }

    /** Used for extracting the internal configuration of BootstrapApp. */
    record BootstrapAppInternals(PackedApplicationTemplate<?> template, MethodHandle launcher) {

        /**
         * Create a new application interface using the specified launch context.
         *
         * @param context
         *            the launch context to use for creating the application instance
         * @return the new application instance
         */
//        public Object newHolder(ApplicationLaunchContext context) {
//            try {
//                return mh.invokeExact(context);
//            } catch (Throwable e) {
//                throw ThrowableUtil.orUndeclared(e);
//            }
//        }

        @SuppressWarnings("unchecked")
        private static <T> T read(Object o, String fieldName) throws ReflectiveOperationException {
            Field f = o.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(o);
        }

        public static BootstrapAppInternals extractInternals(BootstrapApp<?> app) {
            try {
//                Object holder = read(app, "setup");
                return new BootstrapAppInternals(read(app, "template"), read(app, "launcher"));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
    }
}
