package app.packed.application;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.various.TaskListMirror;
import app.packed.bean.BeanMirror;
import app.packed.build.BuildMirror;
import app.packed.bundle.Assembly;
import app.packed.bundle.BundleMirror;
import app.packed.bundle.Wirelet;
import app.packed.bundle.host.ApplicationHost;
import app.packed.bundle.host.ApplicationHostMirror;
import app.packed.component.ComponentMirror;
import app.packed.component.Realm;
import app.packed.extension.Extension;
import app.packed.mirror.SetView;
import app.packed.mirror.TreeWalker;
import packed.internal.application.PackedApplicationDriver;

/**
 * A mirror of an application.
 * <p>
 * An instance of this class is typically obtained by calling {@link #of(Assembly, Wirelet...)} on this class.
 */
// En application kan
//// Vaere ejet af bruger
//// Member of an extension (neeej sjaeldent, if ever...)
//// Controlled by an extension

// Fx Session er controlled by WebExtension men er ikke member af den
public interface ApplicationMirror {

    /** {@return the build the application is a part of.} */
    BuildMirror build();

    /** {@return the root bundle in the application.} */
    BundleMirror bundle();

    /** {@return the type of the root bundle.} */ // IDK bundle().type() might be fine
    default Class<? extends Assembly > bundleType() {
        return bundle().type();
    }

    /** {@return the component in the application}. */
    ComponentMirror component(CharSequence path);

    default Stream<ComponentMirror> components() {
        return bundle().components();
    }

    /** {@return the component in the application}. */
    default <T extends ComponentMirror> SetView<T> components(Class<T> componentType) {
        throw new UnsupportedOperationException();
    }

    /** {@return a descriptor for the application.} */
    ApplicationDescriptor descriptor();

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

    // Er det kun componenter i den application??? Ja ville jeg mene...
    // Men saa kommer vi ud i spoergsmaalet omkring er components contextualizable...
    // app.rootContainer.children() <-- does this only include children in the same
    // application?? or any children...

    // teanker det kun er containere i samme application...
    // ellers maa man bruge container.resolve("....")

    // Skal vi have baade en der finder alle mirrors og alle extensions.
    /** { @return a set view of all extensions that are in use by the application.} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default Set<Class<? super Extension>> findAllExtensions(boolean includeChildApplications) {
        Set<Class<? super Extension>> result = new HashSet<>();
        components(BundleMirror.class).forEach(c -> result.addAll((Set) c.extensions()));
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
        throw new UnsupportedOperationException();
    }

    default void forEachComponent(Consumer<? super ComponentMirror> action) {
        bundle().components().forEach(action);
    }

    /**
     * Returns whether or the application is runnable. The value is always determined by
     * {@link ApplicationDriver#isExecutable()}.
     * 
     * @return whether or the application is runnable
     */
    boolean hasRuntime();

    // Optional<ApplicationRelation> hostRelation();
    /**
     * @return the application host, if this application is hosted. Otherwise empty.
     * 
     * @see #isRoot()
     */
    Optional<ApplicationHostMirror> host();

    default TaskListMirror initialization() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return whether or not this application is the root application.}
     * 
     * @see #host()
     */
    default boolean isRoot() {
        return !host().isEmpty();
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
    // Hmm, hvis applikation = Container specialization... Ved component
    // Tror maaske ikke vi vil have den her, IDK... HVad med bean? er det realm eller bean module
    // Maaske vi skal have et realm mirror????
    Module module();

    /**
     * Returns the name of the application.
     * <p>
     * The name of an application is always identical to the name of the root bundle.
     * 
     * @return the name of the application
     * @see Assembly#named(String)
     * @see Wirelet#named(String)
     */
    default String name() {
        return bundle().name();
    }

    default Realm owner() {
        return Realm.application();
    }

    default void print() {
        bundle().print();
    }

    default <T extends ComponentMirror> Stream<T> select(Class<T> componentType) {
        throw new UnsupportedOperationException();
    }

    // eller container().use(BeanExtensionMirror.class).
    default Stream<BeanMirror> selectBeans() {
        return select(BeanMirror.class);
    }

    default long version() {
        // Taenker det er rart at kunne skelne imellem applicationer der supporter versioning og dem der ikke goer..
        // Maaske returnere en optional<long>???
        return 1;

        //
//      /**
//       * <p>
//       * A root application always returns empty
//       * 
//       * @return whether or not this application is a part of a versionable application
//       */
//      default Optional<VersionableApplicationMirror> versionable() {
//          throw new UnsupportedOperationException();
//      }
    }

    default TreeWalker<ApplicationMirror> walker() {
        throw new UnsupportedOperationException();
        // app.components() <-- all component in the application
        // app.component().walker() <--- all components application or not...

        // someComponent.walker().filter(c->c.application == SomeApp)...
    }

    // Relations between to different applications
    // Ret meget som ComponentRelation

    /// Maaske flyt til ApplicationMirror.relation...
    /// Der er ingen der kommer til at lave dem selv...
//
//    /**
//     * {@return the default application driver that is used when creating mirrors without explicitly specifying an
//     * application driver.}
//     */
//    public static ApplicationDriver<?> defaultMirrorDriver() {
//        return ;
//    }

    /**
     * Create
     * 
     * @param assembly
     *            the assembly containing the application to create a mirror for
     * @param wirelets
     *            optional wirelets
     * @return an application mirror
     */
    // IDK om vi bare altid bruger en Application Launcher class...
    public static ApplicationMirror of(Assembly  assembly, Wirelet... wirelets) {
        return PackedApplicationDriver.MIRROR_DRIVER.mirrorOf(assembly, wirelets);
    }

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