/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.lifecycle.runtime.ApplicationLaunchContext;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public abstract class MethodHandleInvoker {

    public static final class ApplicationBaseLauncher extends MethodHandleInvoker {

        public static ApplicationBaseLauncher EMPTY = new ApplicationBaseLauncher(
                MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class)));

        private final MethodHandle mh;

        public ApplicationBaseLauncher(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        public Object launch(ApplicationLaunchContext launchContext) {
            try {
                return mh.invokeExact(launchContext);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
    }

    public static final class ExportedServiceWrapper extends MethodHandleInvoker {
        private final MethodHandle mh;

        public ExportedServiceWrapper(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        public Object create(ExtensionContext context) {
            try {
                return mh.invokeExact(context);
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
            }
        }
    }
}
