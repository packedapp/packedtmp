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
package app.packed.bean.hooks.sandbox;

/**
 *
 * 
 */

// Sealed

// Sammenslutning af Setter+Getter
// Q) What exception to throw, UOE? Or illegal access?
// It is determined alone by FieldHook#allowX
// No matter what MethodHook#newInvokerMust (Which currently throws UOE)
public interface VarAccessor<T> {
    
    /**
     * Gets the value of the variable.
     * 
     * @return the value
     */
    T get();
    
    /**
     * Sets the value of the variable.
     * 
     * @param value
     *            the value to set
     */
    void set(T value);

    Class<?> type();
}
// Originally the functionality of this class was spread into Setter.java and Getter.java
// However, it was much simpler to have a single class that could be injected.
// On top of that it allow for compare and swap operations/update und so weither...
