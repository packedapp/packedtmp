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
package tck;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class Invoker {

    private final String name;
    private final MethodHandle methodHandle;
    private final Object[] args;

    Invoker(String name, MethodHandle methodHandle, Object[] args) {
        this.name = name;
        this.methodHandle = methodHandle;
        this.args = args;
    }

    public Invoker raw() {
        return new Invoker(name, methodHandle, new Object[] {});
    }

    public String name() {
        return name;
    }

    public MethodType type() {
        return methodHandle.type();
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(Object... args) throws Throwable {
        ArrayList<Object> l = new ArrayList<>(List.of(this.args));
        l.addAll(List.of(args));
        return (T) methodHandle.invokeWithArguments(l);
    }
}
