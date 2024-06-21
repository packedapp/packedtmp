package app.packed.build;

/**
 * A simple marker interface for build mirrors.
 * <p>
 * Build mirrors are only intended to represent the application at build time. It is not intended to reflect settings or
 * configurations of the application that is performed at startup or runtime. For example, wirelets specified at runtime
 * will not be reflected in any mirrors that are accessed from within the application.
 *
 * @see <a href="https://bracha.org/mirrors.pdf">https://bracha.org/mirrors.pdf</a>
 */
public interface BuildMirror {}
