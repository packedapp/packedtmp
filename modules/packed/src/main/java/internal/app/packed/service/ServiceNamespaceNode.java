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
package internal.app.packed.service;

import app.packed.binding.Key;

/**
 *
 */
// We need to reintroduce this
// Because multiple namespaces (for example both main and exports) can provide the same provider

// Problem right now is the dependency tracker.
// I'm not sure how to best handle this now that service names can span a lot
// At the root of the application??

// And where should I store the service providers???
// We want to check for cycles as early as possible

// Also I think as binding you need to depend on the node instead of the provider.
// We need, for example, the exported key for error messages
public record ServiceNamespaceNode(Key<?> key, ServiceProviderSetup provider) {

}
