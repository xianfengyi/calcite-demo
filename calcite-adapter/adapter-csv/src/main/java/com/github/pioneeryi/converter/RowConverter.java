package com.github.pioneeryi.converter;

import org.apache.calcite.avatica.util.DateTimeUtils;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.commons.lang3.time.FastDateFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class RowConverter<E> {

    private static final TimeZone gmt = TimeZone.getTimeZone("GMT");
    private static final FastDateFormat TIME_FORMAT_DATE = FastDateFormat.getInstance("yyyy-MM-dd", gmt);
    private static final FastDateFormat TIME_FORMAT_TIME = FastDateFormat.getInstance("HH:mm:ss", gmt);
    private static final FastDateFormat TIME_FORMAT_TIMESTAMP = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", gmt);

    public abstract E convertRow(String[] rows);

    protected Object convert(RelDataType fieldType, String string) {
        if (fieldType == null || string == null) {
            return string;
        }
        switch (fieldType.getSqlTypeName()) {
            case BOOLEAN:
                if (string.length() == 0) {
                    return null;
                }
                return Boolean.parseBoolean(string);
            case TINYINT:
                if (string.length() == 0) {
                    return null;
                }
                return Byte.parseByte(string);
            case SMALLINT:
                if (string.length() == 0) {
                    return null;
                }
                return Short.parseShort(string);
            case INTEGER:
                if (string.length() == 0) {
                    return null;
                }
                return Integer.parseInt(string);
            case BIGINT:
                if (string.length() == 0) {
                    return null;
                }
                return Long.parseLong(string);
            case FLOAT:
                if (string.length() == 0) {
                    return null;
                }
                return Float.parseFloat(string);
            case DOUBLE:
                if (string.length() == 0) {
                    return null;
                }
                return Double.parseDouble(string);
            case DECIMAL:
                if (string.length() == 0) {
                    return null;
                }
                return parseDecimal(fieldType.getPrecision(), fieldType.getScale(), string);
            case DATE:
                if (string.length() == 0) {
                    return null;
                }
                try {
                    Date date = TIME_FORMAT_DATE.parse(string);
                    return (int) (date.getTime() / DateTimeUtils.MILLIS_PER_DAY);
                } catch (ParseException e) {
                    return null;
                }
            case TIME:
                if (string.length() == 0) {
                    return null;
                }
                try {
                    Date date = TIME_FORMAT_TIME.parse(string);
                    return (int) date.getTime();
                } catch (ParseException e) {
                    return null;
                }
            case TIMESTAMP:
                if (string.length() == 0) {
                    return null;
                }
                try {
                    Date date = TIME_FORMAT_TIMESTAMP.parse(string);
                    return date.getTime();
                } catch (ParseException e) {
                    return null;
                }
            case VARCHAR:
            default:
                return string;
        }
    }

    protected BigDecimal parseDecimal(int precision, int scale, String string) {
        BigDecimal result = new BigDecimal(string);
        // If the parsed value has more fractional digits than the specified scale, round ties away
        // from 0.
        if (result.scale() > scale) {
            result = result.setScale(scale, RoundingMode.HALF_UP);
        }
        // Throws an exception if the parsed value has more digits to the left of the decimal point
        // than the specified value.
        if (result.precision() - result.scale() > precision - scale) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "Decimal value %s exceeds declared precision (%d) and scale (%d).", result, precision, scale));
        }
        return result;
    }
}
