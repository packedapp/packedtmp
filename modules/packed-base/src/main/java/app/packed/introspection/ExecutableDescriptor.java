package app.packed.introspection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * An executable descriptor.
 * <p>
 * Unlike the {@link Executable} class, this interface contains no mutable operations, so it can be freely shared.
 * 
 * @apiNote In the future, if the Java language permits, {@link ExecutableDescriptor} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
public interface ExecutableDescriptor extends AnnotatedElement, MemberDescriptor {

    Class<?>[] getParameterTypes();

    /**
     * Returns true if the takes a variable number of arguments, otherwise false.
     *
     * @return true if the takes a variable number of arguments, otherwise false.
     * 
     * @see Method#isVarArgs()
     * @see Constructor#isVarArgs()
     */
    boolean isVarArgs();

    /**
     * Returns the number of formal parameters (whether explicitly declared or implicitly declared or neither) for the
     * underlying executable.
     *
     * @return The number of formal parameters for the method this object represents
     *
     * @see Executable#getParameterCount()
     * @see Method#getParameterCount()
     * @see Constructor#getParameterCount()
     */
    int parameterCount();

    /**
     * Unreflects this executable.
     * 
     * @param lookup
     *            the lookup object to use for unreflecting this executable
     * @return a MethodHandle corresponding to this executable
     * @throws IllegalAccessException
     *             if the lookup object does not have access to the executable
     * @see Lookup#unreflect(Method)
     * @see Lookup#unreflectConstructor(Constructor)
     */
    // If we have a non method based version....
    // We can use Lookup.find(xxxxx)
    MethodHandle unreflect(MethodHandles.Lookup lookup) throws IllegalAccessException;
}