package com.example.global.config.p6spy;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * P6spy SQL 포맷 설정
 */
public class P6spyPrettySqlFormatter implements MessageFormattingStrategy {

    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss");

    @Override
    public String formatMessage(final int connectionId, final String now, final long elapsed, final String category, final String prepared, final String sql, final String url) {
        final String formattedSql = formatSql(category, sql);
        final String logTime = LocalDateTime.now().format(LOG_TIME_FORMATTER);

        final String safeSql = formattedSql == null ? "" : formattedSql;
        return "%s | OperationTime : %dms%s".formatted(logTime, elapsed, safeSql);
    }

    private String formatSql(final String category, final String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        if (!Category.STATEMENT.getName().equals(category)) {
            return sql;
        }

        final String normalizedSql = sql.trim().toLowerCase(Locale.ROOT);
        final boolean isDdl = normalizedSql.startsWith("create")
                || normalizedSql.startsWith("alter")
                || normalizedSql.startsWith("comment");
        final String formatted = isDdl
                ? FormatStyle.DDL.getFormatter().format(sql)
                : FormatStyle.BASIC.getFormatter().format(sql);

        return """
                |
                FormattedSql(P6Spy sql, Hibernate format):%s
                """.formatted(formatted);
    }
}
