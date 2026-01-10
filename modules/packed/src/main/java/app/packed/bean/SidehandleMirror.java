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
package app.packed.bean;

import java.util.concurrent.atomic.AtomicLong;

import app.packed.service.Provide;

/**
 *
 */
// Was SidebeanUseSiteMirror
public interface SidehandleMirror {

    SidehandleTargetMirror target();

    SidehandleBeanMirror sidebean();
}

class Foo extends AtomicLong {

    private static final long serialVersionUID = 1L;

    // Alternativt implements Supplier?
    // Men hvis de andre skal implementere nogle provides kan vi jo ligesaa godt bruge dem.
    // Det eneste problemer med @Provide og lad os sige operation sidebeans. Er at det jo ikke
    // er noget vi kan provide paa request scope. Eftersom invoker er det eneste der har noget med request at goere
    // Maaske supportere vi det bare ikke

    // Men altsaa Variable provide er bare ikke super brugbart, Andet end til at gemme noget info
    // Jeg ser faktisk ikke at vi kan bruge den til noget rigtigt
    @Provide
    public long getIt() {
        return super.get();
    }
}