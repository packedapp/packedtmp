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
package app.packed.component.sandbox;

/**
 *
 * <p>
 * Don't know if this will make it to final version
 */
// Extension + ComponentTemplate -> ComponentHandle -> ComponentConfiguration
public interface ComponentTemplate {

    /**
     * {@return the module that owns the template}
     * <p>
     * By default an extension can only create modules
     */
    // Altsaa den eksistere jo kun saa vi kan undgaa at lave Descriptors
    Module module();
}
