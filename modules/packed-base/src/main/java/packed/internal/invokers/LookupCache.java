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
package packed.internal.invokers;

import java.lang.invoke.MethodHandles.Lookup;

/**
 *
 */
public class LookupCache<T> {

    // Problemet er at vi ikke har adgang til lookup objektet....
    // Maybe ThreadLocal

    private static final Entry[] ENTRIES;

    static {
        ENTRIES = new Entry[64];
        for (int i = 0; i < ENTRIES.length; i++) {
            ENTRIES[i] = new Entry();
        }
    }

    public T get(Lookup l) {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        System.out.println(Lookup.UNCONDITIONAL);
        System.out.println(Lookup.PUBLIC | Lookup.PRIVATE | Lookup.PROTECTED | Lookup.PACKAGE | Lookup.MODULE | Lookup.UNCONDITIONAL);
    }

    static class Entry extends ClassValue<LookupDescriptorAccessor> {

        /** {@inheritDoc} */
        @Override
        protected LookupDescriptorAccessor computeValue(Class<?> type) {
            return null;
        }
    }

    final class LookupAccessFactory {

        // private static volatile X[] INFOS = new X[10];
        //
        // VarHandle[] fieldHandles;
        //
        // MethodHandle[] methodHandles;
        //
        // public static LookupAccessor get(MethodHandles.Lookup l) {
        // X x = INFOS[l.lookupModes()];
        //
        // return x.get(l.lookupClass());
        // }
        //
        // static class X extends ClassValue<LookupAccessor> {
        //
        // /** {@inheritDoc} */
        // @Override
        // protected LookupAccessor computeValue(Class<?> type) {
        // return null;
        // }
        // }
    }

}
