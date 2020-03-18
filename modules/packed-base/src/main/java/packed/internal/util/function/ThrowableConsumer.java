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
package packed.internal.util.function;

import java.io.IOException;

import packed.internal.reflect.typevariable.TypeVariableExtractor;

/**
 *
 */
public interface ThrowableConsumer<E, T extends Throwable> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t
     *            the input argument
     */
    void accept(E t) throws T;

    public static void main(String[] args) throws Exception {
        App2.executeThrowing(c -> {
            if (c == "123") {
                throw new IOException();
            } else {
                throw new InterruptedException();
            }
        });
    }
}

class App2 {

    public static <T extends Throwable> void executeThrowing(ThrowableConsumer<String, T> c) throws T {
        try {
            // Den virker ikke med interfaces....
            TypeVariableExtractor tve = TypeVariableExtractor.of(ThrowableConsumer.class, 1);
            System.out.println(tve.extract(c.getClass()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Stuff {

    void foo() throws Exception {
        App2.executeThrowing(c -> {
            if (c == "123") {
                throw new IOException();
            } else {
                throw new InterruptedException();
            }
        });
    }
}