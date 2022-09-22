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
package app.packed.lifetime.sandbox;

/**
 *
 */
// Hvordan er en lifetime managed
// Er i virkeligheden LifetimeKind og saa bare Managed splittet op.
// Jeg ved ikke om vi vil beholde den her klasse, smide den ind under lifetime kind
// Eller noget helt 3. Har den bare med for at kunne summe lidt over den
public enum LifetimeManagementKind {
    Stateless,  //Stregn taget er der vel ikke model her... Der er ingen lifetimes
    
    Unmanaged,
    
    Managed_Async, // Start + Stop seperately
    
    Managed_sync // One LifetimeOperation
}