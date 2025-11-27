package jdbq.dao;

import jdbq.core.RowTransaction;
import jdbq.core.SqlTransaction;

public class DaoTransaction extends RowTransaction {

    private final DaoContext ctx;

    public DaoTransaction(DaoContext ctx, SqlTransaction t) {
        super(ctx, t);
        this.ctx = ctx;
    }

    public <T> T dao(Class<T> cls) {
        // todo: cache proxies???
        return DaoSql.createProxy(ctx, cls, this);
    }
}
