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
package internal.app.packed.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import app.packed.build.hook.BuildHook;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.InternalExtensionException;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionHandle;
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

    public static final class ExtensionFactory extends MethodHandleInvoker {
        private final MethodHandle mh;

        public ExtensionFactory(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        public Extension<?> create(ExtensionSetup extension) {
            ExtensionHandle<?> handle = new PackedExtensionHandle<>(extension);
            try {
                return (Extension<?>) mh.invokeExact(handle);
            } catch (Throwable e) {
                throw new InternalExtensionException("An instance of the extension " + extension.model.fullName() + " could not be created.", e);
            }
        }
    }

    public static final class BuildHookFactory extends MethodHandleInvoker {
        private final MethodHandle mh;

        public BuildHookFactory(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        public BuildHook create() {
            try {
                return (BuildHook) mh.invokeExact();
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
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
