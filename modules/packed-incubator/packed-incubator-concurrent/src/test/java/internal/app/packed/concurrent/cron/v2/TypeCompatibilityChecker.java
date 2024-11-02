package internal.app.packed.concurrent.cron.v2;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.function.Function;

public class TypeCompatibilityChecker {

    static <X extends Throwable> void isMethodReturnTypeCompatible(Method method, Class<?> recordClass, Function<? super String, ? extends X> exceptionSupplier)
            throws X {

        if (method == null) {
            throw exceptionSupplier.apply("Method cannot be null");
        }
        if (recordClass == null) {
            throw exceptionSupplier.apply("Record class cannot be null");
        }

        if (recordClass.getRecordComponents().length == 0) {
            throw exceptionSupplier.apply("Record class " + recordClass.getName() + " must have exactly one component");
        }

        // Check if the record class itself has type parameters
        if (recordClass.getTypeParameters().length > 0) {
            throw exceptionSupplier.apply("Record class " + recordClass.getName() + " cannot have type parameters");
        }

        Type recordComponentType = recordClass.getRecordComponents()[0].getGenericType();
        Type methodReturnType = method.getGenericReturnType();

        checkTypeVariables(methodReturnType, exceptionSupplier);
        isTypeCompatible(methodReturnType, recordComponentType, exceptionSupplier);
    }

    private static <X extends Throwable> void checkTypeVariables(Type type, Function<? super String, ? extends X> exceptionSupplier) throws X {

        if (type instanceof TypeVariable) {
            throw exceptionSupplier.apply("Type cannot be a type variable: " + type);
        }
        if (type instanceof ParameterizedType pType) {
            for (Type arg : pType.getActualTypeArguments()) {
                checkTypeVariables(arg, exceptionSupplier);
            }
        }
    }

    private static <X extends Throwable> void isTypeCompatible(Type source, Type target, Function<? super String, ? extends X> exceptionSupplier) throws X {

        if (source.equals(target)) {
            return;
        }

        // Handle raw types in the record (target)
        if (target instanceof Class<?> targetClass && source instanceof ParameterizedType) {
            Class<?> sourceRawType = (Class<?>) ((ParameterizedType) source).getRawType();
            if (!targetClass.isAssignableFrom(sourceRawType)) {
                throw exceptionSupplier.apply(String.format("Source raw type '%s' is not assignable to target type '%s'", sourceRawType, targetClass));
            }
            return;
        }

        // Handle parameterized types
        if (source instanceof ParameterizedType sourceType && target instanceof ParameterizedType targetType) {
            if (!((Class<?>) targetType.getRawType()).isAssignableFrom((Class<?>) sourceType.getRawType())) {
                throw exceptionSupplier
                        .apply(String.format("Source type '%s' is not assignable to target type '%s'", sourceType.getRawType(), targetType.getRawType()));
            }

            Type[] sourceArgs = sourceType.getActualTypeArguments();
            Type[] targetArgs = targetType.getActualTypeArguments();

            if (sourceArgs.length != targetArgs.length) {
                throw exceptionSupplier
                        .apply(String.format("Type parameter count mismatch: source has %d but target has %d", sourceArgs.length, targetArgs.length));
            }

            for (int i = 0; i < sourceArgs.length; i++) {
                isTypeArgumentCompatible(sourceArgs[i], targetArgs[i], exceptionSupplier);
            }

            return;
        }

        if (source instanceof Class<?> sourceClass && target instanceof Class<?> targetClass) {
            if (!targetClass.isAssignableFrom(sourceClass)) {
                throw exceptionSupplier.apply(String.format("Source class '%s' is not assignable to target class '%s'", sourceClass, targetClass));
            }
            return;
        }

        throw exceptionSupplier.apply(String.format("Incompatible types: source '%s' cannot be assigned to target '%s'", source, target));
    }

    private static <X extends Throwable> void isTypeArgumentCompatible(Type source, Type target, Function<? super String, ? extends X> exceptionSupplier)
            throws X {

        if (target instanceof WildcardType wildcardType) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            Type[] lowerBounds = wildcardType.getLowerBounds();

            for (Type upperBound : upperBounds) {
                if (source instanceof Class<?> sourceClass) {
                    if (!((Class<?>) upperBound).isAssignableFrom(sourceClass)) {
                        throw exceptionSupplier.apply(String.format("Source type '%s' does not satisfy upper bound '%s'", sourceClass, upperBound));
                    }
                } else if (source instanceof ParameterizedType) {
                    try {
                        isTypeCompatible(source, upperBound, exceptionSupplier);
                    } catch (Throwable e) {
                        throw exceptionSupplier
                                .apply(String.format("Source type '%s' does not satisfy upper bound '%s': %s", source, upperBound, e.getMessage()));
                    }
                }
            }

            for (Type lowerBound : lowerBounds) {
                if (source instanceof Class<?> sourceClass) {
                    if (!sourceClass.isAssignableFrom((Class<?>) lowerBound)) {
                        throw exceptionSupplier.apply(String.format("Source type '%s' does not satisfy lower bound '%s'", sourceClass, lowerBound));
                    }
                } else if (source instanceof ParameterizedType) {
                    try {
                        isTypeCompatible(lowerBound, source, exceptionSupplier);
                    } catch (Throwable e) {
                        throw exceptionSupplier
                                .apply(String.format("Source type '%s' does not satisfy lower bound '%s': %s", source, lowerBound, e.getMessage()));
                    }
                }
            }

            return;
        }

        if (source instanceof ParameterizedType sourceType && target instanceof ParameterizedType targetType) {
            if (!((Class<?>) targetType.getRawType()).isAssignableFrom((Class<?>) sourceType.getRawType())) {
                throw exceptionSupplier
                        .apply(String.format("Source type '%s' is not assignable to target type '%s'", sourceType.getRawType(), targetType.getRawType()));
            }

            Type[] sourceArgs = sourceType.getActualTypeArguments();
            Type[] targetArgs = targetType.getActualTypeArguments();

            if (sourceArgs.length != targetArgs.length) {
                throw exceptionSupplier
                        .apply(String.format("Type parameter count mismatch: source has %d but target has %d", sourceArgs.length, targetArgs.length));
            }

            for (int i = 0; i < sourceArgs.length; i++) {
                if (targetArgs[i] instanceof WildcardType) {
                    isTypeArgumentCompatible(sourceArgs[i], targetArgs[i], exceptionSupplier);
                } else if (!sourceArgs[i].equals(targetArgs[i])) {
                    throw exceptionSupplier
                            .apply(String.format("Type argument mismatch at position %d: expected '%s' but got '%s'", i, targetArgs[i], sourceArgs[i]));
                }
            }

            return;
        }

        if (source instanceof Class<?> sourceClass && target instanceof Class<?> targetClass) {
            if (!targetClass.isAssignableFrom(sourceClass)) {
                throw exceptionSupplier.apply(String.format("Source class '%s' is not assignable to target class '%s'", sourceClass, targetClass));
            }
            return;
        }

        if (!source.equals(target)) {
            throw exceptionSupplier.apply(String.format("Type argument mismatch: expected '%s' but got '%s'", target, source));
        }
    }
}