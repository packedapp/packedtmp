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
package app.packed.component;

/**
 *
 */
//Ideen er at vi kan koere ting baade foer eller efter boern....
//Default er selvfoelgelig foer for initialize+Startup
// Og Efter for stop... 

//Men vi kan f.eks. stoppe med at acceptere nye forbindelser i en Stop_PreOrder
// Og saa cleanup og await i en postOrder...

//TreeTraversel...
public enum ComponentTraversalOrder {
    PRE_ORDER, POST_ORDER;
}

// Webserver is one child
// WebApp is another
// Vi vil i virkeligheden gerne have dependency graph TraverselOrder
// ListenSocket -> PostOrder in DependencyGraphTraversel

// AnyOrder???? We don't care about when its done...
// What about dependencies....
// I virkeligheden er det maaske mere det vi skal bruge..... :( 

/////// Why not InOrder
//ComponentTraversalOrder (Og saa droppe In_ORDER)
// Don't know about IN_ORDER
// Also this should be generic... I mean, we are going to use tree's other places I think...
// Kan ikke hvad vi skal bruge in_order til.
// Lad os sige vi har en node 3 boern og saa koere In_order.
// Nu laver vi saa en folder og putter de tre boern derind.
// Hvad saa med InOrder?? Nu er den jo helt anderledes...

// Og hvad hvis vi kun har et barn??? For eller efter???
//

//https://en.wikipedia.org/wiki/Tree_traversal