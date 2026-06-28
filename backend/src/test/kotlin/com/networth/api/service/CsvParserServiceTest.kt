package com.networth.api.service

import com.networth.api.model.TransactionType
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CsvParserServiceTest {

    private val csvParserService = CsvParserService()

    @Test
    fun `parseTransactions should parse valid CSV successfully`() {
        // Arrange
        val csvContent = """
            description,amount,type,date,category,notes
            Grocery shopping,50.00,expense,2024-01-15,Groceries,Weekly groceries
            Salary,5000.00,income,2024-01-31,Salary,Monthly salary
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(2, result.successCount)
        assertEquals(0, result.errorCount)
        assertFalse(result.hasErrors)

        val transaction1 = result.transactions[0]
        assertEquals("Grocery shopping", transaction1.description)
        assertEquals(BigDecimal("50.00"), transaction1.amount)
        assertEquals(TransactionType.EXPENSE, transaction1.type)
        assertEquals(LocalDate.of(2024, 1, 15), transaction1.date)
        assertEquals("Groceries", transaction1.category)
        assertEquals("Weekly groceries", transaction1.notes)

        val transaction2 = result.transactions[1]
        assertEquals("Salary", transaction2.description)
        assertEquals(BigDecimal("5000.00"), transaction2.amount)
        assertEquals(TransactionType.INCOME, transaction2.type)
    }

    @Test
    fun `parseTransactions should handle alternative field names`() {
        // Arrange
        val csvContent = """
            desc,value,transaction_type,transaction_date,category_name,memo
            Coffee,5.50,expense,2024-01-16,Food,Morning coffee
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(1, result.successCount)
        assertEquals(0, result.errorCount)

        val transaction = result.transactions[0]
        assertEquals("Coffee", transaction.description)
        assertEquals(BigDecimal("5.50"), transaction.amount)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(LocalDate.of(2024, 1, 16), transaction.date)
        assertEquals("Food", transaction.category)
        assertEquals("Morning coffee", transaction.notes)
    }

    @Test
    fun `parseTransactions should handle different date formats`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Test1,100.00,expense,2024-01-15
            Test2,200.00,income,01/15/2024
            Test3,300.00,expense,15/01/2024
            Test4,400.00,income,2024/01/15
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(4, result.successCount)
        assertEquals(0, result.errorCount)

        for (transaction in result.transactions) {
            assertEquals(LocalDate.of(2024, 1, 15), transaction.date)
        }
    }

    @Test
    fun `parseTransactions should handle transaction type aliases`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Income1,100.00,income,2024-01-15
            Income2,200.00,IN,2024-01-16
            Income3,300.00,credit,2024-01-17
            Income4,400.00,deposit,2024-01-18
            Income5,500.00,+,2024-01-19
            Expense1,50.00,expense,2024-01-20
            Expense2,60.00,OUT,2024-01-21
            Expense3,70.00,debit,2024-01-22
            Expense4,80.00,withdrawal,2024-01-23
            Expense5,90.00,-,2024-01-24
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(10, result.successCount)
        assertEquals(0, result.errorCount)

        val incomeTransactions = result.transactions.take(5)
        for (transaction in incomeTransactions) {
            assertEquals(TransactionType.INCOME, transaction.type)
        }

        val expenseTransactions = result.transactions.drop(5)
        for (transaction in expenseTransactions) {
            assertEquals(TransactionType.EXPENSE, transaction.type)
        }
    }

    @Test
    fun `parseTransactions should handle amounts with currency symbols`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Test1,$100.00,expense,2024-01-15
            Test2,€200.00,income,2024-01-16
            Test3,£300.50,expense,2024-01-17
            Test4,¥400.75,income,2024-01-18
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(4, result.successCount)
        assertEquals(0, result.errorCount)

        assertEquals(BigDecimal("100.00"), result.transactions[0].amount)
        assertEquals(BigDecimal("200.00"), result.transactions[1].amount)
        assertEquals(BigDecimal("300.50"), result.transactions[2].amount)
        assertEquals(BigDecimal("400.75"), result.transactions[3].amount)
    }

    @Test
    fun `parseTransactions should handle optional fields`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Test1,100.00,expense,2024-01-15
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(1, result.successCount)
        assertEquals(0, result.errorCount)

        val transaction = result.transactions[0]
        assertEquals("Test1", transaction.description)
        assertNull(transaction.category)
        assertNull(transaction.notes)
    }

    @Test
    fun `parseTransactions should report error for missing required field`() {
        // Arrange
        val csvContent = """
            description,type,date
            Grocery shopping,expense,2024-01-15
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(0, result.successCount)
        assertEquals(1, result.errorCount)
        assertTrue(result.hasErrors)
        assertTrue(result.errors[0].contains("Missing required field"))
    }

    @Test
    fun `parseTransactions should report error for invalid amount`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Test,invalid,expense,2024-01-15
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(0, result.successCount)
        assertEquals(1, result.errorCount)
        assertTrue(result.errors[0].contains("Invalid amount format"))
    }

    @Test
    fun `parseTransactions should report error for invalid transaction type`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Test,100.00,invalid,2024-01-15
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(0, result.successCount)
        assertEquals(1, result.errorCount)
        assertTrue(result.errors[0].contains("Invalid transaction type"))
    }

    @Test
    fun `parseTransactions should report error for invalid date format`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Test,100.00,expense,invalid-date
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(0, result.successCount)
        assertEquals(1, result.errorCount)
        assertTrue(result.errors[0].contains("Invalid date format"))
    }

    @Test
    fun `parseTransactions should handle mixed valid and invalid rows`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Valid1,100.00,expense,2024-01-15
            Invalid,invalid,expense,2024-01-16
            Valid2,200.00,income,2024-01-17
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(2, result.successCount)
        assertEquals(1, result.errorCount)
        assertTrue(result.hasErrors)

        assertEquals("Valid1", result.transactions[0].description)
        assertEquals("Valid2", result.transactions[1].description)
    }

    @Test
    fun `parseTransactions should handle empty CSV`() {
        // Arrange
        val csvContent = ""

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(0, result.successCount)
        assertEquals(1, result.errorCount)
        assertTrue(result.errors[0].contains("Failed to parse CSV"))
    }

    @Test
    fun `parseTransactions should handle CSV with only headers`() {
        // Arrange
        val csvContent = "description,amount,type,date"

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(0, result.successCount)
        assertEquals(0, result.errorCount)
    }

    @Test
    fun `parseTransactions should trim whitespace from fields`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
             Grocery shopping  ,  50.00  ,  expense  ,  2024-01-15  
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(1, result.successCount)
        assertEquals("Grocery shopping", result.transactions[0].description)
        assertEquals(BigDecimal("50.00"), result.transactions[0].amount)
    }

    @Test
    fun `parseTransactions should handle case-insensitive headers`() {
        // Arrange
        val csvContent = """
            DESCRIPTION,AMOUNT,TYPE,DATE
            Test,100.00,expense,2024-01-15
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(1, result.successCount)
        assertEquals("Test", result.transactions[0].description)
    }

    @Test
    fun `parseTransactions should handle negative amounts`() {
        // Arrange
        val csvContent = """
            description,amount,type,date
            Test,-50.00,expense,2024-01-15
        """.trimIndent()

        // Act
        val result = csvParserService.parseTransactions(csvContent)

        // Assert
        assertEquals(1, result.successCount)
        assertEquals(BigDecimal("-50.00"), result.transactions[0].amount)
    }
}
