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
package app.packed.hook;

/**
 *
 */

// Bliver brugt paa runtime Services
// onFoo(Runnable, Component c, ComponentConfiguration cc)
// First parameter must be a result of the a MethodOperator
public @interface OnHookReady {

    // Den skal kun bruges hvis man har hooks deres producere den samme vaerdi for den samme extension (eller uden
    // extensions)
    String value() default "default";
}

// Vi loeser den forst, for Extension+Runtime, saa kan vi evt. kigge paa Runtime+Runtime bagefter

// ComponentExtension.register(ComponentConfiguration, Applicator<?>)
// ServiceExtension.register(ComponentConfiguration, ServiceApplicator<?>)

// ServiceApplicator og generisk Applicator er separater.
// Generics Applicator er du styr din egen injection.
// Service Applicator kan du injecte stuff
