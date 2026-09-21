package com.example

import com.example.model.ExpenseStatus
import com.example.model.ExportableTransaction
import com.example.model.TransactionType
import com.example.util.CsvLedgerExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the offline CSV contract implementation.
 */
class OfflineCsvContractTest {

    @Test
    fun `generateCsvString creates standard header and correct escaped rows`() {
        val sampleTransactions = listOf(
            ExportableTransaction(
                id = "tx-101",
                dateIso = "2026-09-21",
                amountMinorUnits = 15000L,
                type = TransactionType.EXPENSE,
                category = "SaaS & Software",
                description = "Google Cloud, Compute & Storage",
                spentBy = "Sarah Lead",
                department = "Engineering",
                status = ExpenseStatus.PAID
            ),
            ExportableTransaction(
                id = "tx-102",
                dateIso = "2026-09-20",
                amountMinorUnits = 500000L,
                type = TransactionType.INCOME,
                category = "Client Retainer",
                description = "Acme Corp Q3 Payment",
                spentBy = null,
                department = null,
                status = ExpenseStatus.PAID
            )
        )

        val csv = CsvLedgerExporter.generateCsvString(sampleTransactions)

        assertTrue(csv.startsWith("ID,Date,AmountMinor,AmountMajor,Type,Category,Description,SpentBy,Department,Status"))
        assertTrue(csv.contains("tx-101"))
        assertTrue(csv.contains("15000,150.00,EXPENSE"))
        // Check quotes escaping on comma in description
        assertTrue(csv.contains("\"Google Cloud, Compute & Storage\""))
        assertTrue(csv.contains("tx-102"))
        assertTrue(csv.contains("500000,5000.00,INCOME"))
    }

    @Test
    fun `parseCsvStringToTransactions parses serialized CSV back into strongly typed models`() {
        val rawCsv = """
            ID,Date,AmountMinor,AmountMajor,Type,Category,Description,SpentBy,Department,Status
            tx-201,2026-09-18,1250,12.50,EXPENSE,Food & Dining,Lunch bistro,Ali,Design,PAID
            tx-202,2026-09-19,84000,840.00,EXPENSE,Hardware & Office,"Desk, monitor, chair",Kareem,Ops,REIMBURSABLE
        """.trimIndent()

        val parsed = CsvLedgerExporter.parseCsvStringToTransactions(rawCsv)

        assertEquals(2, parsed.size)

        val first = parsed[0]
        assertEquals("tx-201", first.id)
        assertEquals("2026-09-18", first.dateIso)
        assertEquals(1250L, first.amountMinorUnits)
        assertEquals(TransactionType.EXPENSE, first.type)
        assertEquals("Food & Dining", first.category)
        assertEquals("Lunch bistro", first.description)
        assertEquals("Ali", first.spentBy)
        assertEquals("Design", first.department)
        assertEquals(ExpenseStatus.PAID, first.status)

        val second = parsed[1]
        assertEquals("tx-202", second.id)
        assertEquals("Desk, monitor, chair", second.description)
        assertEquals(ExpenseStatus.REIMBURSABLE, second.status)
    }

    @Test
    fun `bidirectional roundtrip preserves all financial fields and minor units`() {
        val original = listOf(
            ExportableTransaction(
                id = "roundtrip-1",
                dateIso = "2026-09-21",
                amountMinorUnits = 999999L,
                type = TransactionType.TRANSFER,
                category = "Treasury Reserve",
                description = "Internal liquidity transfer",
                spentBy = "CFO Office",
                department = "Finance",
                status = ExpenseStatus.PAID
            )
        )

        val exportedCsv = CsvLedgerExporter.generateCsvString(original)
        val imported = CsvLedgerExporter.parseCsvStringToTransactions(exportedCsv)

        assertEquals(1, imported.size)
        assertEquals(original[0].id, imported[0].id)
        assertEquals(original[0].amountMinorUnits, imported[0].amountMinorUnits)
        assertEquals(original[0].type, imported[0].type)
        assertEquals(original[0].category, imported[0].category)
        assertEquals(original[0].description, imported[0].description)
        assertEquals(original[0].spentBy, imported[0].spentBy)
        assertEquals(original[0].department, imported[0].department)
        assertEquals(original[0].status, imported[0].status)
    }
}
