package packed.internal.hooks.var2;

import java.lang.reflect.AnnotatedType;

public record ExecutableParameterVar(TmpExecutable executable, int index) {

    @SuppressWarnings("unused")
    private AnnotatedType annotations() {
        return executable.annotatationOf(index);
                
    }

}
