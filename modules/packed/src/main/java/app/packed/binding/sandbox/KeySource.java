package app.packed.binding.sandbox;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.binding.Variable;

/** Represents the source of a key for error reporting. */
sealed interface KeySource {
    String describe();

    record FieldSource(Field field) implements KeySource {
        @Override
        public String describe() {
            return "field '" + field.getName() + "' in " + field.getDeclaringClass().getSimpleName();
        }
    }

    record MethodReturnSource(Method method) implements KeySource {
        @Override
        public String describe() {
            return "return type of method '" + method.getName() + "' in " + method.getDeclaringClass().getSimpleName();
        }
    }

    record TypeCaptureSource(Class<?> anonymousClass) implements KeySource {
        @Override
        public String describe() {
            return "type capture from anonymous Key subclass";
        }
    }

    record ClassSource(Class<?> clazz) implements KeySource {
        @Override
        public String describe() {
            return "class " + clazz.getSimpleName();
        }
    }

    record VariableSource(Variable variable) implements KeySource {
        @Override
        public String describe() {
            return "variable from " + variable.getClass().getSimpleName();
        }
    }
}
