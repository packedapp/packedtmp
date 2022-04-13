package app.packed.job;

public interface MainCompleter<T> {
    void complete();
    void complete(T result);
    void fail(Throwable cause);
}
