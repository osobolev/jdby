package jdbq.dao;

import jdbq.core.SqlTransaction;
import jdbq.core.SqlTransactionRaw;

public class DaoTransaction extends SqlTransaction {

    private final DaoContext ctx;

    public DaoTransaction(DaoContext ctx, SqlTransactionRaw t) {
        super(ctx, t);
        this.ctx = ctx;
    }

    public <T> T dao(Class<T> cls) {
        // todo: cache proxies???
        return DaoSql.createProxy(ctx, cls, this);
    }
}
