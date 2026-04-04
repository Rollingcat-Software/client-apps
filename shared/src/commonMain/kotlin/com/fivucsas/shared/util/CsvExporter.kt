package com.fivucsas.shared.util

/**
 * Generic CSV export utility for converting data objects to CSV format.
 *
 * Handles RFC 4180 compliant escaping: double-quotes, commas, and newlines
 * within field values are properly escaped.
 *
 * Design Principles Applied:
 * - Single Responsibility Principle: Only handles CSV serialization
 * - Open/Closed Principle: Generic — works with any data type via extractors
 * - KISS: Simple, stateless utility
 */
object CsvExporter {

    /**
     * Column definition for CSV export.
     * @param header The column header text
     * @param extractor Function to extract the column value from a data object
     */
    data class Column<T>(
        val header: String,
        val extractor: (T) -> String
    )

    /**
     * Convert a list of data objects to a CSV string.
     *
     * @param data The list of objects to export
     * @param columns Column definitions (headers + value extractors)
     * @return RFC 4180 compliant CSV string with BOM for Excel compatibility
     */
    fun <T> toCsv(data: List<T>, columns: List<Column<T>>): String {
        val sb = StringBuilder()

        // UTF-8 BOM for Excel compatibility
        sb.append('\uFEFF')

        // Header row
        sb.appendLine(columns.joinToString(",") { escapeField(it.header) })

        // Data rows
        for (item in data) {
            sb.appendLine(columns.joinToString(",") { col ->
                escapeField(col.extractor(item))
            })
        }

        return sb.toString()
    }

    /**
     * Escape a CSV field value per RFC 4180.
     * Fields containing commas, double-quotes, or newlines are wrapped in quotes.
     * Double-quotes within the field are escaped by doubling them.
     */
    fun escapeField(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    /**
     * Pre-built column definitions for AuditLog export.
     */
    fun auditLogColumns(): List<Column<AuditLogExportRow>> = listOf(
        Column("ID") { it.id },
        Column("Timestamp") { it.timestamp },
        Column("User ID") { it.userId },
        Column("Action") { it.action },
        Column("Status") { it.status },
        Column("IP Address") { it.ipAddress },
        Column("Details") { it.details }
    )

    /**
     * Pre-built column definitions for Analytics/Statistics export.
     */
    fun statisticsColumns(): List<Column<StatisticsExportRow>> = listOf(
        Column("Metric") { it.metric },
        Column("Value") { it.value }
    )
}

/**
 * Flat export row for audit log entries.
 * Decouples CSV export from the domain model.
 */
data class AuditLogExportRow(
    val id: String,
    val timestamp: String,
    val userId: String,
    val action: String,
    val status: String,
    val ipAddress: String,
    val details: String
)

/**
 * Key-value row for statistics export.
 */
data class StatisticsExportRow(
    val metric: String,
    val value: String
)
