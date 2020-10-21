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
package app.packed.inject.sandbox;

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;
import java.util.function.Function;

import packed.internal.util.LookupUtil;

/**
 *
 */
public final class InjectionModel {

    /** A method handle for {@link Function#apply(Object)}. */
    private static final MethodHandle ACCEPT = LookupUtil.lookupVirtualPublic(Consumer.class, "accept", void.class, Object.class);

    InjectionModel DEFAULT = new InjectionModel();
    InjectionModel SIMPLE = new InjectionModel();

    public static void main(String[] args) {
        Consumer<?> c = e -> System.out.println(e);
        MethodHandle mh = ACCEPT.bindTo(c);

        System.out.println(mh);

    }
}
