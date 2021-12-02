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
package app.packed.job;

import app.packed.container.Assembly;
import app.packed.container.BaseAssembly;

/**
 *
 */
interface JobScheduler {
    <S, T extends Assembly  & ResultBearing<S>> Job<S> schedule(T assembly);

    <T> Job<T> schedule(JobAssembly<T> assembly);

}

class Test extends BaseAssembly implements ResultBearing<String> {

    @Override
    protected void build() {}

    public static void foo(JobScheduler js) {
        Job<String> j = js.schedule(new Test());

        System.out.println(j);

    }

}

class Test2 extends JobAssembly<String> {

    @Override
    protected void build() {
        simpleComputable(() -> "Qwe");
        
        // Ideen er man kan registrere jobbet som en service...
        provideJobLauncher(new Test2());
    }

    public static void foo(JobScheduler js) {
        Job<String> j = js.schedule(new Test2());

        System.out.println(j);

    }

}