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
package app.packed.component.driver;

/**
 *
 */
public abstract class ComponentDriverBundle {

    protected abstract void configure();

    // AddHost
    // ScanForImagesViaAnnotation(X) //actors possible children
    // --DoX Process them as Foo. Bundles/sources that can be instantiated with an empty constructor....
    // from the scannable host are accepted.
    // Actor foo must have access to the constructor of FooChild() but it was private, located in another bundle, ect...
}
