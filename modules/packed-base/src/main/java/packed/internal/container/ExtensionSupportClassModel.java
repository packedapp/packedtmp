package packed.internal.container;

import static java.util.Objects.requireNonNull;

import app.packed.container.Extension;
import app.packed.container.ExtensionSupportClass;
import app.packed.container.ExtensionSupportClass.Scope;

@SuppressWarnings("unused")
public final class ExtensionSupportClassModel {

    private static final ClassValue<ExtensionSupportClassModel> MODELS = new ClassValue<>() {

        @Override
        protected ExtensionSupportClassModel computeValue(Class<?> type) {
            ExtensionSupportClass esc = type.getAnnotation(ExtensionSupportClass.class);
            if (esc == null) {
                return null;
            }
            // TODO Auto-generated method stub
            return new ExtensionSupportClassModel(esc.extension(), esc.scope());
        }
    };

    private final Class<? extends Extension> extensionClass;

    private final ExtensionSupportClass.Scope scope;

    public ExtensionSupportClassModel(Class<? extends Extension> extensionClass, Scope scope) {
        this.extensionClass = requireNonNull(extensionClass);
        this.scope = requireNonNull(scope);
    }
}
