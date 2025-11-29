package jdby.testing;

import jdby.core.Batch;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.function.Supplier;

public class MockParamFactory {

    public Object mockParameter(Type type, Supplier<Object> fallback) {
        if (type == int.class || type == Integer.class) {
            return 1;
        } else if (type == long.class || type == Long.class) {
            return 1L;
        } else if (type == double.class || type == Double.class) {
            return 1.0;
        } else if (type == byte.class || type == Byte.class) {
            return (byte) 1;
        } else if (type == short.class || type == Short.class) {
            return (short) 1;
        } else if (type == float.class || type == Float.class) {
            return 1.0f;
        } else if (type == BigDecimal.class) {
            return BigDecimal.ONE;
        } else if (type == String.class) {
            return "1";
        } else if (type == LocalDate.class) {
            return LocalDate.now();
        } else if (type == OffsetDateTime.class) {
            return OffsetDateTime.now();
        } else if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (type == LocalTime.class) {
            return LocalTime.now();
        } else if (type == byte[].class) {
            return new byte[] {1};
        } else if (type == boolean.class || type == Boolean.class) {
            return true;
        } else if (type == Batch.class) {
            return new Batch(0);
        } else {
            return fallback.get();
        }
    }
}
