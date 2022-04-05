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
package app.packed.thread;

/**
 *
 */
// Vi bliver noedt til at skelne mellem single threaded og multi threaded
// Maaske endda paa et niveau hvor det er forskellige typer.

// Vi bliver noedt til at kunne styre detalje niveau...
// Maaske med String tags?? Eller andet.. Nok snare en Tag klasse?

// IncludeThreadInfo, IncludeInterceptors,

// Include Instantiations by Packed....!>!>! Maybe too much

// Only show Instantiations... :)
// Only Show Threads spawning
// ect..
public interface ActivityPlan {

}
//What happens when I call
///foo/ddd?sdsd=sdsd
//
//--> CheckSession
//--> CheckSecurity
//--> FilterStuff
//--> Instantiates Foo
//--> Instantiates Foo
//--> Spawn New thread
//--> new Fiber()
//
//ActivityPlan