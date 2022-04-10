package packed.internal.base.variable2;

import java.lang.reflect.AnnotatedType;

public class TmpExecutable {

    AnnotatedType[] annotatedParameterTypes;
    
    AnnotatedType annotatationOf(int index) {
        return annotatedParameterTypes[index];
    }
    
}
