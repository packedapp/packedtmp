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
package internal.app.packed.bean.sidebean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.BrokenBarrierException;

import org.junit.jupiter.api.Test;

import internal.app.packed.extension.ExtensionContext;

class SidebeanInvokerModelTest {

    // --- Test interfaces ---

    interface VoidNoArgs {
        void execute();
    }

    interface VoidSingleArg {
        void invoke(String value);
    }

    interface VoidMultipleArgs {
        void process(String a, int b, long c);
    }

    interface ReturnsString {
        String getValue(String input);
    }

    interface ReturnsPrimitive {
        int compute(int a, int b);
    }

    interface SamWithException {
        void run() throws IOException;
    }

    interface NotSamInterface {
        void method1();
        void method2();
    }

    // --- Target methods that will be wrapped ---

    public static void targetVoidNoArgs(ExtensionContext ctx) {}

    public static String capturedValue;
    public static void targetVoidSingleArg(ExtensionContext ctx, String value) {
        capturedValue = value;
    }

    public static String capturedA;
    public static int capturedB;
    public static long capturedC;
    public static void targetVoidMultipleArgs(ExtensionContext ctx, String a, int b, long c) {
        capturedA = a;
        capturedB = b;
        capturedC = c;
    }

    public static String targetReturnsString(ExtensionContext ctx, String input) {
        return "Hello, " + input;
    }

    public static int targetReturnsPrimitive(ExtensionContext ctx, int a, int b) {
        return a + b;
    }

    // Exception targets
    public static void targetThrowsRuntimeException(ExtensionContext ctx) {
        throw new IllegalArgumentException("runtime-error");
    }

    public static void targetThrowsError(ExtensionContext ctx) {
        throw new StackOverflowError("stack-error");
    }

    public static void targetThrowsIOException(ExtensionContext ctx) throws IOException {
        throw new IOException("checked-error");
    }

    public static void targetThrowsOtherChecked(ExtensionContext ctx) throws BrokenBarrierException {
        throw new BrokenBarrierException("undeclared-checked-error");
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @Test
    void testVoidNoArgs() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetVoidNoArgs",
            MethodType.methodType(void.class, ExtensionContext.class));

        MethodHandle constructor = SidebeanInvokerModel.of(VoidNoArgs.class).constructor();
        VoidNoArgs instance = (VoidNoArgs) constructor.invoke(targetMH, (ExtensionContext) null);
        instance.execute();
    }

    @Test
    void testVoidSingleArg() throws Throwable {
        capturedValue = null;
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetVoidSingleArg",
            MethodType.methodType(void.class, ExtensionContext.class, String.class));

        MethodHandle constructor = SidebeanInvokerModel.of(VoidSingleArg.class).constructor();
        VoidSingleArg instance = (VoidSingleArg) constructor.invoke(targetMH, (ExtensionContext) null);

        instance.invoke("test-value");
        assertThat(capturedValue).isEqualTo("test-value");
    }

    @Test
    void testVoidMultipleArgs() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetVoidMultipleArgs",
            MethodType.methodType(void.class, ExtensionContext.class, String.class, int.class, long.class));

        MethodHandle constructor = SidebeanInvokerModel.of(VoidMultipleArgs.class).constructor();
        VoidMultipleArgs instance = (VoidMultipleArgs) constructor.invoke(targetMH, (ExtensionContext) null);

        instance.process("hello", 42, 123456789L);
        assertThat(capturedA).isEqualTo("hello");
        assertThat(capturedB).isEqualTo(42);
        assertThat(capturedC).isEqualTo(123456789L);
    }

    @Test
    void testReturnsPrimitiveInt() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetReturnsPrimitive",
            MethodType.methodType(int.class, ExtensionContext.class, int.class, int.class));

        MethodHandle constructor = SidebeanInvokerModel.of(ReturnsPrimitive.class).constructor();
        ReturnsPrimitive instance = (ReturnsPrimitive) constructor.invoke(targetMH, (ExtensionContext) null);

        assertThat(instance.compute(10, 32)).isEqualTo(42);
    }

    // --- Exception Tests ---

    @Test
    void testPropagatesRuntimeException() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetThrowsRuntimeException",
            MethodType.methodType(void.class, ExtensionContext.class));

        MethodHandle constructor = SidebeanInvokerModel.of(VoidNoArgs.class).constructor();
        VoidNoArgs instance = (VoidNoArgs) constructor.invoke(targetMH, (ExtensionContext) null);

        assertThatThrownBy(instance::execute)
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("runtime-error");
    }

    @Test
    void testPropagatesError() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetThrowsError",
            MethodType.methodType(void.class, ExtensionContext.class));

        MethodHandle constructor = SidebeanInvokerModel.of(VoidNoArgs.class).constructor();
        VoidNoArgs instance = (VoidNoArgs) constructor.invoke(targetMH, (ExtensionContext) null);

        assertThatThrownBy(instance::execute)
            .isExactlyInstanceOf(StackOverflowError.class)
            .hasMessage("stack-error");
    }

    @Test
    void testPropagatesDeclaredCheckedException() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetThrowsIOException",
            MethodType.methodType(void.class, ExtensionContext.class));

        MethodHandle constructor = SidebeanInvokerModel.of(SamWithException.class).constructor();
        SamWithException instance = (SamWithException) constructor.invoke(targetMH, (ExtensionContext) null);

        assertThatThrownBy(instance::run)
            .isExactlyInstanceOf(IOException.class)
            .hasMessage("checked-error");
    }

    @Test
    void testWrapsUndeclaredCheckedException() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetThrowsOtherChecked",
            MethodType.methodType(void.class, ExtensionContext.class));

        MethodHandle constructor = SidebeanInvokerModel.of(VoidNoArgs.class).constructor();
        VoidNoArgs instance = (VoidNoArgs) constructor.invoke(targetMH, (ExtensionContext) null);

        assertThatThrownBy(instance::execute)
            .isExactlyInstanceOf(UndeclaredThrowableException.class)
            .cause() // Navigates to the cause and treats it as a Throwable
            .isExactlyInstanceOf(BrokenBarrierException.class)
            .hasMessage("undeclared-checked-error");
    }

    @Test
    void testWrapsUndeclaredEvenIfOthersAreDeclared() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetThrowsOtherChecked",
            MethodType.methodType(void.class, ExtensionContext.class));

        MethodHandle constructor = SidebeanInvokerModel.of(SamWithException.class).constructor();
        SamWithException instance = (SamWithException) constructor.invoke(targetMH, (ExtensionContext) null);

        assertThatThrownBy(instance::run)
            .isExactlyInstanceOf(UndeclaredThrowableException.class)
            .cause()
            .isExactlyInstanceOf(BrokenBarrierException.class);
    }

    // --- Utility / Validation Tests ---

    @Test
    void testNotAnInterface() {
        assertThatThrownBy(() -> SidebeanInvokerModel.of(String.class).constructor())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not an interface");
    }

    @Test
    void testNotSamInterface() {
        assertThatThrownBy(() -> SidebeanInvokerModel.of(NotSamInterface.class).constructor())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("has multiple abstract methods");
    }

    @Test
    void testMultipleInvocations() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerModelTest.class, "targetVoidSingleArg",
            MethodType.methodType(void.class, ExtensionContext.class, String.class));

        MethodHandle constructor = SidebeanInvokerModel.of(VoidSingleArg.class).constructor();
        VoidSingleArg instance = (VoidSingleArg) constructor.invoke(targetMH, (ExtensionContext) null);

        instance.invoke("first");
        assertThat(capturedValue).isEqualTo("first");

        instance.invoke("second");
        assertThat(capturedValue).isEqualTo("second");
    }

    @Test
    void testMultipleGeneratedClasses() throws Throwable {
        MethodHandle ctor1 = SidebeanInvokerModel.of(VoidNoArgs.class).constructor();
        MethodHandle ctor2 = SidebeanInvokerModel.of(VoidNoArgs.class).constructor();

        assertThat(ctor1).isNotNull();
        assertThat(ctor2).isNotNull();
        assertThat(ctor1.type()).isEqualTo(ctor2.type());
    }
}