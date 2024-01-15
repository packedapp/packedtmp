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
package app.packed.build;

/**
 *
 */
// BuildUnit allows asynchronously build of multiple applications

// Er ordnet i et trae.
// Hvor en node bygges foerst og saa kan vi queue new build units naar vi bygger noden.
// Disse kan foerst startes naar vi er faerdige med noden.
// Fordi vi har nogle data structure som vi maaske skal ned og pille i.

// BuildTask

// Minimum units bliver noedt til at vaere applications.
// Ellers kan vi ikke have et extension tree for en enkelt application.
// Vi kan ikke have at to traade lige pludselig piller ved et eller andet samtidigt

// When you have build unit boundary.
// You specify the contract of the child.
// Otherwise the parent does not know what the child looks like.
public class BuildUnitMirror {

}
