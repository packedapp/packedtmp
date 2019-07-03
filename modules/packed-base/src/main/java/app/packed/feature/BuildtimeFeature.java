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
package app.packed.feature;

import app.packed.component.Component;
import app.packed.util.Nullable;

/**
 *
 */
// Ellers omvendt kan man indikere at det er en runtime feature....

// Dvs. hvis man ikke implementere dette interface, bliver man automatisk siet fra...
// Omvendt, saa kan folk altid caste en feature til det...
public interface BuildtimeFeature {

    // return null to have the feature removed whenever the component is instantiated.
    // Otherwise the runtime will call this method every time a component is instantiated.

    // This method will be invoked multiple times for an image...
    // What if want to register under a different key???
    @Nullable
    Object toRuntimeFeature(Component component);
}
