package app.packed.application;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.base.NamespacePath;
import app.packed.component.Assembly;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.mirror.Mirror;
import app.packed.mirror.SetView;
import app.packed.mirror.TreeWalker;
import packed.internal.application.PackedApplicationDriver;

/**
 * A mirror of an application.
 * <p>
 */
// Skal vi have en ApplicationModifier ogsaa?
public interface ApplicationMirror extends Mirror {

    /** {@return the root (container) component in the application}. */
    ComponentMirror applicationComponent(); //

    default Optional<ApplicationHostMirror> applicationHost() {
        throw new UnsupportedOperationException();
    }

    /** {@return the component in the application}. */
    ComponentMirror component(CharSequence path);

    TreeWalker<ComponentMirror> components();

    // Er det kun componenter i den application??? Ja ville jeg mene...
    // Men saa kommer vi ud i spoergsmaalet omkring er components contextualizable...
    // app.rootContainer.children() <-- does this only include children in the same
    // application?? or any children...

    /** {@return the root container in the application}. */
    ContainerMirror container();

    // teanker det kun er containere i samme application...
    // ellers maa man bruge container.resolve("....")
    ContainerMirror container(CharSequence path);

    /** {@return a walker containing all the containers in this application} */
    TreeWalker<ContainerMirror> containers();

    /** { @return a set view of all extensions that are in use by the application.} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default Set<Class<? super Extension>> findAllExtensions(boolean includeChildApplications) {
        Set<Class<? super Extension>> result = new HashSet<>();
        containers().forEach(c -> result.addAll((Set) c.extensions()));
        return Set.copyOf(result);
    }

    /** {@return all the application hosts defined in this application.} */
    default SetView<ApplicationHostMirror> findAllHosts(boolean includeChildApplications) {
        throw new UnsupportedOperationException();
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
        return findAllHosts(includeChildApplications).stream().mapMulti(bc).toList();
    }

    default TaskListMirror initialization() {
        throw new UnsupportedOperationException();
    }

    default boolean isHosted() {
        return !parent().isEmpty();
    }

    /**
     * Returns whether or the application is runnable. The value is always determined by
     * {@link ApplicationDriver#hasRuntime()}.
     * 
     * @return whether or the application is runnable
     */
    boolean isRunnable();

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
     * {@return the module that the application belongs to. This is typically the module of the assembly that defined the
     * root container.}
     */
    Module module();

    /** {@return the name of the application.} */
    String name();

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
    default int ordinal() {
        // Hmm, den er vel altid naermest bare 1...
        // Ahh, hvis vi installere en fall back version... Saa har vi to
        // Syntes hellere vi skal kalde den loebe nummer end version.
        // Maaske er der nogen der gerne ville have deres eget versions begreb
        return 1;
    }

    /** {@return the parent application of this application. Or empty if this application has no parent} */
    Optional<ApplicationMirror> parent();

    NamespacePath path();
    // Optional<ApplicationRelation> parentRelation();

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

    // reflector
    /**
     * {@return the default application driver that is used when creating mirrors without explicitly specifying an application
     * driver.}
     */
    public static ApplicationDriver<?> defaultDriver() {
        return PackedApplicationDriver.MIRROR_DRIVER;
    }

    public static ApplicationMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return BaseMirror.of(assembly, wirelets).application();
    }

    // Relations between to different applications
    // Ret meget som ComponentRelation

    /// Maaske flyt til ApplicationMirror.relation...
    /// Der er ingen der kommer til at lave dem selv...

    // extends Relation???
    interface ParentRelation {

        ApplicationMirror child();

        // Det kan jo ogsaa ligge i en attribute. Og saa kan vi extracte det
        // Via ApplicationHostMirror.of(ApplicationMirror child)
        Object hostInfo();

        ApplicationMirror parent();
    }

    /**
     * An application relation is an unchangeable representation of a directional relationship between two applications. It
     * is typically created via {@link ComponentMirror#relationTo(ComponentMirror)}.
     */
    public interface Relation {

        ApplicationMirror from();

        ApplicationMirror to();
    }
}
