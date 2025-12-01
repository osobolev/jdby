package jdby.transaction;

public interface SqlAction<C, E extends Exception> {

    void run(C connection) throws E;
}
