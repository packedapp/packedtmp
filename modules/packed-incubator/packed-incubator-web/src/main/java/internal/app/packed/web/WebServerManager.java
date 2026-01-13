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
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import app.packed.lifecycle.Start;
import app.packed.lifecycle.Stop;
import app.packed.web.PortInUseException;

/**
 * Manages the HTTP server lifecycle for the application.
 */
public final class WebServerManager {

    private HttpServer server;
    private final Map<String, HttpHandler> handlers = new ConcurrentHashMap<>();
    private final int port = 8080;

    @Start
    public void onStart() throws IOException {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (BindException e) {
            throw new PortInUseException(port, e);
        }

        // Register all handlers that were added during build phase
        for (Map.Entry<String, HttpHandler> entry : handlers.entrySet()) {
            server.createContext(entry.getKey(), entry.getValue());
        }

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        System.out.println("HTTP Server started on port " + port);
    }

    @Stop
    public void onStop() {
        if (server != null) {
            server.stop(1);
            System.out.println("HTTP Server stopped");
        }
    }

    /**
     * Registers a handler for the given path.
     */
    public void registerHandler(String path, HttpHandler handler) {
        handlers.put(path, handler);
        if (server != null) {
            server.createContext(path, handler);
        }
    }

    /**
     * Unregisters a handler for the given path.
     */
    public void unregisterHandler(String path) {
        handlers.remove(path);
        if (server != null) {
            server.removeContext(path);
        }
    }
}
