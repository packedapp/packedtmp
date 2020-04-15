package packed.internal.reflect;

import packed.internal.sidecar.Model;

//Composite kan have composites i sig.
//De kan ogsaa bruge prime annoteringer paa lige maade som alle andre

// Der kan ikke vaere cirkler... Maaske det lettest er bare at forbyde andre composites???
/**
 * A model of a composite.
 */
public final class CompositeModel extends Model {

    /**
     * @param type
     */
    protected CompositeModel(Class<?> type) {
        super(type);
    }

    // Obviously the composite type

    // Whether or not its annotated with Composite... Maybe just ignore it...)

    // Or just anno...
}
