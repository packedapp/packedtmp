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
package app.packed.bean.hooks.scrap;

/**
 *
 */
// Den fungere jo kun paa Class/Member hook taenker jeg
public enum HookTarget {
    BUILD_TIME_COMPONENT_SOURCE,

    RUNTIME_COMPONENT_SOURCE,

    HOOK;
}

class FooBootstrap {

    // Saa kan man seatte buildtime/runtime target efter det
    // Det betyder ogsaa at vi koere en bootstrap per target...
    // Det betyder ogsaa at
    public final HookTarget target() {
        return HookTarget.BUILD_TIME_COMPONENT_SOURCE;
    }
    
    protected static final void $target(HookTarget... targets) {
        // default is BUILD_TIME_COMPONENT_SOURCE
        
        // Alternativet er at smide det paa MethodHook...
        // Men er sgu lidt grimt...
    }
}