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
package app.packed.hook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static testutil.util.TestMemberFinder.findField;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import app.packed.container.InternalExtensionException;
import app.packed.introspection.FieldDescriptor;
import packed.internal.classscan.invoke.OpenClass;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hook.MemberUnreflector;
import testutil.stubs.annotation.AnnotationInstances;
import testutil.stubs.annotation.Left;

/** Tests {@link AnnotatedFieldHook}. */
// TODO, check error messages + Applicators
public class AnnotatedFieldHookTest {

    /** {@link #field} as a {@link Field} instance. */
    private static final Field FIELD = findField("field");

    /** The field used while testing. */
    private String field = "GotIt";

    /**
     * Tests the basic methods.
     * 
     * @see AnnotatedFieldHook#annotation()
     * @see AnnotatedFieldHook#field()
     */
    @Test
    public void basics() {
        MemberUnreflector hc = new MemberUnreflector(new OpenClass(MethodHandles.lookup(), AnnotatedFieldHookTest.class, false),
                UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        AnnotatedFieldHook<Left> h = new AnnotatedFieldHook<>(hc, findField("field"), AnnotationInstances.LEFT);
        hc.close(); // We close it here, because these checks should work, even if it is closed

        assertThat(h.annotation()).isSameAs(AnnotationInstances.LEFT);
        assertThat(h.field()).isEqualTo(FieldDescriptor.from(findField("field")));
        assertThat(h.field()).isSameAs(h.field()); // we should cache it
    }

    // /** Tests {@link #applyStatic()} */
    // @Test
    // public void applyStatic() {
    // UnreflectGate hc = newHookController();
    // AnnotatedFieldHook<Left> f = new AnnotatedFieldHook<>(hc, findField("foo"), AnnotationInstances.LEFT);
    // assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
    // f.applyStatic(VarOperator.supplier()));
    //
    // AnnotatedFieldHook<Left> fStatic = new AnnotatedFieldHook<>(hc, findField("FIELD"), AnnotationInstances.LEFT);
    // // TODO test it
    //
    // hc.close();
    // assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> fStatic.applyStatic(VarOperator.supplier()));
    // }

    /**
     * Tests the various check methods on {@link AnnotatedFieldHook}.
     * 
     * @see AnnotatedFieldHook#checkAssignableTo(Class)
     * @see AnnotatedFieldHook#checkExactType(Class)
     * @see AnnotatedFieldHook#checkFinal()
     * @see AnnotatedFieldHook#checkNotFinal()
     * @see AnnotatedFieldHook#checkNotStatic()
     * @see AnnotatedFieldHook#checkStatic()
     **/
    @Test
    public void checks() {

        @SuppressWarnings("unused")
        class Tester {
            static final String SF_FIELD = "";
            String FIELD = "";
        }

        MemberUnreflector unreflector = new MemberUnreflector(new OpenClass(MethodHandles.lookup(), AnnotatedFieldHookTest.class, false),
                UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        AnnotatedFieldHook<Left> sff = new AnnotatedFieldHook<>(unreflector, findField(Tester.class, "SF_FIELD"), AnnotationInstances.LEFT);
        AnnotatedFieldHook<Left> f = new AnnotatedFieldHook<>(unreflector, findField(Tester.class, "FIELD"), AnnotationInstances.LEFT);
        unreflector.close(); // We close it here, because these checks should work, even if it is closed

        // checkAssignableTo
        assertThat(f.checkAssignableTo(String.class)).isSameAs(f);
        assertThat(f.checkAssignableTo(CharSequence.class)).isSameAs(f);
        assertThatExceptionOfType(InternalExtensionException.class).isThrownBy(() -> f.checkAssignableTo(Long.class));

        // checkExactType
        assertThat(f.checkExactType(String.class)).isSameAs(f);
        assertThatExceptionOfType(InternalExtensionException.class).isThrownBy(() -> f.checkExactType(CharSequence.class));
        assertThatExceptionOfType(InternalExtensionException.class).isThrownBy(() -> f.checkExactType(Long.class));

        // checkFinal
        assertThatExceptionOfType(InternalExtensionException.class).isThrownBy(() -> f.checkFinal());
        assertThat(sff.checkFinal()).isSameAs(sff);

        // checkNotFinal
        assertThat(f.checkNotFinal()).isSameAs(f);
        assertThatExceptionOfType(InternalExtensionException.class).isThrownBy(() -> sff.checkNotFinal());

        // checkNotStatic
        assertThat(f.checkNotStatic()).isSameAs(f);
        assertThatExceptionOfType(InternalExtensionException.class).isThrownBy(() -> sff.checkNotStatic());

        // checkStatic
        assertThatExceptionOfType(InternalExtensionException.class).isThrownBy(() -> f.checkFinal());
        assertThat(sff.checkFinal()).isSameAs(sff);
    }

    /**
     * Tests the various methods that create instances of {@link MethodHandle} and {@link VarHandle}.
     * 
     * @see AnnotatedFieldHook#getter()
     * @see AnnotatedFieldHook#setter()
     * @see AnnotatedFieldHook#varHandle()
     */
    @Test
    public void handles() throws Throwable {
        MemberUnreflector unflector = new MemberUnreflector(new OpenClass(MethodHandles.lookup(), AnnotatedFieldHookTest.class, false),
                UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        AnnotatedFieldHook<Left> h = new AnnotatedFieldHook<>(unflector, FIELD, AnnotationInstances.LEFT);
        AnnotatedFieldHookTest t = new AnnotatedFieldHookTest();

        // getter
        assertThat(h.getter().type()).isSameAs(MethodType.methodType(String.class, AnnotatedFieldHookTest.class));
        assertThat(h.getter().invoke(t)).isEqualTo("GotIt");
        t.field = "notBad";
        assertThat(h.getter().invoke(t)).isEqualTo("notBad");
        assertThatExceptionOfType(WrongMethodTypeException.class).isThrownBy(() -> h.getter().invoke());

        // setter
        assertThat(t.field).isEqualTo("notBad");
        assertThat(h.setter().invoke(t, "fooBar")).isNull();
        assertThat(t.field).isEqualTo("fooBar");
        assertThat(h.getter().invoke(t)).isEqualTo("fooBar");

        assertThatExceptionOfType(WrongMethodTypeException.class).isThrownBy(() -> h.setter().invoke(t));
        assertThatExceptionOfType(WrongMethodTypeException.class).isThrownBy(() -> h.setter().invoke(t, 123));
        // cannot set final fields
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> new AnnotatedFieldHook<>(unflector, findField("FIELD"), AnnotationInstances.LEFT).setter());

        // varHandle
        assertThat(h.varHandle().get(t)).isEqualTo("fooBar");
        h.varHandle().set(t, "blabla");
        assertThat(t.field).isEqualTo("blabla");

        // Tests that we cannot call any of the handle methods after having closed the unreflector
        // This is done in order to make certain that people don't cache the hooks, and calls it at later
        // with the surprising behavior that the field was not added to native image generation

        // Previous we cached the handles, but it was a little inconsistent, because if you, for example,
        // called getter() in scope. You could also call it out scope and have the same method handle returned
        unflector.close();

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.getter());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.setter());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.varHandle());
    }
}
