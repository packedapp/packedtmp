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
package app.packed.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.TypeToken;
import app.packed.inject.FactoryType;

/**
 *
 */
/// Er det altid en operation????
//// Nej, f.eks kan det vaere alle @Initialize actions eller
//// Alle lifecycle actions ialt.
//// Tror der er en invoker = 0..N Operations (maaske 1..N)

/// Nej hvis vi har specificeret et funktions interface saa er det vel ikke???
/// Eller vi har jo lyst til at det har samme interne interface (MethodHandle)

/// lookup MH from path. (trie->int). invoke with pritive wrapper



public interface InvokerConfiguration {

    InvokerConfiguration addArgument(Class<?> key);
    InvokerConfiguration addArgument(Key<?> key);

    // Den er jo bestemt af beanen...
    // Fx en EntityBan har maaske ikke en extension context
    // som parameter. Men selv instancen...
    MethodType invocationType();

    FactoryType targetType();
    
    //// Bliver det kaldt before extension.close???
    //// Vi skal fx have mulighed for at kalde MHS.freezeArray()
    //// Det skal vaere after resolve operation, men foer resolve
    //// extension bean...
    
    // onWired???? onBuild
    void onReady(Consumer<MethodHandle> action);
    <T> void onReady(Class<T> functionalInterface, Consumer<T> action);
    <T> void onReady(TypeToken<T> functionalInterface, Consumer<T> action);
    
}
// addArgument(HttpRequest.class).onReady(mh -> mhs[i]= mh);
