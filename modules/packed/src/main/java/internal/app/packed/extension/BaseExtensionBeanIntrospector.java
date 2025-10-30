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
package internal.app.packed.extension;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.IntrospectorOnContextService;

/**
 *
 */
public abstract class BaseExtensionBeanIntrospector extends BeanIntrospector<BaseExtension> {

    public final BeanSetup bean() {
        return BeanSetup.crack(this);
    }

    @Override
    public final void onExtensionService(Key<?> key, OnContextService service) {
        onExtensionService(key, (IntrospectorOnContextService) service);
    }

    public void onExtensionService(Key<?> key, IntrospectorOnContextService service) {
        super.onExtensionService(key, service);
    }

}
