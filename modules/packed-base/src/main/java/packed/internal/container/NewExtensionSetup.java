package packed.internal.container;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import packed.internal.component.ComponentSetup;

public final class NewExtensionSetup extends ComponentSetup {

    /** A model of the extension. */
    public final ExtensionModel model;

    /** The extension setup if this component represents an extension, otherwise null. */
    @Nullable
    public final ExtensionSetup extension;
    
    public NewExtensionSetup(ComponentSetup parent, ExtensionModel model) {
        super(parent, model);
        this.model = requireNonNull(model);
        this.extension = new ExtensionSetup(this, model);

        setName0(null /* model.nameComponent */); // setName0(String) does not work currently
    }
}
