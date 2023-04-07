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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;

import app.packed.application.ApplicationMirror;
import app.packed.application.BootstrapApp;
import app.packed.bean.BeanMirror;
import app.packed.container.Author;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.operation.Op;
import app.packed.service.ServiceableBeanConfiguration;
import internal.app.packed.container.ApplicationSetup;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.container.WireletSelectionArray;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import internal.app.packed.util.ThrowableUtil;
import tck.AbstractAppTest.State.State3Build;

/**
 *
 */
public abstract class AbstractBootstrapedAppTest<A> extends AbstractAppTest<A> {

    final BootstrapApp<A> app;

    final Holder holder;

    protected AbstractBootstrapedAppTest(BootstrapApp<A> app) {
        this.app = requireNonNull(app);
        this.holder = Holder.of(app);
    }

    protected final ApplicationMirror appMirror() {
        return appSetup().mirror();
    }

    protected final void assertIdenticalMirror(Object expected, Object actual) {
        assertEquals(expected, actual);
    }

    protected final BaseExtension base() {
        return configuration().use(BaseExtension.class);
    }

    /** {@return a bean mirror for the a single application bean.} */
    protected final BeanMirror findSingleApplicationBean() {
        List<BeanMirror> beans = appMirror().container().beans().filter(b -> b.owner() == Author.application()).toList();
        assertThat(beans).hasSize(1);
        return beans.get(0);
    }

    protected final HookExtension hooks() {
        return configuration().use(HookExtension.class);
    }

    protected final <T> ServiceableBeanConfiguration<T> install(Class<T> implementation) {
        return base().install(implementation);
    }

    protected final <T> ServiceableBeanConfiguration<T> install(Op<T> implementation) {
        return base().install(implementation);
    }

    protected final <T> ServiceableBeanConfiguration<T> installInstance(T instance) {
        return base().installInstance(instance);
    }

    @SuppressWarnings("unchecked")
    protected final A app() {
        State3Build b = stateBuild();
        if (b.app != null) {
            return (A) b.app;
        }
        return launch();
    }

    protected final A launch() {
        return launch(new Wirelet[0]);
    }

    @SuppressWarnings("unchecked")
    protected final A launch(Wirelet... wirelets) {
        State3Build b = stateBuild();
        ApplicationSetup as = b.application;

        // Right now we do not support runtime wirelets
        ApplicationLaunchContext alc = ApplicationLaunchContext.launch(as, WireletSelectionArray.of(wirelets));

        A app = (A) holder.newHolder(alc);
        b.app = app;
        return app;
    }

    /** The internal configuration of a bootstrap app. */
    record Holder(Supplier<? extends ApplicationMirror> mirrorSupplier, PackedContainerTemplate template, MethodHandle mh) {

        /**
         * Create a new application interface using the specified launch context.
         *
         * @param context
         *            the launch context to use for creating the application instance
         * @return the new application instance
         */
        public Object newHolder(ApplicationLaunchContext context) {
            try {
                return mh.invokeExact(context);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        @SuppressWarnings("unchecked")
        private static <T> T read(Object o, String fieldName) throws ReflectiveOperationException {
            Field f = o.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(o);
        }

        public static Holder of(BootstrapApp<?> app) {
            try {
                Object holder = read(app, "holder");
                return new Holder(read(holder, "mirrorSupplier"), read(holder, "template"), read(holder, "mh"));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
    }
}
