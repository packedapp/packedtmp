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
package app.packed.inject;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 *
 */
@Documented
@Retention(RUNTIME)
@Target({ METHOD, CONSTRUCTOR })
// @Create, @Supply
//// Factory er jo saa bedre et navn...

public @interface Make {
    // Look for exactly 1 Static Method @Make. If found use this.
    // Look for exactly 1 Constructor Method @Make. If found use this.
    // Look for exactly 1 public Constructor. If found use this
    // Look for exactly 1 protected Constructor. If found use this.
    // Look for exactly 1 package private. If found use this.
    // Look for exactly 1 private constructor. If found use this.
    //
    // // Fail with MakeException... Could not find 1 (or found many)
    //
    // //@Injection Exception
    //
    // In any of the steps, if we find more than 1 then we fail.
    //
    // Make-> Does not allow anyone to use the method...
    // Use @AllowAccess(modules = "*", value = AccessType.Invocation)
    //
    // --> @EveryoneInvoke (meta annotation)
}
// Kunne altsaa ogsaa have en @Make field... 

//Maaske bruge @Inject alligevel. 
// Som regel vil method injection returnere void...
// Men det kan vi jo ikke bruge, eftersom den skal lave noget...
// Saa vi kan smide en exception der.
// Eller hvis hvis der er >1 statiske metoder.

// Saa det er i virkeligheden kun hvis vi har en statisk non-void metode.
// At vi risikere problemer....

//////// Static inject is not supported out of the box.... If you need it, you can do it yourself...
//@Inject meta annotations...