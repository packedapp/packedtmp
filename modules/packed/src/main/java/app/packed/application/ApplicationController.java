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
package app.packed.application;

/**
 *
 */
// Advanced Marker interface. Eller ikke IDK
// Problemet er at nogle fx 


// Ideen er at extensions kan definere disse ting... som andre saa kan udnytte



// EntryExtensionPoint.CALL_ENTRY_APPLICATION_CONTROLLER  = ApplicationController<MethodHandle>
// Man kan saa faa den injected... i sin AppImplementation

public interface ApplicationController<T> {

    public static void main(String[] args) {
        
    }
}
