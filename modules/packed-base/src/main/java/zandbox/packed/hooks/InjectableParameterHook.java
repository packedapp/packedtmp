package zandbox.packed.hooks;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import app.packed.container.Extension;

// Jeg ved faktisk ikke hvor useful den er...
// Vi har hook Provide der formentlig kan daekke de fleste behov for
public @interface InjectableParameterHook {
    
    /** The {@link Bootstrap} class for this hook. */
    Class<? extends InjectableParameterHook.Bootstrap> bootstrapBean();

    /** The extension this hook is a part of. */
    Class<? extends Extension> extension();

    /** The annotation that triggers the hook. */
    Class<? extends Annotation> onAnnotation();

    /** The annotation that triggers the hook. */
    Class<?> onExactClass();

    abstract class Bootstrap {

        /** {@return the underlying parameter} */
        public final Executable getDeclaringExecutable() {
            return getParameter().getDeclaringExecutable();
        }

        /** {@return the index of the parameter in the underlying executable} */
        public final int getIndex() {
            throw new UnsupportedOperationException();
        }
        /** {@return the underlying parameter} */
        public final Parameter getParameter() {
            throw new UnsupportedOperationException();
        }
        
        public final Type parameterizedType() {
            throw new UnsupportedOperationException();
        }

        // if we support vargs it is resolved to an array
        protected static final void $supportVarArgs() {
            throw new UnsupportedOperationException();
        }
    }
}
// how do we handly array types?
// how do we handle primitive types