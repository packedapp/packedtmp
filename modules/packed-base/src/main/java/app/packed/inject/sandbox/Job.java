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
package app.packed.inject.sandbox;

/**
 *
 */
// Er det en speciel shell?? Ja det ville jeg mene

// Altsaa det giver sjaeldent mening at lave den direkte 
//Is not a system namespace by default..
//What about shutdown... I don't think it is active...
interface Job<R> {

    // Job<Void> of()
}
// A job consists of 1 or more tasks ordered in a tree...
// But why not use the Component API...

// Basically it is the job-class that is the root 
interface Task {
    
}
