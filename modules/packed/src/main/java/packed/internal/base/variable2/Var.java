package packed.internal.base.variable2;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public interface Var extends AnnotatedElement {

    /**
     * @return
     * 
     * @see Field#getType()
     * @see Parameter#getType()
     * @see Executable#getParameterTypes()
     */
    Class<?> getType();
    
    Type getParameterizedType();
}
