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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

import internal.app.packed.extension.ExtensionContext;

class SidebeanInvokerTest {

    // Test interfaces
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

    interface ReturnsLong {
        long computeLong(long a, long b);
    }

    interface ReturnsDouble {
        double computeDouble(double a, double b);
    }

    interface ReturnsBoolean {
        boolean check(boolean value);
    }

    interface NotSamInterface {
        void method1();
        void method2();
    }

    // Target methods that will be wrapped
    public static void targetVoidNoArgs(ExtensionContext ctx) {
        // Do nothing
    }

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

    public static long targetReturnsLong(ExtensionContext ctx, long a, long b) {
        return a * b;
    }

    public static double targetReturnsDouble(ExtensionContext ctx, double a, double b) {
        return a / b;
    }

    public static boolean targetReturnsBoolean(ExtensionContext ctx, boolean value) {
        return !value;
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @Test
    void testVoidNoArgs() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetVoidNoArgs",
            MethodType.methodType(void.class, ExtensionContext.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(VoidNoArgs.class);
        assertThat(constructor).isNotNull();

        VoidNoArgs instance = (VoidNoArgs) constructor.invoke(targetMH, (ExtensionContext) null);
        assertThat(instance).isNotNull();

        // Should not throw
        instance.execute();
    }

    @Test
    void testVoidSingleArg() throws Throwable {
        capturedValue = null;

        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetVoidSingleArg",
            MethodType.methodType(void.class, ExtensionContext.class, String.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(VoidSingleArg.class);
        VoidSingleArg instance = (VoidSingleArg) constructor.invoke(targetMH, (ExtensionContext) null);

        instance.invoke("test-value");
        assertThat(capturedValue).isEqualTo("test-value");
    }

    @Test
    void testVoidMultipleArgs() throws Throwable {
        capturedA = null;
        capturedB = 0;
        capturedC = 0L;

        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetVoidMultipleArgs",
            MethodType.methodType(void.class, ExtensionContext.class, String.class, int.class, long.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(VoidMultipleArgs.class);
        VoidMultipleArgs instance = (VoidMultipleArgs) constructor.invoke(targetMH, (ExtensionContext) null);

        instance.process("hello", 42, 123456789L);

        assertThat(capturedA).isEqualTo("hello");
        assertThat(capturedB).isEqualTo(42);
        assertThat(capturedC).isEqualTo(123456789L);
    }

    @Test
    void testReturnsString() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetReturnsString",
            MethodType.methodType(String.class, ExtensionContext.class, String.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(ReturnsString.class);
        ReturnsString instance = (ReturnsString) constructor.invoke(targetMH, (ExtensionContext) null);

        String result = instance.getValue("World");
        assertThat(result).isEqualTo("Hello, World");
    }

    @Test
    void testReturnsPrimitiveInt() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetReturnsPrimitive",
            MethodType.methodType(int.class, ExtensionContext.class, int.class, int.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(ReturnsPrimitive.class);
        ReturnsPrimitive instance = (ReturnsPrimitive) constructor.invoke(targetMH, (ExtensionContext) null);

        int result = instance.compute(10, 32);
        assertThat(result).isEqualTo(42);
    }

    @Test
    void testReturnsPrimitiveLong() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetReturnsLong",
            MethodType.methodType(long.class, ExtensionContext.class, long.class, long.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(ReturnsLong.class);
        ReturnsLong instance = (ReturnsLong) constructor.invoke(targetMH, (ExtensionContext) null);

        long result = instance.computeLong(6L, 7L);
        assertThat(result).isEqualTo(42L);
    }

    @Test
    void testReturnsPrimitiveDouble() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetReturnsDouble",
            MethodType.methodType(double.class, ExtensionContext.class, double.class, double.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(ReturnsDouble.class);
        ReturnsDouble instance = (ReturnsDouble) constructor.invoke(targetMH, (ExtensionContext) null);

        double result = instance.computeDouble(10.0, 2.0);
        assertThat(result).isEqualTo(5.0);
    }

    @Test
    void testReturnsPrimitiveBoolean() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetReturnsBoolean",
            MethodType.methodType(boolean.class, ExtensionContext.class, boolean.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(ReturnsBoolean.class);
        ReturnsBoolean instance = (ReturnsBoolean) constructor.invoke(targetMH, (ExtensionContext) null);

        assertThat(instance.check(true)).isFalse();
        assertThat(instance.check(false)).isTrue();
    }

    @Test
    void testNotAnInterface() {
        assertThatThrownBy(() -> SidebeanInvoker.generateInvoker(String.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not an interface");
    }

    @Test
    void testNotSamInterface() {
        assertThatThrownBy(() -> SidebeanInvoker.generateInvoker(NotSamInterface.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a SAM interface");
    }

    @Test
    void testMultipleInvocations() throws Throwable {
        MethodHandle targetMH = LOOKUP.findStatic(SidebeanInvokerTest.class, "targetVoidSingleArg",
            MethodType.methodType(void.class, ExtensionContext.class, String.class));

        MethodHandle constructor = SidebeanInvoker.generateInvoker(VoidSingleArg.class);
        VoidSingleArg instance = (VoidSingleArg) constructor.invoke(targetMH, (ExtensionContext) null);

        instance.invoke("first");
        assertThat(capturedValue).isEqualTo("first");

        instance.invoke("second");
        assertThat(capturedValue).isEqualTo("second");

        instance.invoke("third");
        assertThat(capturedValue).isEqualTo("third");
    }

    @Test
    void testMultipleGeneratedClasses() throws Throwable {
        // Generate multiple invokers to ensure unique class names
        MethodHandle ctor1 = SidebeanInvoker.generateInvoker(VoidNoArgs.class);
        MethodHandle ctor2 = SidebeanInvoker.generateInvoker(VoidNoArgs.class);

        assertThat(ctor1).isNotNull();
        assertThat(ctor2).isNotNull();
    }
}
