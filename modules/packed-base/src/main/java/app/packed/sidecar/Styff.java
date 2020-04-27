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
package app.packed.sidecar;

/**
 *
 */
public class Styff {

    final String x;
    {
        System.out.println("What");
        foo();
        x = "dsd";
    }

    Styff() {
        System.out.println("Lol");
    }

    Styff(String o) {
        System.out.println("Lol");
        System.out.println(x);
    }

    private void foo() {
        System.out.println("Nam nam");
    }

    public static void main(String[] args) {
        new Styff("ff");
    }
}
