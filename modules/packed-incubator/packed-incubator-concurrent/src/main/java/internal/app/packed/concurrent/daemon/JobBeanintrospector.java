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
package internal.app.packed.concurrent.daemon;

import java.lang.annotation.Annotation;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.concurrent.job.DaemonJob;
import app.packed.concurrent.job.DaemonJobContext;
import app.packed.extension.BaseExtension;
import internal.app.packed.extension.PackedBeanIntrospector;

/**
 * Handles job annotations.
 */
public final class JobBeanintrospector extends PackedBeanIntrospector<BaseExtension> {

    /**
     * Handles {@link DaemonJob}.
     *
     * {@inheritDoc}
     */
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        if (annotation instanceof DaemonJob daemon) {
            DaemonOperationHandle.onAnnotatedMethod(extensionHandle(), method, daemon);
        } else {
            super.onAnnotatedMethod(annotation, method);
        }
    }

    /**
     * Handles {@link DaemonJobContext}.
     *
     * {@inheritDoc}
     */
    @Override
    public void onExtensionService(Key<?> key, OnExtensionService service) {
        if (service.baseClass() == DaemonJobContext.class) {
            service.binder().bindInvocationArgument(1);
        } else {
            super.onExtensionService(key, service);
        }
    }
}
