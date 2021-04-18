package app.packed.hooks;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public @interface InjectableParameterHook {

    abstract class Bootstrap {

        /** {@return the index of the parameter in the underlying executable} */
        public final int getIndex() {
            throw new UnsupportedOperationException();
        }

        /** {@return the underlying parameter} */
        public final Parameter getParameter() {
            throw new UnsupportedOperationException();
        }
        /** {@return the underlying parameter} */
        public final Executable getDeclaringExecutable() {
            return getParameter().getDeclaringExecutable();
        }
        
        public final Type parameterizedType() {
            throw new UnsupportedOperationException();
        }

        protected static final void $supportVarArgs() {
            throw new UnsupportedOperationException();
        }
    }
}
