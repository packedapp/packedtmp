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
package packed.internal.inject;

import app.packed.base.Nullable;
import app.packed.inject.IFactory;

/**
 *
 */
public abstract class AbstractFactory<T> implements IFactory<T> {

    /** {@inheritDoc} */
    @Override
    public final <S> IFactory<T> bind(Class<S> key, @Nullable S instance) {

        // Do we allow binding non-matching keys???
        // Could be useful from Prime annotations...

        // Tror vi skal have to forskellige

        // bindParameter(int index).... retains index....
        // Throws

        // bindWithKey();

        // bindRaw(); <---- Only takes a class, ignores nullable.....

        // Hvordan klarer vi Foo(String firstName, String lastName)...
        // Eller

        // Hvordan klarer vi Foo(String firstName, SomeComposite sc)...

        // Det eneste der er forskel er parameter index'et...
        // Maaske bliver man bare noedt til at lave en statisk metoder....

        // Skal vi have en speciel MemberFactory?????

        //

        // bindTo? Det er jo ikke et argument hvis det f.eks. er et field...

        // resolveDependency()...
        // Its not really an argument its a dependency that we resolve...

        // withArgumentSupplier
        throw new UnsupportedOperationException();
    }
}
