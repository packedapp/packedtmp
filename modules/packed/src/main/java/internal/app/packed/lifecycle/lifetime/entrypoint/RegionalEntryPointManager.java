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
package internal.app.packed.lifecycle.lifetime.entrypoint;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanInstallationException;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.lifetime.Main;
import app.packed.operation.OperationTemplate;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.PackedBeanMethod;
import internal.app.packed.lifecycle.lifetime.entrypoint.OldEntryPointSetup.MainThreadOfControl;

/** An instance of this class is shared between all entry point extensions for a single application. */
public class RegionalEntryPointManager {

    Class<? extends Extension<?>> controlledBy;

    @Nullable
    public Class<? extends Extension<?>> dispatcher;

    public BeanConfiguration ebc;

    /** Any entry point of the lifetime, null if there are none. */
    @Nullable
    public OldEntryPointSetup entryPoint;

    /** All entry points. */
    public final List<EntryPointConf> entrypoints = new ArrayList<>();

    MethodHandle[] entryPoints;

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

    public static boolean testMethodAnnotation(BaseExtension extension, boolean isInApplicationLifetime, PackedBeanMethod method, Annotation annotation) {
        BeanSetup bean = method.bean();

        if (annotation instanceof Main) {

            if (!isInApplicationLifetime) {
                throw new BeanInstallationException("Must be in the application lifetime to use @" + Main.class.getSimpleName());
            }

            bean.container.lifetime.entryPoints.takeOver(extension, BaseExtension.class);

            bean.container.lifetime.entryPoints.entryPoint = new OldEntryPointSetup();

            OperationTemplate temp = OperationTemplate.of(c -> c.returnTypeDynamic());
            MainOperationHandle os = method.newOperation(temp).install(MainOperationHandle::new);

            MainThreadOfControl mc = bean.container.lifetime.entryPoints.entryPoint.mainThread();

            mc.generatedMethodHandle=os.invokerAsMethodHandle();
//            os.generateMethodHandleOnCodegen(mh -> mc.generatedMethodHandle = mh);
            return true;
        }

        return false;
    }

    public static class EntryPointConf {

    }

    public static class EntryPointDispatcher {
        EntryPointDispatcher() {}
    }
}

