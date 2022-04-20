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
package app.packed.bean.operation.usage;

import app.packed.bean.operation.OperationMirror;

/**
 *
 */
// A lifecycle operation 
// Base/Common Lifecycle operation?
public abstract class LifecycleActionMirror extends OperationMirror {

    // Er en del af en liste eller en graf.
}

// Har vi en speciel BeanOperationSelection ting som kan definere en task graph???
// Taenker altsaa det er noget generisk noget


//// Well should be optional... But probably not the right place to model it.
//// Nej isaer nu ikke naar fx ConfigConsume er en del af en lifecycle
//public abstract LifecycleActionMirror previous();
//
//public abstract LifecycleActionMirror next();
