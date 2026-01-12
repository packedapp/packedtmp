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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

import app.packed.component.SidehandleBinding;
import app.packed.component.SidehandleBinding.Kind;
import app.packed.lifecycle.Start;
import app.packed.lifecycle.Stop;
import app.packed.web.HttpContext;
import app.packed.web.HttpRequest;
import app.packed.web.HttpResponse;

/**
 * Per-operation sidehandle that manages HTTP handler registration.
 */
public final class WebServerSidehandle implements HttpContext {

    private final String urlPattern;
    private final WebHandlerInvoker invoker;
    private final WebServerManager serverManager;

    /** Thread-local to hold current context during request handling. */
    private static final ThreadLocal<PackedHttpContext> currentContext = new ThreadLocal<>();

    public WebServerSidehandle(
            @SidehandleBinding(Kind.CONSTANT) String urlPattern,
            @SidehandleBinding(Kind.OPERATION_INVOKER) WebHandlerInvoker invoker,
            WebServerManager serverManager) {
        this.urlPattern = requireNonNull(urlPattern);
        this.invoker = requireNonNull(invoker);
        this.serverManager = requireNonNull(serverManager);
    }

    @Start
    protected void onStart() {
        serverManager.registerHandler(urlPattern, this::handleRequest);
    }

    @Stop
    protected void onStop() {
        serverManager.unregisterHandler(urlPattern);
    }

    private void handleRequest(HttpExchange exchange) {
        PackedHttpContext ctx = new PackedHttpContext(exchange);
        currentContext.set(ctx);
        try {
            invoker.invoke(this);
        } catch (Throwable e) {
            try {
                String error = "Internal Server Error: " + e.getMessage();
                byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (IOException ioe) {
                // Ignore if we can't send error response
            }
        } finally {
            currentContext.remove();
            exchange.close();
        }
    }

    @Override
    public HttpRequest request() {
        PackedHttpContext ctx = currentContext.get();
        return ctx != null ? ctx.request() : null;
    }

    @Override
    public HttpResponse response() {
        PackedHttpContext ctx = currentContext.get();
        return ctx != null ? ctx.response() : null;
    }

    /** Interface for invoking the handler method. */
    public interface WebHandlerInvoker {
        void invoke(WebServerSidehandle context) throws Throwable;
    }
}
