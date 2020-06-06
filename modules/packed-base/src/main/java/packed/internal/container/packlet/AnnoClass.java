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
package packed.internal.container.packlet;

/**
 *
 */
class AnnoClass extends AbstractAnno {

    // May be overridden on the component, but have already been checked
    // May be overridden on the container, but have already been checked
    static final ClassValue<AnnoClass> DEFAULTS = new ClassValue<AnnoClass>() {

        @Override
        protected AnnoClass computeValue(Class<?> type) {
            // See if it has a packlet annotation...
            return null;
        }
    };

    // I think the container support should also be useable for selective overrides (per class)... Maybe even via
    // non-annotations
    // Hmmmm, is there any situations where we want to treat components differently???
}
