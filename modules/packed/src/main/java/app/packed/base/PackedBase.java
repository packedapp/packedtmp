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
package app.packed.base;

/**
 *
 */
// Vi maa have en for hver...
// Kan jo godt have en anden jar af web paa
class PackedBase {

    // Per module layer??? Maybe not here
    // Maaske paa LoggingExtensionSupport klassen
    public static boolean isJavaLoggingAvailable() {
        return true;
    }
    
    public static boolean isJavaManagementAvailable() {
        return true;
    }
    
    public static boolean isJdkJfrAvailable() {
        return true;
    }
    
    public static Runtime.Version version() {
        // I think we long term want out own version class..
        throw new UnsupportedOperationException();
    }
}
