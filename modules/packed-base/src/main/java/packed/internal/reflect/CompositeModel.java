package packed.internal.reflect;

import packed.internal.sidecar.Model;

//Composite kan have composites i sig.
//De kan ogsaa bruge prime annoteringer paa lige maade som alle andre

// Der kan ikke vaere cirkler... Maaske det lettest er bare at forbyde andre composites???
/**
 * A model of a composite.
 */
// Composite modeller vil vaere et nightmare med generics...
public final class CompositeModel extends Model {

    /** Whether or not the annotation is on the actual class. */
    boolean annotationOnClass;

    /** A cache of models. */
    static final ClassValue<CompositeModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected CompositeModel computeValue(Class<?> type) {
            // Vi har en global laas.. Saa vi kun er 1 composite af gangen vi beregner...
            // Eller ogsaa skal vi kun laase hvis vi faktisk har referencer til andre composites...

            // Saa find executable....
            // Loeb alle parameterene igennem
            // Gem dem i en liste taenker jeg...

            // Taenker ikke vi understoetter det i foerste omgang...
            // Hvis der er flere composites...

            // Vi kan jo bruge lookup context fra calleren tit...
            // Saa kan vi snyde at vi ikke har adgang...

            // Maaske skal vi slet ikke share composites af composites...
            // Men kun lave dem et sted...
            // Altsaa vi kan share toppen...
            // Men resten skal vaere synlig fra en composite til en anden......

            // JA vi sharer ikke noget...

            throw new Error();
        }
    };

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
