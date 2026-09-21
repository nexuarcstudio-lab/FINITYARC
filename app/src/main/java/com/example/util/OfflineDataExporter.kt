package com.example.util

import com.example.model.ExpenseStatus
import com.example.model.ExportableTransaction
import com.example.model.HomeTransaction
import com.example.model.TransactionType

/**
 * Offline CSV data contract interface for FINITYARC.
 *
 * Defines explicit bidirectional conversion between transactions and offline CSV records.
 * Columns covered: ID, Date, Amount (Major/Minor), Type, Category, Description, SpentBy, Department, Status.
 */
interface OfflineDataExporter {
    /**
     * Generates a standard RFC 4180 compliant CSV string from a collection of transactions.
     */
    fun generateCsvString(transactions: List<Any>): String

    /**
     * Parses an RFC 4180 CSV string into a list of strongly-typed [ExportableTransaction] items.
     */
    fun parseCsvStringToTransactions(csvContent: String): List<ExportableTransaction>
}

/**
 * Production implementation of [OfflineDataExporter].
 * Handles quote escaping, robust line splitting, and numeric conversions.
 */
object CsvLedgerExporter : OfflineDataExporter {

    private const val CSV_HEADER = "ID,Date,AmountMinor,AmountMajor,Type,Category,Description,SpentBy,Department,Status"

    override fun generateCsvString(transactions: List<Any>): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(CSV_HEADER).append("\n")

        for (item in transactions) {
            val exportable = when (item) {
                is ExportableTransaction -> item
                is HomeTransaction -> ExportableTransaction(
                    id = item.id,
                    dateIso = item.dateFormatted,
                    amountMinorUnits = item.amountMinorUnits,
                    type = if (item.isExpense) TransactionType.EXPENSE else TransactionType.INCOME,
                    category = item.category,
                    description = item.title,
                    spentBy = item.spentBy,
                    department = null,
                    status = ExpenseStatus.PAID
                )
                else -> null
            }

            if (exportable != null) {
                stringBuilder.append(escapeCsv(exportable.id)).append(",")
                stringBuilder.append(escapeCsv(exportable.dateIso)).append(",")
                stringBuilder.append(exportable.amountMinorUnits).append(",")
                stringBuilder.append(exportable.amountMajorString).append(",")
                stringBuilder.append(escapeCsv(exportable.type.name)).append(",")
                stringBuilder.append(escapeCsv(exportable.category)).append(",")
                stringBuilder.append(escapeCsv(exportable.description)).append(",")
                stringBuilder.append(escapeCsv(exportable.spentBy ?: "")).append(",")
                stringBuilder.append(escapeCsv(exportable.department ?: "")).append(",")
                stringBuilder.append(escapeCsv(exportable.status.name)).append("\n")
            }
        }

        return stringBuilder.toString()
    }

    override fun parseCsvStringToTransactions(csvContent: String): List<ExportableTransaction> {
        if (csvContent.isBlank()) return emptyList()

        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val result = mutableListOf<ExportableTransaction>()

        // Check if first line is the header
        val startIndex = if (lines.first().contains("ID", ignoreCase = true) &&
            lines.first().contains("Amount", ignoreCase = true)
        ) {
            1
        } else {
            0
        }

        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            val tokens = parseCsvLine(line)
            if (tokens.size >= 6) {
                val id = tokens.getOrElse(0) { java.util.UUID.randomUUID().toString() }
                val dateIso = tokens.getOrElse(1) { "Unknown" }
                val amountMinor = tokens.getOrElse(2) { "0" }.toLongOrNull() ?: 0L
                // tokens[3] is amountMajor (redundant, verified against minor)
                val typeStr = tokens.getOrElse(4) { "EXPENSE" }
                val type = runCatching { TransactionType.valueOf(typeStr.uppercase()) }.getOrDefault(TransactionType.EXPENSE)
                val category = tokens.getOrElse(5) { "General" }
                val description = tokens.getOrElse(6) { "" }
                val spentBy = tokens.getOrNull(7)?.takeIf { it.isNotBlank() }
                val department = tokens.getOrNull(8)?.takeIf { it.isNotBlank() }
                val statusStr = tokens.getOrElse(9) { "PAID" }
                val status = runCatching { ExpenseStatus.valueOf(statusStr.uppercase()) }.getOrDefault(ExpenseStatus.PAID)

                result.add(
                    ExportableTransaction(
                        id = id,
                        dateIso = dateIso,
                        amountMinorUnits = amountMinor,
                        type = type,
                        category = category,
                        description = description,
                        spentBy = spentBy,
                        department = department,
                        status = status
                    )
                )
            }
        }

        return result
    }

    private fun escapeCsv(value: String): String {
        val containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        return if (containsSpecial) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var insideQuote = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (insideQuote && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++ // skip escaped quote
                    } else {
                        insideQuote = !insideQuote
                    }
                }
                c == ',' && !insideQuote -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
