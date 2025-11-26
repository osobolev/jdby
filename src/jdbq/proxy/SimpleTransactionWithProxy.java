package jdbq.proxy;

import jdbq.core.SqlTransactionRaw;
import jdbq.mapping.SimpleSqlTransactionWithMapper;

public class SimpleTransactionWithProxy extends SimpleSqlTransactionWithMapper implements SqlTransactionWithProxy {

    private final ProxyContext ctx;

    public SimpleTransactionWithProxy(SqlTransactionRaw t, ProxyContext ctx) {
        super(ctx, t);
        this.ctx = ctx;
    }

    @Override
    public <T extends BaseDao> T proxy(Class<T> cls) {
        return ctx.proxy(cls, this);
    }
}
