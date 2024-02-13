package app.packed.component;

/**
 * A simple marker interface for mirrors.
 * <p>
 * Mostly used for getting a quick overview of the different mirror types.
 *
 * @see <a href="https://bracha.org/mirrors.pdf">https://bracha.org/mirrors.pdf</a>
 */
public interface Mirror {}
//http://bracha.org/mirrors.pdf

//Maybe seal it, and the have
//findAll(Class<? extends Mirror>);
//FrameworkMirror? Mhhhh we can never stop than




/**
 *
 */

// Application, Assembly?, Container, Bean, Operation, Binding?
// What is the usecase. Should also have BaseComponentConfiguration???
interface BaseComponentMirror extends ComponentMirror {

}
