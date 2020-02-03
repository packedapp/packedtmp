package app.packed.base.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import app.packed.base.Key;
import app.packed.base.TypeLiteral;
import packed.internal.base.reflect.PackedMethodDescriptor;

public interface MethodDescriptor extends ExecutableDescriptor {

    Key<?> fromMethodReturnType();

    /**
     * Returns the generic return type of the method.
     *
     * @return the generic return type of the method
     */
    Type getGenericReturnType();

    /**
     * Returns whether or not this method is a static method.
     *
     * @return whether or not this method is a static method
     * @see Modifier#isStatic(int)
     */
    boolean isStatic();

    boolean overrides(MethodDescriptor supeer);

    /**
     * Returns a {@code Class} object that represents the formal return type of this method .
     *
     * @return the return type of this method
     * @see Method#getReturnType()
     */
    Class<?> returnType();

    /**
     * Returns a type literal that identifies the generic type return type of the method.
     *
     * @return a type literal that identifies the generic type return type of the method
     * @see Method#getGenericReturnType()
     */
    TypeLiteral<?> returnTypeLiteral();

    /**
     * Produces a method handle for the underlying method.
     * 
     * @param lookup
     *            the lookup object
     * @param specialCaller
     *            the class nominally calling the method
     * @return a method handle which can invoke the reflected method
     * @throws IllegalAccessException
     *             if access checking fails, or if the method is {@code static}, or if the method's variable arity modifier
     *             bit is set and {@code asVarargsCollector} fails
     * @see Lookup#unreflectSpecial(Method, Class)
     */
    MethodHandle unreflectSpecial(Lookup lookup, Class<?> specialCaller) throws IllegalAccessException;

    boolean isNullableReturnType();

    /**
     * Creates a new descriptor from the specified method.
     *
     * @param method
     *            the method to create a descriptor from
     * @return a new method descriptor
     */
    static MethodDescriptor from(Method method) {
        return new PackedMethodDescriptor(method);
    }

}