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
package packed.internal.sidecar;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.inject.Provide;
import app.packed.sidecar.MethodSidecar;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.util.LookupUtil;

/** A model of a {@link MethodSidecar}. */
public final class MethodSidecarModel extends SidecarModel<MethodSidecar> {

    /** A VarHandle that can access MethodSidecar#configuration. */
    private static final VarHandle VH_METHOD_SIDECAR_CONFIGURATION = LookupUtil.vhPrivateOther(MethodHandles.lookup(), MethodSidecar.class, "configuration",
            MethodSidecarConfiguration.class);

    /** A MethodHandle that can invoke MethodSidecar#configure. */
    private static final MethodHandle MH_METHOD_SIDECAR_CONFIGURE = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), MethodSidecar.class, "configure",
            void.class);

    public final Map<Key<?>, MethodHandle> keys;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder
     */
    private MethodSidecarModel(Builder builder) {
        super(builder);
        this.keys = builder.map.size() == 0 ? null : Map.copyOf(builder.map);
    }

    /** {@inheritDoc} */
    @Override
    public SidecarType type() {
        return SidecarType.METHOD;
    }

    /** A builder for method sidecar. */
    final static class Builder extends SidecarModel.Builder<MethodSidecar, MethodSidecarConfiguration> {

        private final HashMap<Key<?>, MethodHandle> map = new HashMap<>();

        Builder(Class<?> implementation) {
            super(VH_METHOD_SIDECAR_CONFIGURATION, MH_METHOD_SIDECAR_CONFIGURE, implementation, new MethodSidecarConfiguration());
        }

        /** {@inheritDoc} */
        @Override
        protected MethodSidecarModel build() {
            ib.oc().findMethods(m -> {
                Provide ap = m.getAnnotation(Provide.class);
                if (ap != null) {
                    if (!Modifier.isStatic(m.getModifiers())) {
                        throw new IllegalStateException("Methods annotated with @Provide must be static, method = " + m);
                    }
                    Key<?> k = Key.fromMethodReturnType(m);
                    MethodHandle mh = ib.oc().unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    map.put(k, mh);
                }
            });

            return new MethodSidecarModel(this);
        }
    }

    /** A configuration object we provide to MethodSidecar. */
    public final static class MethodSidecarConfiguration {

        boolean debug;

        public void debug() {
            System.out.println("DEBUG DU ER SEJ");
            this.debug = true;
        }
    }
}