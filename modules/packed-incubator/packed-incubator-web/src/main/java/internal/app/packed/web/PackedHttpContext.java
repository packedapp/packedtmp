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
package internal.app.packed.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import app.packed.web.HttpContext;
import app.packed.web.HttpRequest;
import app.packed.web.HttpResponse;
import app.packed.web.session.SessionContext;

/**
 * Implementation of HttpContext wrapping a HttpExchange.
 */
public final class PackedHttpContext implements HttpContext {

    final HttpExchange exchange;
    private final PackedHttpRequest request;
    private final PackedHttpResponse response;

    public PackedHttpContext(HttpExchange exchange) {
        this.exchange = exchange;
        this.request = new PackedHttpRequest(exchange);
        this.response = new PackedHttpResponse(exchange);
    }

    @Override
    public HttpRequest request() {
        return request;
    }

    @Override
    public HttpResponse response() {
        return response;
    }

    /** Implementation of HttpRequest wrapping HttpExchange. */
    static final class PackedHttpRequest implements HttpRequest {

        private final HttpExchange exchange;

        PackedHttpRequest(HttpExchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public URI uri() {
            return exchange.getRequestURI();
        }

        @Override
        public String method() {
            return exchange.getRequestMethod();
        }

        @Override
        public String header(String name) {
            return exchange.getRequestHeaders().getFirst(name);
        }

        @Override
        public Headers headers() {
            return exchange.getRequestHeaders();
        }

        @Override
        public String queryParam(String name) {
            String query = exchange.getRequestURI().getQuery();
            if (query == null) {
                return null;
            }
            for (String param : query.split("&")) {
                String[] parts = param.split("=", 2);
                if (parts[0].equals(name)) {
                    return parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                }
            }
            return null;
        }

        @Override
        public String body() throws IOException {
            return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Implementation of HttpResponse wrapping HttpExchange. */
    static final class PackedHttpResponse implements HttpResponse {

        private final HttpExchange exchange;
        private int statusCode = 200;
        private boolean headersSent = false;

        PackedHttpResponse(HttpExchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public void header(String name, String value) {
            if (!headersSent) {
                exchange.getResponseHeaders().set(name, value);
            }
        }

        @Override
        public void status(int code) {
            if (!headersSent) {
                this.statusCode = code;
            }
        }

        @Override
        public void write(String body) throws IOException {
            write(body, "text/plain; charset=utf-8");
        }

        @Override
        public void write(String body, String contentType) throws IOException {
            if (headersSent) {
                return;
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            headersSent = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public SessionContext session() {
        return new SessionContext() {};
    }
}
