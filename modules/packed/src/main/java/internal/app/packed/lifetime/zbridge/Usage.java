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
package internal.app.packed.lifetime.zbridge;

import java.lang.invoke.MethodHandles;

import app.packed.operation.Op1;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class Usage {

    public static void main(String[] args) {
        PackedBridge<MyE> b = PackedBridge.builder(MethodHandles.lookup(), MyE.class);

        b.provideGeneratedConstant(String.class, e -> e.foo);

        // Bliver noedt til at blive resolvet mod guest containeren
        b.provide(MyE.RuntimeBean.class, new Op1<MyE.RuntimeBean, ServiceLocator>(e -> e.calc()) {});

//        b.provide(MyE.RuntimeBean.class, new Op1<@CodeGenerated String, ServiceLocator>(e -> e.calc()) {});

    }
}
