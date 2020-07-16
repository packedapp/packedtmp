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
package app.packed.artifact;

import app.packed.container.Wirelet;

/**
 *
 */
// Contains

// Ways to initialize, start, stop, execute, ect...
// Ways to query the image... in the same way as a Bundle...
//// Tror dog det betyder vi skal have noget a.la. 
// ArtifactImage -> SystemDescribable, saa metoder, f.eks.,
// ServiceContract.from(Image|new XBundle()); -> 
//// SystemInspector.find(iOrB, ServiceContract.class); <-- SC exposed as a contract

// Image<App> app = App.image(new MyApp());
// Image app = Image.of(new MyApp());

// App.driver().image(Bundle b);
public interface Image<T> {

    T initialize(Wirelet... wirelets);

    T start(Wirelet... wirelets);
}
// Problemet med Image er guest images..
// Og om en envelope for en artifact.