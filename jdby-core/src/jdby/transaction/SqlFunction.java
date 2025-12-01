package jdby.transaction;

public interface SqlFunction<C, R, E extends Exception> {

    R call(C connection) throws E;
}
