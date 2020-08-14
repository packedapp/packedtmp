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
package zamples;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 *
 */

// Problemet med at have en main init.
// Er hvis vi samtidig har et image i et static field...
// Saa afhaenger initialisering af felt raekkefolgen.

public class MainInit {

    final static StackWalker st = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    static {
        System.out.println(st);
    }

    public static void main(String[] args) throws InterruptedException {
        sss();
        Executors.newCachedThreadPool().execute(() -> sss());
        // sss();
        Thread.sleep(1000);
        System.runFinalization();
        Thread.sleep(1000);
        // java.lang.Object

        for (var t : Thread.getAllStackTraces().values()) {
            System.out.println();
            System.out.println(Arrays.asList(t));
            if (t.length > 0) {
                System.out.println(t[t.length - 1].getClassName());
            }
        }

    }

    public static void sss() {
        StackFrame sf = st.walk(s -> s.reduce((a, b) -> b).orElse(null));
        System.out.println(sf);
    }
}
