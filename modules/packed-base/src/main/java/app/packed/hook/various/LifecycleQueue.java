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
package app.packed.hook.various;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.OnHook;

/**
 *
 */
// ideen er at man kan koere component.lifecycle.initialization.beforeTransitioning().add(()->sysout("everything set to
// go");
// Bedre end en annoteringen, fordi man kan saette det for hosted apps ogsaa...
// initialization()
public interface LifecycleQueue {

    // boolean = weather or not it was added, for example, if we are already shutdown....

    boolean add(Runnable r);

    boolean add(CompletableFuture<?> f);

    // Ideen er man kan have en metode, via et hook, der kalder ind f.eks. onStart()....
    // @XXX
    // Secret er bare et objekt, saa vi f.eks. sikre os der kun er et callback af et specifik type....

    boolean addIfAbsent(Secret secret, Runnable r);/// Hvis man er i gang med at processere secret'en saa bliver den tilfoejet igen..... Eller maaske fejler den
                                                   /// StackOverflow...

    interface Secret {}

    static class PreloadCache {
        static final Object SECRET = new Object();

        ConcurrentMap<URL, String> cached;

        ConcurrentMap<URL, String> outstanding;

        @OnHook
        public void preload(AnnotatedTypeHook<Preload> p) {
            // for (String s : p.annotation().values()) {
            // // async preload into cache
            // }
            // p.container.beforeTransitioningToRunningQueue(SECRET, checkOutstanding is empty)
        }
    }

    @interface Preload {
        String[] values();
    }
}
