package jdby.dao;

public interface DaoSource {

    <T> T dao(Class<T> daoInterface);
}
