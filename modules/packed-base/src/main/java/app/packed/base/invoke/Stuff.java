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
package app.packed.base.invoke;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import app.packed.base.TypeLiteral;

/**
 *
 */
public class Stuff {

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
        var t1 = new TypeLiteral<Class>() {};
        var t2 = new TypeLiteral<Class<?>>() {};

        System.out.println(t1);
        System.out.println(t2);
    }
}

//Always only available at runtime
@Target(ElementType.TYPE)
@interface InjectInvoker {
    Class<?> type();

    Class<?>[] typeParameters() default {}; // if left empty = Lower bound everywhere. If filled out. All type parameters must be filled out

    Class<?>[] typeTemplates() default {}; //
}
// @InjectInvoker(type = MethodHandle.class) <--- will be bound to any component instance. 
// @InjectInvoker(type = Invoker1.class, typeParameters=SomeTemplate.class, typeTemplates = SomeTemplate.class). not a big fan...

//Many, so we can repeat it
//If Getter+ Setter??? No I think
//We will make this automatically for a JavaBeanProperty sidecar...
// @Class(enableJavaBeanProperties = true)