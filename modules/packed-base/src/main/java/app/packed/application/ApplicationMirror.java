package app.packed.application;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.application.host.ApplicationHost;
import app.packed.application.host.ApplicationHostMirror;
import app.packed.base.NamespacePath;
import app.packed.component.Assembly;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.mirror.Mirror;
import app.packed.mirror.SetView;
import app.packed.mirror.TreeWalker;

/**
 * A mirror of an application.
 * <p>
 * An instance of this class is typically obtained by calling {@link #of(Assembly, Wirelet...)} on this class.
 */
public interface ApplicationMirror extends Mirror {

    /** {@return the component in the application}. */
    ComponentMirror component(CharSequence path);

    /** {@return the component in the application}. */
    default <T extends ComponentMirror> SetView<T> components(Class<T> componentType) {
        throw new UnsupportedOperationException();
    }

    /** {@return the root container of the application}. */
    ContainerMirror container();

    // Er det kun componenter i den application??? Ja ville jeg mene...
    // Men saa kommer vi ud i spoergsmaalet omkring er components contextualizable...
    // app.rootContainer.children() <-- does this only include children in the same
    // application?? or any children...

    // teanker det kun er containere i samme application...
    // ellers maa man bruge container.resolve("....")

    /**
     * Returns an immutable set containing any extensions that have been disabled.
     * 
     * @return an immutable set containing any extensions that have been disabled
     * 
     * @see ApplicationDriver.Builder#disableExtension(Class...)
     */
    Set<Class<? extends Extension>> disabledExtensions();

    default <T extends ComponentMirror> SetView<T> findAll(Class<T> componentType, boolean includeChildApplications) {
        throw new UnsupportedOperationException();
    }

    // Skal vi have baade en der finder alle mirrors og alle extensions.
    /** { @return a set view of all extensions that are in use by the application.} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default Set<Class<? super Extension>> findAllExtensions(boolean includeChildApplications) {
        Set<Class<? super Extension>> result = new HashSet<>();
        components(ContainerMirror.class).forEach(c -> result.addAll((Set) c.extensions()));
        return Set.copyOf(result);
    }

    /** {@return all child applications of this application.} */
    // Det her har "store" implikationer for versions drevet applicationer...
    // Det betyder nemlig at vi totalt flatliner applikationer...
    // Det er bare applikation... Hvordan brugeren ser det er helt anderledes i forhold
    // til hvordan vi internt ser det
    /**
     * Returns all child applications deploy
     * 
     * @return
     */
    // IDK or children();
    default Collection<ApplicationMirror> findAllinstallations(boolean includeChildApplications) {
        BiConsumer<ApplicationHostMirror, Consumer<ApplicationMirror>> bc = (m, c) -> m.installations().forEach(p -> c.accept(p));
        return findAllInstalledHosts(includeChildApplications).stream().mapMulti(bc).toList();
    }

    /** {@return all the application hosts defined in this application.} */
    default SetView<ApplicationHostMirror> findAllInstalledHosts(boolean includeChildApplications) {
        return findAll(ApplicationHostMirror.class, includeChildApplications);
    }

    /**
     * Returns whether or the application is runnable. The value is always determined by
     * {@link ApplicationDriver#hasRuntime()}.
     * 
     * @return whether or the application is runnable
     */
    boolean hasRuntime();

    // Optional<ApplicationRelation> hostRelation();
    /**
     * @return the application host, if this application is hosted. Otherwise empty.
     * 
     * @see #isGuest()
     */
    Optional<ApplicationHostMirror> host();

    default TaskListMirror initialization() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not this application is hosted on top of another application.
     * 
     * @return
     * @see #host()
     */
    default boolean isGuest() {
        return host().isEmpty();
    }

    // Wired er parent component<->child component
    // connections er component til any component.
    /**
     * Returns whether or not this application is strongly wired to a parent application.
     * <p>
     * A root application will always return false.
     * 
     * @return {@code true} if this application is strongly wired to a parent application, otherwise {@code false}
     */
    boolean isStronglyWired();

    default boolean isUninstallable() {
        // If not-hosted???
        // true -> All root applications are trivially uninstallable
        // false -> All root applications have no uninstallation function
        return false;
    }

    /**
     * {@return the module of the application. This is always the module of the Assembly or Composer class that defines the
     * root container.}
     */
    Module module();

    /**
     * Returns the name of the application.
     * 
     * @return the name of the application
     * @see Wirelet#named(String)
     */
    String name();

    NamespacePath path();

    /**
     * <p>
     * A root application always returns empty
     * 
     * @return whether or not this application is a part of a versionable application
     */
    default Optional<VersionableApplicationMirror> versionable() {
        throw new UnsupportedOperationException();
    }

    default TreeWalker<ApplicationMirror> walker() {
        throw new UnsupportedOperationException();
        // app.components() <-- all component in the application
        // app.component().walker() <--- all components application or not...

        // someComponent.walker().filter(c->c.application == SomeApp)...
    }

    public static ApplicationMirror of(ApplicationDriver<?> applicationDriver, Assembly<?> assembly, Wirelet... wirelets) {
        return BaseMirror.of(assembly, wirelets).application();
    }

    /**
     * Create
     * 
     * @param assembly
     *            the assembly containing the application to create a mirror for
     * @param wirelets
     *            optional wirelets
     * @return an application mirror
     */
    public static ApplicationMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return of(ApplicationDriver.defaultMirrorDriver(), assembly, wirelets);
    }

    // Relations between to different applications
    // Ret meget som ComponentRelation

    /// Maaske flyt til ApplicationMirror.relation...
    /// Der er ingen der kommer til at lave dem selv...

    // extends Relation???
    interface HostGuestRelation {

        ApplicationMirror guest();

        /**
         * Returns the ordinal of this application (its position in its enum declaration, where the initial constant is assigned
         * an ordinal of zero).
         *
         * Most programmers will have no use for this method. It is designed for use by sophisticated enum-based data
         * structures, such as {@link java.util.EnumSet} and {@link java.util.EnumMap}.
         *
         * @return the ordinal of this application
         */
        // -1 for non-hosted? Nah 1
        // For mini hosts, er det et problem at skulle have en AtomicInteger... nah Vi har formentligt et map der fylder endnu
        // mere
        default int guestId() {
            // Hmm, den er vel altid naermest bare 1...
            // Ahh, hvis vi installere en fall back version... Saa har vi to
            // Syntes hellere vi skal kalde den loebe nummer end version.
            // Maaske er der nogen der gerne ville have deres eget versions begreb
            return 1;
        }

        ApplicationHost host();

        // Det kan jo ogsaa ligge i en attribute. Og saa kan vi extracte det
        // Via ApplicationHostMirror.of(ApplicationMirror child)
        Object hostInfo();

    }

    // Skal HostGuestRelation extende denne klasse
    /**
     * An application relation is an unchangeable representation of a directional relationship between two applications. It
     * is typically created via {@link ComponentMirror#relationTo(ComponentMirror)}.
     */
    public interface Relation {

        ApplicationMirror from();

        ApplicationMirror to();
    }
}
