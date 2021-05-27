package zandbox.internal.hooks3;

import packed.internal.container.ExtensionModel;

/**
 * A model of a class that employs hooks in some form.
 */
// Ved ikke om vi skal lave specifikke modeller???
public class HookedClassModel {

    public static HookedClassModel ofApplication(Class<?> applicationType) {
        throw new UnsupportedOperationException();
    }

    // Called from Extensor model??? Ja det ville jeg mene.
    // Vi checker ikke here om extensor.extensionType = extension.extensionType
    public static HookedClassModel ofExtensor(ExtensionModel extension, Class<?> extensorType) {
        throw new UnsupportedOperationException();
    }

}
// We do not expose the Actual Hook class model only beans, applications, extensors