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
package app.packed.web;

import app.packed.bean.BeanTrigger.AutoInject;
import app.packed.binding.Key;
import app.packed.context.Context;
import internal.app.packed.bean.scanning.IntrospectorOnContextService;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
/**
 * Context providing access to the HTTP request and response.
 */
@AutoInject(introspector = HttpContextBeanIntrospector.class, requiresContext = HttpContext.class)
public interface HttpContext extends Context<WebExtension> {

    /** Returns the current HTTP request. */
    HttpRequest request();

    /** Returns the current HTTP response. */
    HttpResponse response();
}

final class HttpContextBeanIntrospector extends BaseExtensionBeanIntrospector {

    /** {@inheritDoc} */
    @Override
    public void onExtensionService(Key<?> key, IntrospectorOnContextService service) {
        service.binder().bindContext(HttpContext.class);
    }
}
