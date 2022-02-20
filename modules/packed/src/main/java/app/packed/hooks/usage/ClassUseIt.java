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
package app.packed.hooks.usage;

import app.packed.hooks.BeanMethod;
import app.packed.hooks.accessors.MethodAccessor;

/**
 *
 */
public class ClassUseIt {

    class Startup extends BeanMethod {
        
        // Maaske er invoker<?> bare en default invoker()
        // Som er tilgaengelig...
        // Tror maaske det skal saettes op i Bootstrap
        // Hvis man vil have mulighed for at kunne se hvad der sker...
        // @HintCallInvoker <---
        //@OnInitialize
        public void invoke(MethodAccessor<?> i) throws Throwable {
            i.call(); // another method could just set the invoker...
            // for later invocation, a.la. web
            // So we will never be able to see what exactly is going on
            // Unless we get hints from the extension developer
            // or use, for example, schudule(....);
            // Men saa mister vi det at bare kunne tage de parametere
            // vi har lyst til.
        }
    }

}
