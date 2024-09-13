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

// AssemblyTransformer

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//@NotHookable <-- You cannot change this in build hooks. Its fixed
// Maybe you can change it with a Lookup object
public @interface AssemblySecurity {

    // The class must be open to the framework and have
    Class<? extends AssemblySecurity.Model> value();

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

    // Do we have one both for observing and transforming???
    // Ignore on Classpath
    public enum A {

        // Transforming Buildhooks can never be applied (do we allow observing)
        NEVER,

        // HOOKS applied from the same module (does not propopate to other module
        SAME_MODULE,

        // ANYONE who has the instance
        OPEN,

        // A list of named modules
        LIST,

        // Classpath can never apply hooks on a module assembly
        // And can trivially apply on other classpath assemblies

        NONE, ALL, MODULES // And then a String. Fx har vi en BaseAppAssembly
    }

    public class AllOpen {}

    // Default model is INSTANCE <- Meaning if you have an instance you can hook into it
    // Vi kan jo ikke sige at det kun er parent der kan apply hooks paa deres boern...
    // Det maa jo noedvendig ogsaa vaere parenten selv
    public class Default extends Model {
        public static Default INSTANCE = new Default();

        /** {@inheritDoc} */
        @Override
        protected void define() {}
    }
}

/**
 * Tror det er her vi define en code security model
 */
// Er det udelukkede build hooks???
// Eller er der andet der bliver styret her.
// Umiddelbart taenker jeg kun build hooks,

// Not Metaable. Syntes kun man skal bruge denne annotering
// Annoteringen kan ikke transformeres (Maaske har vi en meta annotering for det??)

// Maaske er default STRICT?
// Maaske definere Assembly den her
// Maaske definere Consumer ANYTHING_GOES

// Tror maaske vi kan bruge den andre steder (Policy.PARANOID) Saa kan en extension sige hov, vi skal fx have SSL enabled IDK

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

// Replace by AssemblySecurityModel
@interface AllowBuildHooksFromParentAssembly {

    WhatIsAllowed policy() default WhatIsAllowed.STRICT;

    // calling modules (module system is always checked), but these will also be allowed
    String[] additionalModules() default {};

    public enum WhatIsAllowed {

        // Strictly checked accordingly to the module system
        STRICT,

        // Parent is allowed to do anything. This means the assembly instances should be guarded
        PARENT_OWNER,

        // Anyone can place build hooks
        ANYTHING_GOES;
    }
}