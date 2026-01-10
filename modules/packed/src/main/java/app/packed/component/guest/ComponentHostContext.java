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
package app.packed.component.guest;

import java.util.Set;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoInject;
import app.packed.binding.Key;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import internal.app.packed.application.GuestBeanHandle;

/**
 * I think this probably mostly be informational. You would only need it for debugging.
 *
 * @see FromComponentGuest
 * @see OnComponentGuestLifecycle
 */
@AutoInject(introspector = ComponentHostContextBeanIntrospector.class, requiresContext = ComponentHostContext.class)
// Alternativt er ogsaa at have en per type.
// A.ka GuestApplicationContext
public interface ComponentHostContext extends Context<BaseExtension> {

    // Hvis vi teanker paa at faa injected selve bean instancen. Kan vi sagtens have Object, eller Entity som key
    /**
     * {@return services that are available from the guest}
     * <p>
     * This services can be injected into parameter of a factory method using {@link FromComponentGuest}.
     */
    Set<Key<?>> keys();
}

final class ComponentHostContextBeanIntrospector extends BeanIntrospector<BaseExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        ComponentHostContext c = beanHandle(GuestBeanHandle.class).get().toContext();
        service.binder().bindConstant(c);
    }
}
