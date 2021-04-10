package app.packed.application;

import app.packed.component.Assembly;
import app.packed.component.Wirelet;

// Maaske ikke kun application???
// Men JOb.bind... 
// Og hvad med Small component stuff
public interface Launcher<A> {

    /**
     * Launches an instance of the application. What happens here is dependent on application driver that created the image.
     * The behavior of this method is identical to {@link ApplicationDriver#launch(Assembly, Wirelet...)}.
     * 
     * @param wirelets
     *            optional wirelets
     * @return an application instance
     * @see {@link ApplicationDriver#launch(Assembly, Wirelet...)}
     */
    A launch(Wirelet... wirelets);
}
