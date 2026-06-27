package com.networth.api.service

import com.networth.api.dto.TransactionRequest
import com.networth.api.model.TransactionType
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.StringReader
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class CsvParserService {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val alternativeDateFormatters = listOf(
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ISO_LOCAL_DATE
    )

    fun parseTransactions(csvContent: String): CsvParseResult {
        val transactions = mutableListOf<TransactionRequest>()
        val errors = mutableListOf<String>()

        try {
            val reader = BufferedReader(StringReader(csvContent))
            val csvParser = CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())

            for (record in csvParser) {
                try {
                    val transaction = parseTransactionRecord(record)
                    transactions.add(transaction)
                } catch (e: Exception) {
                    errors.add("Row ${record.recordNumber}: ${e.message}")
                }
            }

            csvParser.close()
        } catch (e: Exception) {
            errors.add("Failed to parse CSV: ${e.message}")
        }

        return CsvParseResult(transactions, errors)
    }

    private fun parseTransactionRecord(record: org.apache.commons.csv.CSVRecord): TransactionRequest {
        val description = getRequiredField(record, "description", "desc", "name")
        val amountString = getRequiredField(record, "amount", "value", "price")
        val typeString = getRequiredField(record, "type", "transaction_type")
        val dateString = getRequiredField(record, "date", "transaction_date")
        val category = getOptionalField(record, "category", "category_name")
        val notes = getOptionalField(record, "notes", "note", "memo")

        val amount = parseAmount(amountString)
        val type = parseTransactionType(typeString)
        val date = parseDate(dateString)

        return TransactionRequest(
            description = description,
            amount = amount,
            type = type,
            date = date,
            category = category,
            notes = notes
        )
    }

    private fun getRequiredField(record: org.apache.commons.csv.CSVRecord, vararg possibleNames: String): String {
        for (name in possibleNames) {
            if (record.isMapped(name)) {
                val value = record.get(name).trim()
                if (value.isNotEmpty()) {
                    return value
                }
            }
        }
        throw IllegalArgumentException("Missing required field: ${possibleNames.joinToString(", ")}")
    }

    private fun getOptionalField(record: org.apache.commons.csv.CSVRecord, vararg possibleNames: String): String? {
        for (name in possibleNames) {
            if (record.isMapped(name)) {
                val value = record.get(name).trim()
                if (value.isNotEmpty()) {
                    return value
                }
            }
        }
        return null
    }

    private fun parseAmount(amountString: String): BigDecimal {
        return try {
            val cleaned = amountString.replace(Regex("[^\\d.-]"), "")
            BigDecimal(cleaned)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid amount format: $amountString")
        }
    }

    private fun parseTransactionType(typeString: String): TransactionType {
        return when (typeString.lowercase()) {
            "income", "in", "credit", "deposit", "+" -> TransactionType.INCOME
            "expense", "out", "debit", "withdrawal", "-" -> TransactionType.EXPENSE
            else -> throw IllegalArgumentException("Invalid transaction type: $typeString. Must be 'income' or 'expense'")
        }
    }

    private fun parseDate(dateString: String): LocalDate {
        // Try primary format first
        try {
            return LocalDate.parse(dateString, dateFormatter)
        } catch (e: DateTimeParseException) {
            // Try alternative formats
            for (formatter in alternativeDateFormatters) {
                try {
                    return LocalDate.parse(dateString, formatter)
                } catch (e: DateTimeParseException) {
                    // Continue to next formatter
                }
            }
            throw IllegalArgumentException("Invalid date format: $dateString. Expected format: yyyy-MM-dd")
        }
    }

    data class CsvParseResult(
        val transactions: List<TransactionRequest>,
        val errors: List<String>
    ) {
        val hasErrors: Boolean
            get() = errors.isNotEmpty()

        val successCount: Int
            get() = transactions.size

        val errorCount: Int
            get() = errors.size
    }
}
