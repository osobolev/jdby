package jdby.core;

import jdby.core.testing.SqlTestingHook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class Batch implements AutoCloseable {

    private static final class BatchedStatement {

        final PreparedStatement ps;
        int batchSize = 0;

        BatchedStatement(PreparedStatement ps) {
            this.ps = ps;
        }

        void addBatch(int batchPortionSize) throws SQLException {
            ps.addBatch();
            batchSize++;
            if (batchPortionSize > 0 && batchSize >= batchPortionSize) {
                ps.executeBatch();
                batchSize = 0;
            }
        }

        void close() throws SQLException {
            try {
                if (batchSize > 0) {
                    ps.executeBatch();
                }
            } catch (SQLException ex) {
                try {
                    ps.close();
                } catch (SQLException ex2) {
                    ex.addSuppressed(ex2);
                    throw ex;
                }
            }
            ps.close();
        }
    }

    private final Map<String, BatchedStatement> bySql = new HashMap<>();

    private final int batchPortionSize;

    /**
     * @param batchPortionSize
     * call {@link PreparedStatement#executeBatch()} each time when this limit is reached.
     * For example, insertintg 100 rows with batchPortionSize=20 results in calling executeUpdate 5 times
     * for batches of 20 statements each. If non-positive, executeUpdate is only called once at the end.
     *
     */
    public Batch(int batchPortionSize) {
        this.batchPortionSize = batchPortionSize;
    }

    public void addBatch(Connection connection, Query query) throws SQLException {
        String sql = query.getSql();
        if (SqlTestingHook.isTesting()) {
            query.executeUpdate(connection);
            return;
        }
        BatchedStatement bs = bySql.get(sql);
        if (bs == null) {
            bs = new BatchedStatement(connection.prepareStatement(sql));
            bySql.put(sql, bs);
        }
        query.setParameters(bs.ps);
        bs.addBatch(batchPortionSize);
    }

    public void addBatch(RowConnection connection, Query query) throws SQLException {
        addBatch(connection.getConnection(), query);
    }

    @Override
    public void close() throws SQLException {
        SQLException error = null;
        for (BatchedStatement bs : bySql.values()) {
            try {
                bs.close();
            } catch (SQLException ex) {
                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }
        }
        if (error != null) {
            throw error;
        }
    }
}
