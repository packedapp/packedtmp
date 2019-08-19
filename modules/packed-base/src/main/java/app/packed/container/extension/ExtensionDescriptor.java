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
package app.packed.container.extension;

/**
 *
 */
class ExtensionDescriptor {

    // Hook Annotations
    //// Field | Method | Activating (Although you can see that on the Annotation)

    //// Other Extension

    //// Sidecars

    public static ExtensionDescriptor of(ExtensionDescriptor ed) {
        throw new UnsupportedOperationException();
    }

    // Et muligt design er at tage en ExtensionConfiguration klasse som parameter til
    // Extension(Class<? extends ExtensionConfiguration>)

    // static final ExtensionConfig EC = initialize(new InjectionExtensionConfig());
}
