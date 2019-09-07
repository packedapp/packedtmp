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
package packed.internal.container.model;

import app.packed.artifact.ArtifactImage;
import app.packed.container.BaseBundle;
import app.packed.container.Bundle;
import app.packed.inject.InjectorContract;

/**
 *
 */
public class Test2 {

    static Bundle b() {
        return new BaseBundle() {
            @Override
            protected void configure() {
                provide("foob");
                provide(-123L);
                provide((short) -123L);
                exportAll();
            }
        };
    }

    public static void main(String[] args) {
        System.out.println(InjectorContract.of(b()));

        ArtifactImage ai = ArtifactImage.of(b());

        System.out.println(InjectorContract.of(ai));
    }
}
