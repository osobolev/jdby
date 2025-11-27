package jdbq.core;

import java.util.List;

public interface QueryLike {

    CharSequence getSql();

    List<SqlParameter> getParameters();

    Query toQuery();
}
