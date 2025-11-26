package jdbq.dao;

import jdbq.core.SqlTransaction;
import jdbq.core.SqlTransactionRaw;

public class DaoTransaction extends SqlTransaction {

    private final DaoContext ctx;

    public DaoTransaction(SqlTransactionRaw t, DaoContext ctx) {
        super(ctx, t);
        this.ctx = ctx;
    }

    public <T extends BaseDao> T dao(Class<T> cls) {
        return ProxyQueries.create(ctx, cls, this);
    }
}
