package jdbq.mapping;

import java.lang.reflect.RecordComponent;
import java.util.Objects;

public interface ColumnNaming {

    default String sqlName(RecordComponent component) {
        SqlName sqlName = component.getDeclaredAnnotation(SqlName.class);
        if (sqlName != null) {
            return sqlName.value();
        }
        return sqlName(component.getName());
    }

    String sqlName(String javaName);

    static ColumnNaming byPosition() {
        return null;
    }

    static ColumnNaming raw() {
        return javaName -> javaName;
    }

    static ColumnNaming camelCase() {
        return javaName -> {
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
        };
    }
}
