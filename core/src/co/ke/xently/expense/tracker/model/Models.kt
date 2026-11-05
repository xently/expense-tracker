package co.ke.xently.expense.tracker.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ProjectUser(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String
)

@Serializable
data class Project(
    val id: String,
    val name: String,
    val description: String,
    val ownerUserId: String,
    val createdAtEpochMs: Long
)

@Serializable
enum class ProjectMemberRole {
    OWNER,
    PARTNER
}

@Serializable
data class ProjectMembership(
    val projectId: String,
    val userId: String,
    val role: ProjectMemberRole,
    val joinedAtEpochMs: Long
)

@Serializable
enum class ProjectInvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

@Serializable
data class ProjectInvitation(
    val id: String,
    val projectId: String,
    val projectName: String,
    val inviterUserId: String,
    val inviterUsername: String,
    val inviteeUserId: String,
    val inviteeUsername: String,
    val status: ProjectInvitationStatus,
    val createdAtEpochMs: Long
)

@Serializable
data class MyProjectsResponse(
    val projects: List<Project>,
    val defaultProjectId: String?
)

@Serializable
data class RegistrationRequest(
    val username: String,
    val displayName: String
)

@Serializable
data class CreateProjectRequest(
    val name: String,
    val description: String = ""
)

@Serializable
data class InviteProjectPartnerRequest(
    val username: String
)

@Serializable
data class RespondToInvitationRequest(
    val accept: Boolean
)

@Serializable
enum class PartnerId(val displayName: String, val email: String) {
    HARRISON("Harrison", "harrison@farm.co.ke"),
    HERMAN("Herman", "herman@farm.co.ke"),
    VIN("Vin", "vin@farm.co.ke");

    companion object {
        fun fromString(value: String): PartnerId? {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                        it.displayName.equals(value, ignoreCase = true)
            }
        }
    }
}

@Serializable
data class Partner(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val fcmToken: String? = null
)

@Serializable
enum class ExpenseCategory(val displayName: String) {
    LAND_PREP("Land Prep"),
    SEEDLINGS("Seedlings"),
    WEEDING("Weeding"),
    FERTILIZER("Fertilizer"),
    CHOPPING("Chopping"),
    TRANSPORT("Transport"),
    HARVEST_OPERATIONS("Harvest Operations"),
    MISCELLANEOUS("Miscellaneous");

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                        it.displayName.equals(value, ignoreCase = true)
            } ?: MISCELLANEOUS
        }
    }
}

@Serializable
data class ExpenseSplit(
    val partnerId: String,
    val shareAmount: Double,
    val dueDate: LocalDate
)

@Serializable
data class Expense(
    val id: String,
    val title: String,
    val category: ExpenseCategory,
    val totalAmount: Double,
    val currency: String = "KES",
    val transactionDate: LocalDate,
    val payingPartnerId: String,
    val notes: String = "",
    val splits: List<ExpenseSplit> = emptyList(),
    val createdAtEpochMs: Long = 0L
)

@Serializable
data class TranchePayment(
    val id: String,
    val partnerId: String,
    val amount: Double,
    val paymentDate: LocalDate,
    val notes: String = "",
    val referenceNumber: String = "",
    val createdAtEpochMs: Long = 0L
)

@Serializable
enum class ComplianceStatus(val displayName: String) {
    PAID_IN_FULL("Paid in Full"),
    PARTIAL("Partial"),
    OVERDUE("Overdue")
}

@Serializable
data class PartnerLedgerSummary(
    val partnerId: String,
    val partnerName: String,
    val directOutOfPocket: Double,
    val cashTranches: Double,
    val totalContributed: Double,
    val totalObligated: Double,
    val netBalance: Double, // totalContributed - totalObligated (positive = surplus/credit, negative = owes)
    val overdueAmount: Double,
    val complianceStatus: ComplianceStatus,
    val investmentSharePercentage: Double
)

@Serializable
data class PartnerSettlement(
    val partnerId: String,
    val partnerName: String,
    val actualInvestment: Double,
    val investmentSharePercentage: Double,
    val grossPayoutShare: Double,
    val netPayoutShare: Double,
    val directOutOfPocketRecovery: Double,
    val finalTotalPayout: Double,
    val netProfit: Double,
    val returnOnInvestmentPercentage: Double
)

@Serializable
data class SeasonRevenueCalculation(
    val grossFactoryPayout: Double,
    val harvestOperationsExpenses: Double,
    val netFactoryPayout: Double,
    val totalProjectInvestment: Double,
    val partnerSettlements: List<PartnerSettlement>
)

@Serializable
data class AppNotification(
    val id: String,
    val partnerId: String,
    val partnerName: String,
    val title: String,
    val message: String,
    val amountOverdue: Double,
    val monthYear: String,
    val timestampEpochMs: Long,
    val isSent: Boolean = true,
    val fcmMessageId: String? = null
)

@Serializable
data class DashboardSummary(
    val totalExpenses: Double,
    val totalContributions: Double,
    val totalOutstandingObligations: Double,
    val totalOverdueAmount: Double,
    val partnerSummaries: List<PartnerLedgerSummary>,
    val categoryBreakdowns: Map<ExpenseCategory, Double>,
    val recentExpenses: List<Expense>,
    val recentTranches: List<TranchePayment>,
    val notifications: List<AppNotification>
)

@Serializable
sealed class ServerEvent {
    @Serializable
    data class ExpenseCreated(val expense: Expense) : ServerEvent()

    @Serializable
    data class ExpenseDeleted(val expenseId: String) : ServerEvent()

    @Serializable
    data class TranchePaymentRecorded(val payment: TranchePayment) : ServerEvent()

    @Serializable
    data class NotificationDispatched(val notification: AppNotification) : ServerEvent()

    @Serializable
    data class LedgerUpdated(val summary: DashboardSummary) : ServerEvent()
}
