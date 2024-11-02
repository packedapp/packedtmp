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
package app.packed.runtime.errorhandling;

/**
 *
 */
public class Tester {

    public static void main(String[] args) {
    }

    public static <T extends Throwable> double calc(int f, ErrorProcessor<T> ep) throws T {
        if (f < 0) {
            throw ep.onError("f must be positive");
        }
        return 3.0 / f;
    }
}
