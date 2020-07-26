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
package old.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public abstract class CyclicCheckingClassValue<T> extends ClassValue<T> {

    private final ReentrantLock lock = new ReentrantLock();

    private final ThreadLocal<LinkedHashSet<Class<?>>> stack = ThreadLocal.withInitial(() -> new LinkedHashSet<>());

    @Override
    protected final T computeValue(Class<?> type) {
        lock.lock();
        try {
            LinkedHashSet<Class<?>> s = stack.get();
            if (s.contains(type)) {
                cycleDetected(List.copyOf(s));
            }
            return computeValue0(type);
        } finally {
            lock.unlock();
        }
    }

    protected abstract T computeValue0(Class<?> type);

    protected abstract void cycleDetected(List<Class<?>> cycle);
}
