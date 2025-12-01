package jdby.testing;

import jdby.dao.DaoContext;
import jdby.dao.DefaultDaoContextBuilder;
import jdby.mapping.MapperContext;

public class DefaultTestInstantiator extends BaseTestInstantiator {

    public final DefaultDaoContextBuilder builder;

    public DefaultTestInstantiator(DefaultDaoContextBuilder builder) {
        this.builder = builder;
    }

    @Override
    protected MapperContext mapper() {
        return builder.buildCore();
    }

    @Override
    protected DaoContext dao() {
        return builder.build();
    }
}
