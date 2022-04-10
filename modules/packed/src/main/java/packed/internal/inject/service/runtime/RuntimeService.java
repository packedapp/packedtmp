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
package packed.internal.inject.service.runtime;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import app.packed.base.Key;
import packed.internal.inject.service.InternalService;

/** Represents a service at runtime. */
public non-sealed interface RuntimeService extends InternalService {

    @Override
    default <T> InternalService decorate(Function<? super T, ? extends T> decoratingFunction) {
        throw new UnsupportedOperationException();
    }

    // We need this to adapt to build time transformations
    public abstract MethodHandle dependencyAccessor();

    /**
     * Returns an instance.
     * 
     * @param request
     *            a request if needed by {@link #requiresProvisionContext()}
     * @return the instance
     */
    public abstract Object provideInstance();

    @Override
    default InternalService rekeyAs(Key<?> key) {
        return new DelegatingRuntimeService(key, this);
    }

    public abstract boolean requiresProvisionContext();

    public static String toString(RuntimeService rs) {
        StringBuilder sb = new StringBuilder();
        sb.append(rs.key());
        sb.append("[constant=").append(rs.isConstant()).append(']');
        return sb.toString();
    }
    
    static RuntimeService constant(Key<?> key, Object constant) {
        return new ConstantRuntimeService(key, constant);
    }
}
