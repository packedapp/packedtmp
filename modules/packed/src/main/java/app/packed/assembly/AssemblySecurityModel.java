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
package app.packed.assembly;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.TYPE )
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//@NotHookable <-- You cannot change this in build hooks. Its fixed
// Maybe you can change it with a Lookup object
public @interface AssemblySecurityModel {

    // The class must be open to the framework and have
    Class<? extends AssemblySecurityModel.Model> value();

    public interface Descriptor {

        static Descriptor of(Model model) {
            throw new UnsupportedOperationException();
        }
    }

    // Alternativ er en enum. Eller maaske kan det hele vaere attributer i annotatering AssemblySecurityModel
    /**
     * Subclasses must define an INSTANCE field
     */
    public abstract class Model {
        // Restrict Launching to these classes (default any launcher)

        // Podable <- Can we override Logging, Web, ect.

        // Buildhook handling

        //
        protected abstract void define();
    }

    public class AllOpen {}
    public class Default extends Model {
        public static Default INSTANCE = new Default();

        /** {@inheritDoc} */
        @Override
        protected void define() {}
    }
}
