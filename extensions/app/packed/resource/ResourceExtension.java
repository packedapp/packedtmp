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
package app.packed.resource;

import app.packed.container.Extension;

/**
 *
 */
public class ResourceExtension extends Extension<ResourceExtension> {

    // Hmm altsaa Man gemmer det jo tit i en statisk felt. F.eks. I18N beskeder...
    // Det gider man ikke saette op hver gang.
    // Se f.eks. jdk.incubator.jpackage.main.Main
    
}
// Ideen er at folk vil loaded resources fra classpath...

// skal have noget interception, listeners, event handlers, ect...

// skal kunne fungere baade paa classpath og modulepath...

// https://stackoverflow.com/questions/27845223/whats-the-difference-between-a-resource-uri-url-path-and-file-in-java/27845506#27845506

// https://stackoverflow.com/questions/27845223/whats-the-difference-between-a-resource-uri-url-path-and-file-in-java

// https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html