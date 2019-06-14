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
package packed.internal.xain;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.inject.InjectorExtension;
import app.packed.inject.Provide;
import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;
import packed.internal.annotations.AtProvidesGroup;

/**
 * A meta annotation that can be used on annotations to indicate that a specific should, if not already it, be installed
 * into the configuration of a container.
 */

public class A {

}
// The extension class must have a matching method for each annotation target
// @OnHook AnnotatedField<Provides>
// @OnHook AnnotatedMethod<Provides>
// I think we should require the methods....
// Default implementation can just ignore them
// We only support one extension here unlike requiresExtension...
// Because we need a single place to look for interesting methods....
/// @OnHookDiscovery er det jo..
// newMethodHandle...
// Kan andre metoder ikke bruge dem????

/// We want to cache method handle...
/// We want to cache the whole processing

// Maybe register callbacks that are run. Whenever such a class is encountered

// These are static methods. The callback should be Consumer<ServiceExtension>

// @RunAfter10Minutes

// Alternative we could make a visitor....
// Which could create an object....

/// Analyze.... Skal kun goeres en gang per object....
///// Og saa kan vi gemme det information vi har lyst til....

// @OnComponent(AtInjectGroup)

/// Extension + Attachment

// BiConsumer(Extension, Attachment)

abstract class ExtensionProcessor<T extends Extension<T>, S> {
    protected final void checkNotStatic(FieldDescriptor d) {}

    protected final void checkNotStatic(MethodDescriptor d) {}

    protected final void checkStatic(FieldDescriptor d) {}

    protected final void checkStatic(MethodDescriptor d) {}

    public void registerCallback(Consumer<T> extension) {

    }

    protected void visitAnnotatedField(FieldDescriptor method, VarHandle handle, S annotation) {}// We want to support injection

    protected void visitAnnotatedMethod(MethodDescriptor method, MethodHandle handle, S annotation) {}// We want to support injection

    protected void visitAnnotatedType(Class<?> type, S annotation) {}// We want to support injection

    protected void visitBegin(Class<?> type) {};

    protected void visitEnd(Class<?> type) {};
    // visitMethod
    // visitField
    // visitAnnotatedType
    // visitEnd

    protected void visitInstance(Class<S> type) {}
}

// Maaske man gerne registrere et object for baade ManegedOperation + ManagedAttribute
/// Man kan vel sige saa skal FF vaere annotering og man bliver noedt
class FF<T extends Extension<T>, Annotation> {
    // visitBegin
    // visitMethod
    // visitField
    // visitAnnotatedType
    // visitEnd

    public void registerCallback(BiConsumer<T, ComponentConfiguration> extension) {

    }
}

// A new Ff is made every time
class P extends FF<InjectorExtension, Provide> {
    // void registerCallback()
}

// boolean isCachable???
// Hele ideen med den er vel at vaere cachable
// Runtime componenter er lidt doed nu... efter de her extensions...
// Med mindre man ogsaa har en runtime callback man kan kalde....
class ProvidePro extends ExtensionProcessor<InjectorExtension, Provide> {

    final AtProvidesGroup.Builder b = new AtProvidesGroup.Builder(); // We know we will see at least one

    /** {@inheritDoc} */
    @Override
    protected void visitAnnotatedMethod(MethodDescriptor method, MethodHandle handle, Provide annotation) {
        // checkNotStatic
        // validate stuff

        // Det er saa her vi har problemet med instance metoder.....
        //// Og hvorfor vi ligger alt det lort ovenpaaaaa!!!! ARGHHH
    }

    /** {@inheritDoc} */
    @Override
    protected void visitEnd(Class<?> type) {
        // AtProvidesGroup g = b.build();
        // registerCallback(e -> e.installGroup(g));
    }

}

// Eneste problem er der bundle.provide().
// I cachen kan vi ikke vide om der er blevet brugt install() eller provide()..
// Maaske push/pop en variable i InjectorExtension
// provide(Factory<?> f) {
//// provided = f;
//// install(f);
//// if (provided !=null) <- Callback metoden har ikke nullet den ud...
////
//// provided = null;

//// i callback check if provided er != null.
//// In which we ogsaa installere denne og saette provided til null.
