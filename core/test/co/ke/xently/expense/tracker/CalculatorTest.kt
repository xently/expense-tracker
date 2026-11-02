package co.ke.xently.expense.tracker

import co.ke.xently.expense.tracker.calculator.ExpenseCalculator
import co.ke.xently.expense.tracker.calculator.LedgerCalculator
import co.ke.xently.expense.tracker.calculator.RevenueSplitCalculator
import co.ke.xently.expense.tracker.model.ComplianceStatus
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.PartnerId
import co.ke.xently.expense.tracker.model.PartnerLedgerSummary
import co.ke.xently.expense.tracker.model.TranchePayment
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatorTest {

    @Test
    fun testDueDateCalculation() {
        val dateMay = LocalDate(2026, 5, 15)
        val dueDateMay = ExpenseCalculator.calculateDueDate(dateMay)
        assertEquals(LocalDate(2026, 6, 10), dueDateMay)

        val dateDec = LocalDate(2026, 12, 4)
        val dueDateDec = ExpenseCalculator.calculateDueDate(dateDec)
        assertEquals(LocalDate(2027, 1, 10), dueDateDec)
    }

    @Test
    fun testEqualSplitsCalculation() {
        val total = 30000.0
        val date = LocalDate(2026, 5, 1)
        val splits = ExpenseCalculator.calculateSplits(total, date)

        assertEquals(3, splits.size)
        assertEquals(10000.0, splits[0].shareAmount)
        assertEquals(10000.0, splits[1].shareAmount)
        assertEquals(10000.0, splits[2].shareAmount)
        assertEquals(LocalDate(2026, 6, 10), splits[0].dueDate)
        assertEquals(30000.0, splits.sumOf { it.shareAmount })
    }

    @Test
    fun testLedgerBalancesAndComplianceStatus() {
        val date1 = LocalDate(2026, 5, 1)
        val exp1 = Expense(
            id = "exp-1",
            title = "Fertilizer purchase",
            category = ExpenseCategory.FERTILIZER,
            totalAmount = 90000.0,
            transactionDate = date1,
            payingPartnerId = PartnerId.HARRISON.name,
            splits = ExpenseCalculator.calculateSplits(90000.0, date1)
        )

        // Harrison paid 90,000 out of pocket. Obligation = 30,000 each.
        // Herman pays a tranche of 30,000.
        // Vin pays nothing.
        val trancheHerman = TranchePayment(
            id = "tr-1",
            partnerId = PartnerId.HERMAN.name,
            amount = 30000.0,
            paymentDate = LocalDate(2026, 5, 15)
        )

        val asOfDate = LocalDate(2026, 6, 15) // Past June 10 due date
        val summaries = LedgerCalculator.computePartnerSummaries(
            expenses = listOf(exp1),
            tranches = listOf(trancheHerman),
            asOfDate = asOfDate
        )

        val harrison = summaries.first { it.partnerId == PartnerId.HARRISON.name }
        val herman = summaries.first { it.partnerId == PartnerId.HERMAN.name }
        val vin = summaries.first { it.partnerId == PartnerId.VIN.name }

        // Harrison: contributed 90000, obligated 30000, netBalance = +60000, Paid in Full
        assertEquals(90000.0, harrison.totalContributed)
        assertEquals(30000.0, harrison.totalObligated)
        assertEquals(60000.0, harrison.netBalance)
        assertEquals(ComplianceStatus.PAID_IN_FULL, harrison.complianceStatus)

        // Herman: contributed 30000, obligated 30000, netBalance = 0, Paid in Full
        assertEquals(30000.0, herman.totalContributed)
        assertEquals(30000.0, herman.totalObligated)
        assertEquals(0.0, herman.netBalance)
        assertEquals(ComplianceStatus.PAID_IN_FULL, herman.complianceStatus)

        // Vin: contributed 0, obligated 30000, netBalance = -30000, Overdue (since asOfDate > June 10)
        assertEquals(0.0, vin.totalContributed)
        assertEquals(30000.0, vin.totalObligated)
        assertEquals(-30000.0, vin.netBalance)
        assertEquals(30000.0, vin.overdueAmount)
        assertEquals(ComplianceStatus.OVERDUE, vin.complianceStatus)
    }

    @Test
    fun testRevenueSplitCalculation() {
        val summaries = listOf(
            PartnerLedgerSummary(
                partnerId = PartnerId.HARRISON.name,
                partnerName = "Harrison",
                directOutOfPocket = 60000.0,
                cashTranches = 0.0,
                totalContributed = 60000.0,
                totalObligated = 40000.0,
                netBalance = 20000.0,
                overdueAmount = 0.0,
                complianceStatus = ComplianceStatus.PAID_IN_FULL,
                investmentSharePercentage = 50.0
            ),
            PartnerLedgerSummary(
                partnerId = PartnerId.HERMAN.name,
                partnerName = "Herman",
                directOutOfPocket = 0.0,
                cashTranches = 36000.0,
                totalContributed = 36000.0,
                totalObligated = 40000.0,
                netBalance = -4000.0,
                overdueAmount = 0.0,
                complianceStatus = ComplianceStatus.PARTIAL,
                investmentSharePercentage = 30.0
            ),
            PartnerLedgerSummary(
                partnerId = PartnerId.VIN.name,
                partnerName = "Vin",
                directOutOfPocket = 0.0,
                cashTranches = 24000.0,
                totalContributed = 24000.0,
                totalObligated = 40000.0,
                netBalance = -16000.0,
                overdueAmount = 0.0,
                complianceStatus = ComplianceStatus.PARTIAL,
                investmentSharePercentage = 20.0
            )
        )

        val grossPayout = 300000.0
        val harvestCosts = 50000.0 // Net = 250,000.0

        val calc = RevenueSplitCalculator.calculate(grossPayout, harvestCosts, summaries)
        assertEquals(250000.0, calc.netFactoryPayout)
        assertEquals(120000.0, calc.totalProjectInvestment)

        val harrison = calc.partnerSettlements.first { it.partnerId == PartnerId.HARRISON.name }
        val herman = calc.partnerSettlements.first { it.partnerId == PartnerId.HERMAN.name }
        val vin = calc.partnerSettlements.first { it.partnerId == PartnerId.VIN.name }

        assertEquals(50.0, harrison.investmentSharePercentage)
        assertEquals(125000.0, harrison.netPayoutShare)
        assertEquals(65000.0, harrison.netProfit)

        assertEquals(30.0, herman.investmentSharePercentage)
        assertEquals(75000.0, herman.netPayoutShare)
        assertEquals(39000.0, herman.netProfit)

        assertEquals(20.0, vin.investmentSharePercentage)
        assertEquals(50000.0, vin.netPayoutShare)
        assertEquals(26000.0, vin.netProfit)
    }
}
