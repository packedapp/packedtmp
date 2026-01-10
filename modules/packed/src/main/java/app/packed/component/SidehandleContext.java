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
package app.packed.component;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoInject;
import app.packed.binding.Key;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.sidehandle.SidehandleBeanHandle;

/**
 *
 */
@AutoInject(requiresContext = SidehandleContext.class, introspector = SidehandleContextBeanIntrospector.class)
public interface SidehandleContext extends Context<BaseExtension> {}

final class SidehandleContextBeanIntrospector extends BeanIntrospector<BaseExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        SidehandleContext c = beanHandle(SidehandleBeanHandle.class).get().toContext();
        service.binder().bindConstant(c);
    }
}
