package app.packed.introspection;

import java.lang.reflect.Parameter;

import packed.internal.introspection.PackedParameterDescriptor;

/**
 * A parameter descriptor.
 * <p>
 * Unlike the {@link Parameter} class, this interface contains no mutable operations, so it can be freely shared.
 * 
 * @apiNote In the future, if the Java language permits, {@link ParameterDescriptor} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
public interface ParameterDescriptor extends VariableDescriptor {

    /**
     * Returns the executable that defines the parameter.
     * 
     * @return the executable that defines the parameter
     */
    ExecutableDescriptor getDeclaringExecutable();

    /**
     * Returns the index of the parameter.
     *
     * @return the index of the parameter
     */
    int index();

    /**
     * Returns true if this parameter represents a variable argument list, otherwise returns false.
     *
     * @return true if an only if this parameter represents a variable argument list.
     * @see Parameter#isVarArgs()
     */
    boolean isVarArgs();

    /**
     * Returns a parameter descriptor representing the specified parameter.
     *
     * @param parameter
     *            the parameter for which to return a descriptor for
     * @return the descriptor
     */
    static ParameterDescriptor from(Parameter parameter) {
        return PackedParameterDescriptor.from(parameter);
    }
}