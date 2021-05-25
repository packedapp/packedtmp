package packed.internal.container;

import static java.util.Objects.requireNonNull;

import app.packed.container.Extension;
import app.packed.hooks.sandbox.ExtensionSupportClass;
import app.packed.hooks.sandbox.ExtensionSupportClass.Scope;

@SuppressWarnings("unused")
public final class ExtensorModel {

    private static final ClassValue<ExtensorModel> MODELS = new ClassValue<>() {

        @Override
        protected ExtensorModel computeValue(Class<?> type) {
            ExtensionSupportClass esc = type.getAnnotation(ExtensionSupportClass.class);
            if (esc == null) {
                return null;
            }
            // TODO Auto-generated method stub
            return new ExtensorModel(esc.extension(), esc.scope());
        }
    };

    private final Class<? extends Extension> extensionClass;

    private final ExtensionSupportClass.Scope scope;

    public ExtensorModel(Class<? extends Extension> extensionClass, Scope scope) {
        this.extensionClass = requireNonNull(extensionClass);
        this.scope = requireNonNull(scope);
    }
}
