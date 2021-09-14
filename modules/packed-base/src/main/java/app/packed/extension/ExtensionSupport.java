package app.packed.extension;

import java.util.ArrayList;

/**
 * Extension support classes are the primary way that extensions interact without other extensions. Developers that are
 * not developing their own extensions will likely deal with these type of classes.
 * <p>
 * 
 * <p>
 * Hvorfor eksistere disse support klasser
 * 
 * Vi vil gerne provide andre services til extensions istedet for application developers.
 * 
 * The application developers is in control not the extension developers
 * <p>
 * Extension support classes are typically (although not required) defined as a primitive class. Taking the extension
 * that it is a member of as a parameter in the constructor.
 * 
 * Make example... Maybe usin
 * <p>
 * 
 * <p>
 * Potentially also taking the requesting extension class as a parameter.
 * 
 * An extension There are no annotations that make sense for this class
 * 
 * {@code Class<? extends Extension>} which is the
 * 
 * <p>
 * A new instance of the extension support class is automatically created by the runtime when requested. These instances
 * are <strong>never</strong> cached, instead a new instance is created every time it is requested.
 * 
 * @see Extension#use(Class)
 * @see ExtensionConfiguration#use(Class)
 */
public abstract class ExtensionSupport {}
//Kalder vi den bare ExtensionSupport, og Saa FileSupport, ServiceSupport

class Zxtension extends Extension {
    final ArrayList<String> list = new ArrayList<>();
}

class Zample {
    final Zxtension extension;

    Zample(Zxtension extension) {
        this.extension = extension;
    }

    public void foobar(String x) {
        extension.list.add(x);
    }
}