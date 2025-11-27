package jdbq.mapping;

import java.lang.reflect.RecordComponent;
import java.util.Objects;

public interface ColumnNaming {

    default boolean useNames() {
        return true;
    }

    default String sqlName(RecordComponent component) {
        SqlName sqlName = component.getDeclaredAnnotation(SqlName.class);
        if (sqlName != null) {
            return sqlName.value();
        }
        return sqlName(component.getName());
    }

    String sqlName(String javaName);

    final class ByPosition implements ColumnNaming {

        @Override
        public boolean useNames() {
            return false;
        }

        @Override
        public String sqlName(String javaName) {
            throw new IllegalStateException();
        }
    }

    final class Raw implements ColumnNaming {

        @Override
        public String sqlName(String javaName) {
            return javaName;
        }
    }

    final class CamelCase implements ColumnNaming {

        @Override
        public String sqlName(String javaName) {
            Boolean prevLowerCase = null;
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < javaName.length(); i++) {
                char ch = javaName.charAt(i);
                Boolean lowerCase;
                if (Character.isLowerCase(ch)) {
                    lowerCase = true;
                } else if (Character.isUpperCase(ch)) {
                    lowerCase = false;
                } else {
                    lowerCase = null;
                }
                if (i > 0 && lowerCase != null && !lowerCase.booleanValue() && !Objects.equals(lowerCase, prevLowerCase)) {
                    buf.append('_');
                }
                buf.append(ch);
                prevLowerCase = lowerCase;
            }
            return buf.toString().toLowerCase();
        }
    }

    static ColumnNaming byPosition() {
        return new ByPosition();
    }

    static ColumnNaming raw() {
        return new Raw();
    }

    static ColumnNaming camelCase() {
        return new CamelCase();
    }
}
