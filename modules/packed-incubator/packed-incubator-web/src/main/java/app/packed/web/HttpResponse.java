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

import java.io.IOException;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.extension.BaseExtension;
import app.packed.operation.Op1;

/**
 * Represents an HTTP response.
 */
@AutoService(introspector = HttpResponseBeanIntrospector.class, requiresContext = HttpContext.class)
public interface HttpResponse {

    /** Sets a response header. */
    void header(String name, String value);

    /** Sets the response status code. */
    void status(int code);

    /** Writes the response body. */
    void write(String body) throws IOException;

    /** Writes the response body with a specific content type. */
    void write(String body, String contentType) throws IOException;
}

final class HttpResponseBeanIntrospector extends BeanIntrospector<BaseExtension> {

    /** {@inheritDoc} */
    @Override
    public void onAutoService(Key<?> key, OnAutoService service) {
        service.binder().bindOp(new Op1<>(HttpContext::response) {});
    }
}