package internal.app.packed.concurrent.cron.v2;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TypeCompatibilityCheckerTest {

    static class TestException extends Exception {
        private static final long serialVersionUID = 1L;

        TestException(String message) {
            super(message);
        }
    }

    // Test records
    record BoundedWildcardRecord(List<? extends Number> value) {}
    @SuppressWarnings("rawtypes")
    record RawListRecord(List value) {}
    record ConcreteTypeRecord(List<Integer> value) {}
    record WildcardWithLowerBoundRecord(List<? super Integer> value) {}
    record NestedGenericRecord(List<List<Number>> value) {}
    record GenericRecord<T>(List<T> value) {}
    record CollectionRecord(Collection<Number> value) {}
    record SetRecord(Set<Integer> value) {}
    record UnboundedWildcardRecord(List<?> value) {}
    record EmptyRecord() {}
    record ListRecord(List<Number> value) {}
    record QueueRecord(Queue<Number> value) {}

    // Test class with various return types
    static class TestClass<T> {
        public List<Integer> getIntegers() { return null; }
        public List<String> getStrings() { return null; }
        public List<Number> getNumbers() { return null; }
        public T getTypeVariable() { return null; }
        public List<T> getListTypeVariable() { return null; }
        public ArrayList<Integer> getArrayListIntegers() { return null; }
        public LinkedList<Integer> getLinkedListIntegers() { return null; }
        public List<List<Integer>> getNestedList() { return null; }
        @SuppressWarnings("rawtypes")
        public List getRawList() { return null; }
        public Collection<Integer> getCollection() { return null; }
        public Set<Integer> getSet() { return null; }
        public List<? extends Object> getWildcardObject() { return null; }
        public List<Long> getLongs() { return null; }
        public Number getNumber() { return null; }
        public Integer getInteger() { return null; }
        public Object getObject() { return null; }
    }

    @Nested
    class BasicValidation {
        @Test
        void nullMethodShouldThrowException() {
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    null, BoundedWildcardRecord.class, TestException::new));
            assertEquals("Method cannot be null", ex.getMessage());
        }

        @Test
        void nullRecordClassShouldThrowException() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getIntegers");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, null, TestException::new));
            assertEquals("Record class cannot be null", ex.getMessage());
        }

        @Test
        void emptyRecordShouldThrowException() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getIntegers");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, EmptyRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("must have exactly one component"));
        }
    }

    @Nested
    class TypeVariableHandling {
        @Test
        void methodWithTypeVariableShouldThrowException() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getTypeVariable");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, BoundedWildcardRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("Type cannot be a type variable"));
        }

        @Test
        void methodWithTypeVariableInListShouldThrowException() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getListTypeVariable");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, BoundedWildcardRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("Type cannot be a type variable"));
        }

        @Test
        void recordWithTypeVariableShouldThrowException() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getIntegers");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, GenericRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("cannot have type parameters"));
        }
    }

    @Nested
    class BoundedWildcardTests {
        @Test
        void shouldAcceptCompatibleTypes() throws Exception {
            Method method = TestClass.class.getMethod("getIntegers");
            assertDoesNotThrow(() ->
                TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, BoundedWildcardRecord.class, TestException::new));
        }

        @Test
        void shouldRejectIncompatibleTypes() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getStrings");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, BoundedWildcardRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("does not satisfy upper bound"));
        }
    }

    @Nested
    class NestedGenericsTests {
        @Test
        void shouldRejectIncompatibleNestedTypes() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getNestedList");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, NestedGenericRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("Type argument mismatch"));
        }

        @Test
        void nestedWildcardsShouldWork() throws NoSuchMethodException {
            record NestedWildcardRecord(List<List<? extends Number>> value) {}
            Method method = TestClass.class.getMethod("getNestedList");
            assertDoesNotThrow(() ->
                TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, NestedWildcardRecord.class, TestException::new));
        }
    }

    @Nested
    class CollectionHierarchyTests {
        @Test
        void shouldRejectDifferentCollectionTypes() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getSet");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, ListRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("is not assignable to"));
        }

        @Test
        void shouldAcceptCollectionSubtypes() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getArrayListIntegers");
            assertDoesNotThrow(() ->
                TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, ListRecord.class, TestException::new));
        }
    }

    @Nested
    class WildcardTests {
        @Test
        void shouldRejectViolatedLowerBound() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getLongs");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, WildcardWithLowerBoundRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("does not satisfy lower bound"));
        }

        @Test
        void shouldAcceptUnboundedWildcard() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getIntegers");
            assertDoesNotThrow(() ->
                TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, UnboundedWildcardRecord.class, TestException::new));

            Method method2 = TestClass.class.getMethod("getWildcardObject");
            assertDoesNotThrow(() ->
                TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method2, UnboundedWildcardRecord.class, TestException::new));
        }
    }

    @Nested
    class NonGenericTypeTests {
        @Test
        void shouldRejectNonGenericType() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getInteger");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, BoundedWildcardRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("Incompatible types"));
        }

        @Test
        void shouldRejectObjectType() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("getObject");
            TestException ex = assertThrows(TestException.class,
                () -> TypeCompatibilityChecker.isMethodReturnTypeCompatible(
                    method, BoundedWildcardRecord.class, TestException::new));
            assertTrue(ex.getMessage().contains("Incompatible types"));
        }
    }
}