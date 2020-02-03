package app.packed.base.reflect;

import java.lang.reflect.Parameter;

import app.packed.base.Key;
import packed.internal.base.reflect.PackedParameterDescriptor;

public interface ParameterDescriptor extends VariableDescriptor {

    Key<?> toKey();

    ExecutableDescriptor getDeclaringExecutable();

    /**
     * Return a descriptor of the executable declaring this parameter.
     *
     * @return a descriptor of the executable declaring this parameter
     * @see Parameter#getDeclaringExecutable()
     */
    ExecutableDescriptor declaringExecutable();

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
     * Creates a new parameter descriptor from the specified parameter.
     *
     * @param parameter
     *            the parameter to create a descriptor for
     * @return a new parameter descriptor
     */
    static ParameterDescriptor from(Parameter parameter) {
        return PackedParameterDescriptor.from(parameter);
    }
}