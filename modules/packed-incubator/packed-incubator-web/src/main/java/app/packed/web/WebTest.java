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

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.web.session.SessionContext;

/**
 * Demo test for the web server functionality.
 */
public class WebTest extends BaseAssembly {

    public static void main(String[] args) throws Exception {
        App app = App.start(new WebTest());

        System.out.println("Server running on http://localhost:8080");
        System.out.println("Try: curl http://localhost:8080/json");
        System.out.println("Press Enter to stop...");

        System.in.read();
        app.stop();
    }

    @Override
    protected void build() {
        install(Handlers.class);
    }

    public static class Handlers {

        Handlers(ApplicationMirror am) {
            am.operations().forEach(c -> {
                if (c instanceof WebOperationMirror wom) {
                    System.out.println(wom.urlPattern() + wom.contexts());
                }
            });
        }

        @WebGet(url = "/json")
        public void json(HttpResponse ctx, HttpRequest request) throws IOException {
            //System.out.println(sc.getClass());
            ctx.write("{\"status\":\"ok\"}", "application/json");
        }

        @WebGet(url = "/jsons")
        public void json(SessionContext ctx) {
           System.out.println(ctx.getClass());
        }
    }
}
