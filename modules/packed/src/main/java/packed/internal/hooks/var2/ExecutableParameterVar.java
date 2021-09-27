package packed.internal.hooks.var2;

import java.lang.reflect.AnnotatedType;

// Ideen er at lade vaere at lave en egentlig parameter
public record ExecutableParameterVar(TmpExecutable executable, int index) {

    @SuppressWarnings("unused")
    private AnnotatedType annotations() {
        return executable.annotatationOf(index);
    }
}
