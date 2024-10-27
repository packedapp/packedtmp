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
package app.packed.bean.lifecycle;

import java.util.concurrent.Callable;

import app.packed.bean.scanning.BeanTrigger.OnExtensionServiceBeanTrigger;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;

/** A context object that can be injected into methods annotated with {@link OnStart}. */
@OnExtensionServiceBeanTrigger(extension = BaseExtension.class, requiresContext = StartContext.class)
public interface StartContext extends Context<BaseExtension>{

    // Maaske har vi i virkeligheden two scopes her???

    // et "parent" lifetime scope    (Before the lifetime, for examples, before the application starts)
    // og en "Bean" lifecycle scope  (Before all dendencies)

    void fork(Callable<?> callable); // what about when to join?

    void fork(Runnable runnable); // what about when to join?

    // Will keep running until it is finished
    void forkNoAwait(Runnable runnable); // what about when to join?

    // Kan man lukke ned normalt under start?
    // Eller er det altid en cancel
    void fail(Throwable cause); //hvorfor ikke bare smide den...

    // StartReason?

}

//Eller har vi noget generisk AsyncDependecyContext???
//Det er jo i virkeligheden en dependency graf...
//TreeDependencyExecutionContext
//Som maaske kan bruges til nogle smart async beregninger...

//I virkeligheden er det jo et trae.. Som vi vil gerne vil kunne bevaege os op ad og hooke ind i.
//Men ikke noedvendig vis aendre.
//Vil gerne kunne sige baade naar beanen er faerdig.
//Og hvis applikationen bliver afinstalleret.

//Skal kunne forke ting.
//Kunne awaite ting
//Kunne faile
//BeanStartContext (saa kan den lidt mere tydeligt bliver brugt af andre annoteringen)