package zandbox.internal.hooks2.bootstrap;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.base.Nullable;
import packed.internal.bean.hooks.var2.Var;
import packed.internal.util.TypeUtil;

public final class VarModel {

    boolean isParameterized;

    public final Type parameterizedType;

    public final Class<?> type;

    VarModel(Builder builder) {
        this.type = builder.type;
        this.parameterizedType = builder.parameterizedType;
    }

    public Type getParameterizedType() {
        return parameterizedType;
    }

    public boolean isNullable() {
        return false;
    }

    @Nullable
    public MethodHandle wrap() {
        return null;
    }

    public static class Builder {
        final Class<?> actualType;

        boolean isNullable;

        Class<?> optionalClass;

        Type parameterizedType;

        Class<?> type;

        final Var var;

        public Builder(Var var) {
            this.var = requireNonNull(var);
            actualType = type = var.getType();
        }

        public VarModel build(Settings request) {

            parameterizedType = var.getParameterizedType();

            // Check for Nullable, long term I think we will just snatch up any Nullable annotation...
            if (request.checkNullable) {
                boolean isNullable = var.isAnnotationPresent(Nullable.class);

                if (isNullable && actualType.isPrimitive()) {
                    throw new IllegalStateException("Cannot use @Nullable on primitive type");
                }
            }

            // Process Optional? (Optional, OptionalInt, OptionalLong and OptionalDouble)
            if (request.processOptional) {
                if (actualType == Optional.class) {
                    if (!(var.getParameterizedType()instanceof ParameterizedType pt)) {
                        // MalformedParameterType???
                        throw new IllegalArgumentException("Cannot use raw type Optional, it must be parameterized");
                    }

                    Type optionalType = pt.getActualTypeArguments()[0];
                    if (!TypeUtil.isFreeFromTypeVariables(optionalType)) {
                        throw new IllegalArgumentException("Type variables is not supported for type " + parameterizedType);
                    }
                    parameterizedType = optionalType;

                    this.type = TypeUtil.rawTypeOf(optionalType);

                    // TODO should we do similar checks for wildcard??

                    // wildcard types not supporteed
                    // System.out.println(pt);

                    // System.out.println(parameterizedType.getClass() + " " + parameterizedType);
                    if (parameterizedType instanceof WildcardType) {
                        throw new IllegalArgumentException("Wildcard types is not supported for Optional, was " + parameterizedType);
                    }

                    // type = pt.getActualTypeArguments()[0];
                    // TODO check that we do not have optional of OptionalX, also ServiceRequest can never be optionally
                    // Also Provider cannot be optionally...
                    // TODO include annotation
                    // Cannot have Nullable + Optional....
                    optionalClass = Optional.class;
                } else if (actualType == OptionalInt.class) {
                    parameterizedType = type = int.class;
                    optionalClass = OptionalInt.class;
                } else if (actualType == OptionalLong.class) {
                    parameterizedType = type = long.class;
                    optionalClass = OptionalLong.class;
                } else if (actualType == OptionalDouble.class) {
                    parameterizedType = type = double.class;
                    optionalClass = OptionalDouble.class;
                }

                if (optionalClass != null && request.checkNullable && isNullable) {
                    throw new IllegalArgumentException("Cannot use use @" + optionalClass.getSimpleName() + " together with @Nullable");
                }
            }

            requireNonNull(parameterizedType);

            if (!request.supportWildcardTypes) {
                if (!TypeUtil.isFreeFromWildcarVariables(parameterizedType)) {
                    throw new IllegalStateException(parameterizedType + " must be free for wildcard types");
                }
            }

            return new VarModel(this);
        }

    }

    // IGNORE_NULLABLE
    // IGNORE_OPTIONAL
    // public static final int IGNORE_ALL

    public static final class Settings {

        boolean checkNullable = true;

        boolean processOptional = true;

        boolean supportGenericArrays;

        boolean supportParameterizedTypes;

        boolean supportTypeVariables;

        boolean supportWildcardTypes;

        public Settings() {}

        Settings(InjectableVariableBootstrapModel.Builder loader) {
            this.supportWildcardTypes = loader.supportWildcardTypes;
            this.supportTypeVariables = loader.supportTypeVariables;
        }
    }
}
