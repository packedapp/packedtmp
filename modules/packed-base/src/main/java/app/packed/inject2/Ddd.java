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
package app.packed.inject2;

import app.packed.inject.InjectorContract;
import app.packed.inject.InjectorContract.Builder;

/**
 *
 */
public class Ddd {

    public static void mainx(String[] args) {
        Builder b = InjectorContract.builder();
        b.addOptional(String.class);
        b.addRequires(String.class);
        b.build();
    }

    public static void main(String[] args) {
        InjectorContract.Builder b = InjectorContract.builder();
        b.addOptional(String.class);
        b.addRequires(String.class);
        b.build();
        // ServicesContract.Builder b = ServicesContract.Builder;ddd
    }

    // HelpInjection
    // Rebinding of services.
    // I think service interception across containers...

    // For example, let us introduce a delay...... Lets try and serialize all arguments and return types...

}
