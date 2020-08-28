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
package app.packed.sidecar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;

/**
 *
 */
// PreCompiler en Invoker der burger indy...
// Saa laver vi en ny instant hver gang.. 
// Og har en bootstrap metode der bruger noget thread local..

// Vi bliver noedt til at have denne fordi vi ikke kan lave method handles paa runtime...
public interface Invoker {

    /**
     * Invokes the underlying executable.
     * 
     * @return the result
     * @throws Throwable
     *             anything thrown by the underlying executable propagates unchanged through the method handle call
     */
    @Nullable
    Object invoke() throws Throwable;

    /**
     * Returns a new parameter-less method handle. The return type of the method handle will be the exact return type of the
     * underlying executable.
     * 
     * @return the method handle
     */
    // Kan ikke se hvordan den kan virke med graal....
    // Da vi ikke kan binde paa runtime...
    MethodHandle toMethodHandle();
}

//Always only available at runtime
@Target(ElementType.TYPE)
@interface InjectInvoker {
    Class<?> type();

    Class<?>[] typeParameters() default {}; // if left empty = Lower bound everywhere. If filled out. All type parameters must be filled out

    Class<?>[] typeTemplates() default {}; //
}
//@InjectInvoker(type = MethodHandle.class) <--- will be bound to any component instance. 
//@InjectInvoker(type = Invoker1.class, typeParameters=SomeTemplate.class, typeTemplates = SomeTemplate.class). not a big fan...

//Many, so we can repeat it
//If Getter+ Setter??? No I think
//We will make this automatically for a JavaBeanProperty sidecar...
//@Class(enableJavaBeanProperties = true)
//<T> so we can specialize paa et tidspunkt... Eller faa SpecializedInvoker paa et senere tidspunkt
//Invoker extends SpecializedInvoker<Object>
//Maaske laver vi det til en inline klasse...
//Er jo saaden set en special version af InvocationTemplate der ikke tager nogle parameter.
interface SpecializedInvoker<R> {

    /**
     * Invokes the underlying executable.
     * 
     * @return the result
     * @throws Throwable
     *             anything thrown by the underlying executable propagates unchanged through the method handle call
     */
    @Nullable
    R invoke() throws Throwable;

    /**
     * Returns a new parameter-less method handle. The return type of the method handle will be the exact return type of the
     * underlying executable.
     * 
     * @return the method handle
     */
    MethodHandle toMethodHandle();
}

interface SpecializedTemplateInvoker<R> {

}