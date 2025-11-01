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
package internal.app.packed.extension.base;

import app.packed.bean.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.component.guest.ComponentHostContext;
import app.packed.extension.BaseExtension;
import internal.app.packed.application.GuestBeanHandle;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.lifecycle.runtime.ApplicationLaunchContext;

/**
 *
 */
public final class BaseExtensionHostGuestBeanintrospector extends BeanIntrospector<BaseExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        OnVariableUnwrapped binding = service.binder();

        Class<?> hook = key.rawType();

        if (ApplicationLaunchContext.class.isAssignableFrom(hook)) {
            binding.bindContext(ApplicationLaunchContext.class);
        } else if (hook == ExtensionContext.class) {
            // We probably should have failed already, so no need to check. Only beans that are in the context
            if (beanOwner().isApplication()) {
                binding.failWith(hook.getSimpleName() + " can only be injected into bean that owned by an extension");
            }
            binding.bindContext(ExtensionContext.class);
        } else if (hook == ComponentHostContext.class) {
            ComponentHostContext c = beanHandle(GuestBeanHandle.class).get().toContext();
            binding.bindInstance(c);
        } else {
            super.onExtensionService(key, service);
        }
    }
}
