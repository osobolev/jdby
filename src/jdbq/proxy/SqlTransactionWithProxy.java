package jdbq.proxy;

import jdbq.core.SqlTransaction;

// todo: do not use separate interface???
public interface SqlTransactionWithProxy extends SqlTransaction {

    <T extends BaseDao> T proxy(Class<T> cls);
}
