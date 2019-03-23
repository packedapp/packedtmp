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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import packed.internal.classscan.DescriptorFactory;
import packed.internal.inject.builder.InjectorBuilder;

/**
 * A context object that is available via {@link Bundle#context} from within the invocation of
 * {@link Bundle#configure()}.
 */

// Per app or per container. Maybe two?? Tror vi skal have 2 saa vi har konteksten
// BuildAppContext Bundle.app()
// BuildContainerContext Bundle.container()
public abstract class ContainerBuildContext {

    public final void configure(Bundle b) {
        requireNonNull(b);
        b.context = this;
        try {
            b.configure();
        } finally {
            b.context = null;
        }
    }

    public final boolean freezeConfiguration() {
        // maaske alligevel freezeBundleConfiguration()
        // Kan jo intet naar den foerst er frosset
        return false;
    }

    /** {@inheritDoc} */
    final void lookup(Lookup lookup) {
        with(InjectorBuilder.class).accessor = DescriptorFactory.get(lookup);
    }

    /**
     * Returns a configuration class of the specified type.
     * <p>
     * This method only supports exact matches, {@code wrap(Service.class)} must not return {@code ServiceImpl.class}.
     * 
     * @param <T>
     *            stuff
     * @param type
     *            the type of configuration class
     * @return stuff
     * @throws UnsupportedOperationException
     *             if the specified configuration type is not supported
     */
    public <T> T with(Class<? super T> type) {
        throw new UnsupportedOperationException("Does not support " + type);
    }
}
