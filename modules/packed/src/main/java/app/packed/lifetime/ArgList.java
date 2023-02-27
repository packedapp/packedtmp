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
package app.packed.lifetime;

/**
 *
 */
//En wrapper omkring et String arrays
//Alle skal kunne faa den injected.
//Paanaer non- entrypoint controlling extensions.
//Som jeg vil mene ikke har adgang til den

// was StringArgs
// MainArgs... Men det er vel ikke kun Main den virker sammen med?
// Eller maaske er det. Nej fordi vi vil gerne kunne bruge
// ApplicationImage.use("fsdfsdf") <- uaghaendig af extension
// Det kan vi vel egentlig ogsaa...
public /* primitive */ class ArgList {
    private final String[] args;

    ArgList(String[] args) {
        this.args = args;
    }

    public int argumentCount() {
        return args.length;
    }
}
