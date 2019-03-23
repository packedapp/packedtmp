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
package app.packed.app;

/**
 * A context object that is shared all containers in an application. Is typically dependency injected into a service or
 * a component.
 */
public interface AppContext {

    // SharedContext

    // Allow for shutdown... Could be done from container context...
    // shutdownApp();

    /**
     * Returns the name of the application
     * 
     * @return
     */
    String name();

    /**
     * The app of this context.
     * 
     * @return the app of this context.
     */
    App app();
}
