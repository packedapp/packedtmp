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
import java.net.URI;

import com.sun.net.httpserver.Headers;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.operation.Op1;

/**
 * Represents an HTTP request.
 */
@AutoService(introspector = HttpRequestBeanIntrospector.class, requiresContext = HttpContext.class)
public interface HttpRequest {

    /** Returns the request URI. */
    URI uri();

    /** Returns the request method (GET, POST, etc.). */
    String method();

    /** Returns a request header value. */
    String header(String name);

    /** Returns all request headers. */
    Headers headers();

    /** Returns a query parameter value. */
    String queryParam(String name);

    /** Returns the request body as a string. */
    String body() throws IOException;
}

final class HttpRequestBeanIntrospector extends BeanIntrospector<WebExtension> {

    @Override
    public void onAutoService(Key<?> key, OnAutoService service) {
        service.binder().bindOp(new Op1<>(HttpContext::request) {});
    }
}
