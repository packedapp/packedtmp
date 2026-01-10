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
package internal.app.packed.lifecycle.lifetime.entrypoint;

import java.util.ArrayList;
import java.util.List;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanIntrospector;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.lifetime.Main;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.IntrospectorOnMethod;

/** An instance of this class is shared between all entry point extensions for a single application. */
public class EntryPointManager {

    Class<? extends Extension<?>> ownedByExtension;

    @Nullable
    public Class<? extends Extension<?>> dispatcher;

    public BeanConfiguration<?> ebc;

    /** Any entry point of the lifetime, null if there are none. */
    @Nullable
    public OldEntryPointSetup entryPoint;

    /** All entry points. */
    public final List<EntryPointConf> entrypoints = new ArrayList<>();

    Class<?> resultType;

    public int takeOver(Extension<?> epe, Class<? extends Extension<?>> takeOver) {
        if (this.dispatcher != null) {
            if (takeOver == this.dispatcher) {
                return 0;
            }
            throw new IllegalStateException();
        }
        this.dispatcher = takeOver;
        // ebc = epe.provide(EntryPointDispatcher.class);
        return 0;
    }

    public static void testMethodAnnotation(BaseExtension extension, boolean isInApplicationLifetime, BeanIntrospector.OnMethod method, Main annotation) {
        BeanSetup bean = ((IntrospectorOnMethod) method).bean();

        if (!isInApplicationLifetime) {
            throw new BeanInstallationException("Must be in the application lifetime to use @" + Main.class.getSimpleName());
        }

        bean.container.lifetime.entryPoints.takeOver(extension, BaseExtension.class);

        bean.container.lifetime.entryPoints.entryPoint = new OldEntryPointSetup();

        // Ive commented this out as part of the refactoring

//        OperationTemplate temp = OperationTemplate.defaults().withReturnTypeDynamic();
//        MainOperationHandle os = method.newOperation(temp).install(MainOperationHandle::new);
//
//        MainThreadOfControl mc = bean.container.lifetime.entryPoints.entryPoint.mainThread();

      //  mc.generatedMethodHandle = OperationSetup.crack(os).codeHolder.asMethodHandle();
    }

    public static class EntryPointConf {

    }

    public static class EntryPointDispatcher {
        EntryPointDispatcher() {}
    }
}
