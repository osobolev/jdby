package jdbq.proxy;

import jdbq.core.Query;

// todo: remove BaseDao???
public interface BaseDao {

    // todo: rename
    // todo: can move to static method???
    Query sqlPiece(String sql);
}
