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
package app.packed.namespace.sandbox;

import app.packed.build.BuildAuthority;

/**
 *
 */

// Ideen er vist at baade Users og Extensions kan have et "main" namespace for fx services.
// Ved ikke om vi skal bruge bruge den i sidste ende
 record NamespaceKey(BuildAuthority authority, String name) {}

// Problemet er lidt her at vi ikke kan skrive et fyldestgoerende navn
// Eftersom vi kan have flere extensions med samme navn...
// Saa den er maaske ikke super brugbar


// main <-- for user
// $ConfigExtension$main  should be obvious