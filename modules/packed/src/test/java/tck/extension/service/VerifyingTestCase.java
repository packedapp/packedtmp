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
package tck.extension.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanExtension;
import app.packed.container.ContainerAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.service.ServiceExtension;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
@ExtendWith(VerifyingTestCase.MyExt.class)
public abstract class VerifyingTestCase {

    // @RegisterExtension
    // public static MyExt myExt = new MyExt();

    private final ArrayList<Consumer<? super ApplicationMirror>> mirrorChecks = new ArrayList<>(1);

    private TestA testA;

    protected final BeanExtension bean() {
        return use(BeanExtension.class);
    }

    private ContainerConfiguration cc() {
        if (testA == null) {
            throw new IllegalStateException();
        }
        ContainerConfiguration cc = testA.cc();

        return cc;
    }

    protected final ServiceExtension services() {
        return use(ServiceExtension.class);
    }

    protected final <E extends Extension<?>> E use(Class<E> extensionClass) {
        return cc().use(extensionClass);
    }

    protected final void checkMirror(Consumer<? super ApplicationMirror> action) {
        mirrorChecks.add(action);
    }

    public static final class MyExt implements org.junit.jupiter.api.extension.Extension , InvocationInterceptor {

        @Override
        public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
                throws Throwable {
            VerifyingTestCase lse = (VerifyingTestCase) invocationContext.getTarget().get();

            TestA ta = new TestA(lse, invocation);
            ApplicationMirror am = App.newMirror(ta);
            lse.mirrorChecks.forEach(c -> c.accept(am));
            lse.testA = null;
        }
    }

    private static class TestA extends ContainerAssembly {

        final InvocationInterceptor.Invocation<Void> invocation;
        final VerifyingTestCase lse;

        TestA(VerifyingTestCase lse, InvocationInterceptor.Invocation<Void> invocation) {
            this.lse = lse;
            this.invocation = invocation;

        }

        /** {@inheritDoc} */
        @Override
        protected void build() {
            lse.testA = this;
            try {
                invocation.proceed();
            } catch (Throwable e) {
                ThrowableUtil.orUndeclared(e);
            }
        }

        ContainerConfiguration cc() {
            return configuration();
        }
    }

}
