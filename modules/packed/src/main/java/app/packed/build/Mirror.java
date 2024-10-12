package app.packed.build;

/**
 * A simple marker interface for build mirrors.
 * <p>
 * Build mirrors are only intended to represent the application at build time. It does not reflect settings or
 * configurations that is applied to the application at startup or runtime. For example, wirelets specified at runtime
 * will not be reflected in any mirrors that are accessed from within the application.
 *
 * @see <a href="https://bracha.org/mirrors.pdf">https://bracha.org/mirrors.pdf</a>
 */
public interface Mirror {}
