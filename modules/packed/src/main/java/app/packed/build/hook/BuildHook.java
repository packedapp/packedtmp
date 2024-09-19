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
package app.packed.build.hook;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.application.ApplicationBuildHook;
import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyBuildHook;
import app.packed.assembly.AssemblyPropagator;
import app.packed.bean.BeanBuildHook;
import app.packed.build.BuildCodeSource;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerBuildHook;
import app.packed.operation.OperationBuildHook;

/**
 * Build hooks are used for ...
 *
 * <p>
 * Build hooks can be applied in two different ways. Either by using {@link ApplyBuildHook} on an assembly or bean
 * class. Or by using {@link #apply(Assembly, Consumer)} on an Assembly instance.
 * <p>
 * {@link #apply(Assembly, Consumer)} supports recursively applying build hooks. Use them sparingly
 * <p>
 * Build hooks comes in two shapes:
 *
 * Observing build hooks. Does not modify the application being build in any way. But just observes how it is being
 * built
 *
 * Transforming build hooks. Modifies the application in such a way that the application is different to what the
 * application was without the build hook
 * <p>
 *
 * A build transformer should be safe for multi threaded access.
 * <p>
 * Build hooks are typically stateless. Use the various types of {@link app.packed.build.BuildLocal build locals} to
 * store state across multiple invocations of the same hook.
 */
// Abstract class because we do not want a build hook to do multiple things

public sealed abstract class BuildHook implements BuildCodeSource
        permits ApplicationBuildHook, AssemblyBuildHook, ContainerBuildHook, BeanBuildHook, OperationBuildHook {

    // If the same (.getClass()) build hook is placed on annotation ignore it
    // other.getClass() == this.getClass()
    public boolean ignoreDuplicate(BuildHook other) {
        return true; // Maybe false or maybe True/False/Fail/Warn
    }

    public Set<String> tags() {
        return Set.of();
    }

    // Naah vi kalder ind paa noget andet
    public static void checkTransforming() {}

    /**
     *
     * <p>
     * If no build hooks are applied, the specified assembly will be returned
     *
     * @param assembly
     *            the assembly to apply build hooks to
     * @param transformer
     *            a transformer that is responsible for applyi
     * @return
     */
    public static Assembly apply(Assembly assembly, Consumer<? super Applicator> transformer) {
        throw new UnsupportedOperationException();
    }

    // Wirelet transformers are run before any other wirelets
//    @SuppressWarnings("exports")
//    public static TransformingWirelet applyWirelet(Consumer<? super Applicator> transformer) {
//        throw new UnsupportedOperationException();
//    }

    // Okay vi har et BuildHook fra X Extension.
    // I build hooket gemmer vi AssemblyConfiguration i en ThreadLocal
    // Den hiver saa ud i Extension.onAppFinished og goer et eller andet med.
    // Saa vil den checke extension permissions naar vi kalder en pa AssemblyConfiguration
    // Og ikke build hook. Men det er maaske ogsaa fint.

    public interface Applicator {
        // Normally the build hooks are inserted first
        // IDK, firstOccurence?
        Applicator applyBefore(Class<? extends BuildHook> bh);

        Applicator applyLast();

        // Den eneste forskel er jo en marker i vores BuildHookMirror...
        // Vi checker altid det samme
        void observe(BuildHook... hooks);

        void transform(BuildHook... hooks);

        Applicator traverse(AssemblyPropagator ap);

        Applicator traverseRecursively();

        // I think if you want them parallel, you probably need to write them yourself.
        // assemblies.streamCompo
        // Maaske supporter vi kun base componenter...
        // Assembly can ikke være et child af container, jo maaske er det kun et child for root containeren i assemblyen

        <T extends ComponentMirror> Applicator verify(Class<T> mirrorType, Consumer<? super T> verifier);

    }

    // Wirelet for build hooks are a bad idea.
    // Any extension can return them together with their own wirelets. Thereby introspecting the whole appplication
    // Do not support it. It is to easy to misue, somewhere some extensino har going to return one
//    static class TransformingWirelet extends Wirelet {
//        public Module module() {
//            throw new UnsupportedOperationException();
//        }
//        // Information about owner
//    }
}

//Skal nok tilfoeje Namespace, hvad med Service????
class Zandbox {
    // ignore transformers with the same class (and tags??)
    // Hmm hvis man annotere en Assembly, saa laver man jo ikke recursively as default
    // Ved ikke om en boolean er nok? eller vi har en decideret strategi

    // was allowMultiple. But
    // Override existing tror jeg er bedre. Fordi saa er det klart at det kun er den forste hvor metoden bliver kaldt
    boolean overrideSameClass() {
        return true;
    }

    // Ideen er at vi kan bruge dem til fx at styre rækkefølgen

}

// Application