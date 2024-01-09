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
package app.packed.lifetime;

import app.packed.context.Context;
import app.packed.extension.BaseExtension;

/**
 *
 */

// Skal kunne forke ting.
// Kunne awaite ting
// Kunne faile
// OnStartContext???
// BeanStartContext (saa kan den lidt mere tydeligt bliver brugt af andre annoteringen)
public interface OnStartContext extends Context<BaseExtension>{

    void fork(Runnable runnable); // what about when to join?

    // Kan man lukke ned normalt under start?
    // Eller er det altid en cancel
    void fail(Throwable cause); //hvorfor ikke bare smide den...
}
