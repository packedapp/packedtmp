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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import app.packed.lang.reflect.FieldDescriptor;
import packed.internal.hook.UnreflectGate;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.UncheckedThrowableFactory;
import testutil.stubs.annotation.AnnotationInstances;
import testutil.stubs.annotation.Left;

/** Tests {@link AnnotatedFieldHook}. */
// TODO, check error messages
// Applicators
public class AnnotatedFieldHookTest {

    static final Field FIELD = findField("foo");

    String foo = "GotIt";

    /**
     * Tests the basics.
     * 
     * @see AnnotatedFieldHook#annotation()
     * @see AnnotatedFieldHook#field()
     */
    @Test
    public void basics() {
        UnreflectGate hc = newHookController();
        AnnotatedFieldHook<Left> h = new AnnotatedFieldHook<>(hc, findField("foo"), AnnotationInstances.LEFT);
        hc.close(); // We close it here, because these checks should work, even if it is closed

        assertThat(h.annotation()).isSameAs(AnnotationInstances.LEFT);
        assertThat(h.field()).isEqualTo(FieldDescriptor.of(findField("foo")));
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

        UnreflectGate hc = newHookController();
        AnnotatedFieldHook<Left> sff = new AnnotatedFieldHook<>(hc, findField(Tester.class, "SF_FIELD"), AnnotationInstances.LEFT);
        AnnotatedFieldHook<Left> f = new AnnotatedFieldHook<>(hc, findField(Tester.class, "FIELD"), AnnotationInstances.LEFT);
        hc.close(); // We close it here, because these checks should work, even if it is closed

        // checkAssignableTo
        assertThat(f.checkAssignableTo(String.class)).isSameAs(f);
        assertThat(f.checkAssignableTo(CharSequence.class)).isSameAs(f);
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> f.checkAssignableTo(Long.class));

        // checkExactType
        assertThat(f.checkExactType(String.class)).isSameAs(f);
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> f.checkExactType(CharSequence.class));
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> f.checkExactType(Long.class));

        // checkFinal
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> f.checkFinal());
        assertThat(sff.checkFinal()).isSameAs(sff);

        // checkNotFinal
        assertThat(f.checkNotFinal()).isSameAs(f);
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> sff.checkNotFinal());

        // checkNotStatic
        assertThat(f.checkNotStatic()).isSameAs(f);
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> sff.checkNotStatic());

        // checkStatic
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> f.checkFinal());
        assertThat(sff.checkFinal()).isSameAs(sff);
    }

    /**
     * Tests the various handle methods.
     * 
     * @see AnnotatedFieldHook#getter()
     * @see AnnotatedFieldHook#setter()
     * @see AnnotatedFieldHook#varHandle()
     */
    @Test
    public void handles() throws Throwable {
        UnreflectGate hc = newHookController();
        AnnotatedFieldHook<Left> h = new AnnotatedFieldHook<>(hc, FIELD, AnnotationInstances.LEFT);
        AnnotatedFieldHookTest t = new AnnotatedFieldHookTest();

        // getter
        assertThat(h.getter().type()).isSameAs(MethodType.methodType(String.class, AnnotatedFieldHookTest.class));
        assertThat(h.getter().invoke(t)).isEqualTo("GotIt");
        t.foo = "notBad";
        assertThat(h.getter().invoke(t)).isEqualTo("notBad");
        assertThatExceptionOfType(WrongMethodTypeException.class).isThrownBy(() -> h.getter().invoke());

        // setter
        assertThat(t.foo).isEqualTo("notBad");
        assertThat(h.setter().invoke(t, "fooBar")).isNull();
        assertThat(t.foo).isEqualTo("fooBar");
        assertThat(h.getter().invoke(t)).isEqualTo("fooBar");

        assertThatExceptionOfType(WrongMethodTypeException.class).isThrownBy(() -> h.setter().invoke(t));
        assertThatExceptionOfType(WrongMethodTypeException.class).isThrownBy(() -> h.setter().invoke(t, 123));
        // cannot set final fields
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> new AnnotatedFieldHook<>(hc, findField("FIELD"), AnnotationInstances.LEFT).setter());

        // varHandle
        assertThat(h.varHandle().get(t)).isEqualTo("fooBar");
        h.varHandle().set(t, "blabla");
        assertThat(t.foo).isEqualTo("blabla");

        // Tests that we cannot call any of the handle methods after having closed the controller
        // This is done in order to make certain that people don't cache the hooks, and calls it at
        // some later, being surprised that the field was not added to native image generation

        // Previous we cached the handles, but it was a little inconsistent, because if you, for example,
        // called getter() in scope. You could also call it out scope and have the same method handle returned
        hc.close();

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.getter());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.setter());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> h.varHandle());
    }

    private static UnreflectGate newHookController() {
        ClassProcessor cp = new ClassProcessor(MethodHandles.lookup(), AnnotatedFieldHookTest.class, false);
        return new UnreflectGate(cp, UncheckedThrowableFactory.ASSERTION_ERROR);
    }
}
