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
package packed.internal.container.extension.stuff;

/**
 *
 */
public class SomeBuilder implements CommonInterface {

    public void instantiate(PkdInstantiator i) {
        CommonInterface ci = i.get(CIBuilderPipeline.class);
        if (ci == null) {
            ci = this;
        }

        // do stuff...
    }

    // Okay builder refac...
    // Pipeline skal have en simple constructor der tager en extension
    // Som skal vaere lokaliseret i samme pakke..

    // Alle extension's wirelets som matcher den samme <T> type. bliver smidt i den samme pipeline.

    // Runtime'en kan decorere pipelinen, med forskellige ting, som f.eks. om stack frame capturing er enabled.

    // Problemet er nok at
    // ExtensionWiring ogsaa er en abstract classe..
    //

}
