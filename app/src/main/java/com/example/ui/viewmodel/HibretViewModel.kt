package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HibretRepository
import com.example.model.AppLanguage
import com.example.model.BudgetItem
import com.example.model.Campaign
import com.example.model.CampaignCategory
import com.example.model.Donation
import com.example.model.DraftCampaign
import com.example.model.NotificationItem
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    LOGIN,
    REGISTRATION,
    HOME,
    EXPLORE, // The CAMPAIGN bottom nav destination
    CAMPAIGN_DETAIL,
    DONATION_AMOUNT,
    DONATION_PAYMENT,
    DONATION_CONFIRM,
    DONATION_SUCCESS,
    CAMPAIGN_CREATION,
    MY_ACTIVITY,
    PROFILE,
    NOTIFICATIONS
}

data class FilterCriteria(
    val query: String,
    val category: CampaignCategory,
    val region: String,
    val onlyVerified: Boolean
)

class HibretViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HibretRepository
    val userProfile = MutableStateFlow(UserProfile())

    // Language state
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Screen navigation stack
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<AppScreen>()

    // Selected campaign for detail / donation
    private val _selectedCampaignId = MutableStateFlow<String?>("camp_001")
    val selectedCampaignId: StateFlow<String?> = _selectedCampaignId.asStateFlow()

    // Campaigns from repository
    val approvedCampaigns: StateFlow<List<Campaign>>
    val adminCampaigns: StateFlow<List<Campaign>>
    val allDonations: StateFlow<List<Donation>>
    val totalDonatedAmount: StateFlow<Double?>
    val activeDraft: StateFlow<DraftCampaign?>
    val allNotifications: StateFlow<List<NotificationItem>>
    val unreadNotificationsCount: StateFlow<Int>

    // Explore / Search filters
    val searchQuery = MutableStateFlow("")
    val filterCategory = MutableStateFlow<CampaignCategory>(CampaignCategory.ALL)
    val filterRegion = MutableStateFlow<String>("All Regions")
    val filterOnlyVerified = MutableStateFlow(false)
    val sortBy = MutableStateFlow("popular") // "popular", "newest", "progress"

    // Filtered campaigns stream
    val filteredCampaigns: StateFlow<List<Campaign>>

    // Donation Flow State
    val donationStep = MutableStateFlow(1) // 1: Amount, 2: Details & Anonymous, 3: Method, 4: Review fees, 5: Result
    val donationAmount = MutableStateFlow(500.0)
    val customAmountInput = MutableStateFlow("")
    val donorName = MutableStateFlow("Abebe Bikila")
    val donorPhone = MutableStateFlow("+251 91 123 4567")
    val donorEmail = MutableStateFlow("abebe.b@hibret.et")
    val isAnonymousDonation = MutableStateFlow(false)
    val selectedPaymentMethod = MutableStateFlow("Telebirr")
    val platformTipPercent = MutableStateFlow(0) // 0%, 5%, 10%
    val latestDonationReceipt = MutableStateFlow<Donation?>(null)
    val isDonating = MutableStateFlow(false)

    // Server-side Payment Engine State & Sandbox Simulation
    val activePaymentInitResult = MutableStateFlow<com.example.data.payment.PaymentInitResult?>(null)
    val activeVerificationResult = MutableStateFlow<com.example.data.payment.ServerVerificationResult?>(null)
    val selectedTestScenario = MutableStateFlow(com.example.data.payment.ServerPaymentEngine.SandboxTestScenario.SUCCESS)
    val paymentErrorMessage = MutableStateFlow<String?>(null)
    val paymentIdempotencyKey = MutableStateFlow(java.util.UUID.randomUUID().toString())

    // Campaign Creation Wizard State (6 steps)
    val wizardStep = MutableStateFlow(1)
    val wizardCategory = MutableStateFlow(CampaignCategory.MEDICAL)
    val wizardTitle = MutableStateFlow("")
    val wizardShortDesc = MutableStateFlow("")
    val wizardRegion = MutableStateFlow("Addis Ababa")
    val wizardCity = MutableStateFlow("Addis Ababa")
    val wizardBeneficiaryName = MutableStateFlow("")
    val wizardBeneficiaryRelation = MutableStateFlow("Myself")
    val wizardStory = MutableStateFlow("")
    val wizardTargetAmount = MutableStateFlow("150000")
    val wizardDurationDays = MutableStateFlow("30")
    val wizardBudgetExplanation = MutableStateFlow("")
    val wizardOrganizerName = MutableStateFlow("Abebe Bikila")
    val wizardOrganizerPhone = MutableStateFlow("+251 91 123 4567")
    val wizardPayoutMethod = MutableStateFlow("Telebirr")
    val wizardPayoutAccount = MutableStateFlow("0911234567")
    val wizardTermsAccepted = MutableStateFlow(false)
    val wizardSaveMessage = MutableStateFlow<String?>(null)

    // Media & Document Attachments
    val selectedCoverDrawable = MutableStateFlow("img_medical_aid")
    val attachedDocuments = MutableStateFlow<List<String>>(
        listOf("National_ID_Scan.pdf", "Hospital_Clinical_Letter.pdf")
    )

    // Dynamic Itemized Budget Builder
    val dynamicBudgetItems = MutableStateFlow<List<BudgetItem>>(
        listOf(
            BudgetItem("Primary Medical Surgery / Equipment", 100000.0),
            BudgetItem("Post-Care Medications & Rehabilitation", 50000.0)
        )
    )

    // Editing Existing Campaign State
    val editingCampaignId = MutableStateFlow<String?>(null)

    fun addBudgetItem(title: String, amount: Double) {
        val current = dynamicBudgetItems.value.toMutableList()
        current.add(BudgetItem(title, amount))
        dynamicBudgetItems.value = current
        recalculateTargetFromBudget()
    }

    fun removeBudgetItem(index: Int) {
        val current = dynamicBudgetItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            dynamicBudgetItems.value = current
            recalculateTargetFromBudget()
        }
    }

    private fun recalculateTargetFromBudget() {
        val sum = dynamicBudgetItems.value.sumOf { it.amount }
        if (sum > 0) {
            wizardTargetAmount.value = sum.toLong().toString()
        }
    }

    fun addAttachedDocument(name: String) {
        val current = attachedDocuments.value.toMutableList()
        current.add(name)
        attachedDocuments.value = current
    }

    // Selected receipt for dialog view
    val receiptForDialog = MutableStateFlow<Donation?>(null)

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = HibretRepository(database)

        approvedCampaigns = repository.allApprovedCampaigns.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        adminCampaigns = repository.allCampaignsForAdmin.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allDonations = repository.allDonations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        totalDonatedAmount = repository.totalDonatedAmount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0.0
        )

        activeDraft = repository.activeDraft.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        allNotifications = repository.allNotifications.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        unreadNotificationsCount = repository.unreadNotificationsCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        val filtersFlow = combine(
            searchQuery,
            filterCategory,
            filterRegion,
            filterOnlyVerified
        ) { query, category, region, onlyVerified ->
            FilterCriteria(query, category, region, onlyVerified)
        }

        filteredCampaigns = combine(
            approvedCampaigns,
            filtersFlow,
            sortBy
        ) { campaigns, filters, sort ->
            campaigns.filter { campaign ->
                val matchesQuery = filters.query.isBlank() ||
                        campaign.title.contains(filters.query, ignoreCase = true) ||
                        campaign.titleAm.contains(filters.query, ignoreCase = true) ||
                        campaign.titleOm.contains(filters.query, ignoreCase = true) ||
                        campaign.organizerName.contains(filters.query, ignoreCase = true) ||
                        campaign.city.contains(filters.query, ignoreCase = true) ||
                        campaign.region.contains(filters.query, ignoreCase = true)

                val matchesCategory = filters.category == CampaignCategory.ALL ||
                        campaign.categoryId.equals(filters.category.id, ignoreCase = true)

                val matchesRegion = filters.region == "All Regions" ||
                        campaign.region.contains(filters.region, ignoreCase = true)

                val matchesVerified = !filters.onlyVerified || campaign.isVerified

                matchesQuery && matchesCategory && matchesRegion && matchesVerified
            }.let { filtered ->
                when (sort) {
                    "progress" -> filtered.sortedByDescending { it.progressPercent }
                    "newest" -> filtered.sortedByDescending { it.createdAt }
                    else -> filtered.sortedByDescending { it.donorsCount }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenBackStack.isNotEmpty()) {
            val previous = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = previous
            true
        } else {
            if (_currentScreen.value != AppScreen.HOME) {
                _currentScreen.value = AppScreen.HOME
                true
            } else {
                false
            }
        }
    }

    fun selectCampaign(id: String) {
        _selectedCampaignId.value = id
        navigateTo(AppScreen.CAMPAIGN_DETAIL)
    }

    // Authentication Form State
    val loginPhoneOrEmail = MutableStateFlow("+251911234567")
    val loginPassword = MutableStateFlow("Password123!")
    val isPhoneMode = MutableStateFlow(true)
    val regFullName = MutableStateFlow("")
    val regPhone = MutableStateFlow("+251")
    val regEmail = MutableStateFlow("")
    val regLocation = MutableStateFlow("Addis Ababa")
    val regPassword = MutableStateFlow("")
    val regTermsAgreed = MutableStateFlow(false)

    fun startDonationFlow(campaignId: String) {
        _selectedCampaignId.value = campaignId
        donationStep.value = 1
        donationAmount.value = 500.0
        customAmountInput.value = ""
        platformTipPercent.value = 0
        latestDonationReceipt.value = null
        paymentErrorMessage.value = null
        activeVerificationResult.value = null
        activePaymentInitResult.value = null
        paymentIdempotencyKey.value = java.util.UUID.randomUUID().toString()
        navigateTo(AppScreen.DONATION_AMOUNT)
    }

    fun proceedToPayment() {
        donationStep.value = 3
        navigateTo(AppScreen.DONATION_PAYMENT)
    }

    fun proceedToConfirm() {
        donationStep.value = 4
        navigateTo(AppScreen.DONATION_CONFIRM)
    }

    /**
     * Executes payment through the Server Payment Engine.
     * Enforces server-side verification, cryptographic signature check, and reconciliation.
     */
    fun completeDonation(campaign: Campaign) {
        viewModelScope.launch {
            isDonating.value = true
            paymentErrorMessage.value = null

            val amt = donationAmount.value
            val tip = amt * (platformTipPercent.value / 100.0)
            val fee = 0.0 // Hibret 0% platform fee
            val total = amt + tip + fee

            try {
                // Step 1: Initialize payment intent with idempotency key
                val initRequest = com.example.data.payment.PaymentInitRequest(
                    campaignId = campaign.id,
                    campaignTitle = campaign.title,
                    donorName = if (isAnonymousDonation.value) "Anonymous Donor" else donorName.value,
                    donorPhone = donorPhone.value,
                    donorEmail = donorEmail.value,
                    isAnonymous = isAnonymousDonation.value,
                    amount = amt,
                    tipAmount = tip,
                    feeAmount = fee,
                    totalAmount = total,
                    providerId = selectedPaymentMethod.value.lowercase().replace(" ", "_"),
                    idempotencyKey = paymentIdempotencyKey.value
                )

                val initResult = repository.serverPaymentEngine.initializePayment(initRequest)
                activePaymentInitResult.value = initResult

                // Step 2: In Sandbox/Demo mode, simulate bank/telco callback using selected scenario
                val (verificationResult, reconciledDonation) = repository.serverPaymentEngine.simulateSandboxExecution(
                    transactionRef = initResult.transactionRef,
                    scenario = selectedTestScenario.value
                )

                activeVerificationResult.value = verificationResult

                if (verificationResult.isSuccess && reconciledDonation != null) {
                    latestDonationReceipt.value = reconciledDonation
                    isDonating.value = false
                    donationStep.value = 5
                    navigateTo(AppScreen.DONATION_SUCCESS)
                } else {
                    isDonating.value = false
                    val errorMsg = verificationResult.errorReason?.userMessage
                        ?: verificationResult.verificationNotes
                    paymentErrorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                isDonating.value = false
                paymentErrorMessage.value = e.localizedMessage ?: "Transaction verification error occurred."
            }
        }
    }

    fun saveDraftProgress() {
        viewModelScope.launch {
            val draft = DraftCampaign(
                step = wizardStep.value,
                categoryId = wizardCategory.value.id,
                title = wizardTitle.value,
                shortDescription = wizardShortDesc.value,
                region = wizardRegion.value,
                city = wizardCity.value,
                beneficiaryName = wizardBeneficiaryName.value,
                beneficiaryRelation = wizardBeneficiaryRelation.value,
                story = wizardStory.value,
                targetAmount = wizardTargetAmount.value,
                durationDays = wizardDurationDays.value,
                budgetExplanation = wizardBudgetExplanation.value,
                organizerName = wizardOrganizerName.value,
                organizerPhone = wizardOrganizerPhone.value,
                payoutMethod = wizardPayoutMethod.value,
                payoutAccount = wizardPayoutAccount.value,
                termsAccepted = wizardTermsAccepted.value,
                lastUpdated = System.currentTimeMillis()
            )
            repository.saveDraft(draft)
            wizardSaveMessage.value = "Draft saved successfully"
        }
    }

    fun loadDraft(draft: DraftCampaign) {
        wizardStep.value = draft.step
        wizardCategory.value = CampaignCategory.fromId(draft.categoryId)
        wizardTitle.value = draft.title
        wizardShortDesc.value = draft.shortDescription
        wizardRegion.value = draft.region
        wizardCity.value = draft.city
        wizardBeneficiaryName.value = draft.beneficiaryName
        wizardBeneficiaryRelation.value = draft.beneficiaryRelation
        wizardStory.value = draft.story
        wizardTargetAmount.value = draft.targetAmount
        wizardDurationDays.value = draft.durationDays
        wizardBudgetExplanation.value = draft.budgetExplanation
        wizardOrganizerName.value = draft.organizerName
        wizardOrganizerPhone.value = draft.organizerPhone
        wizardPayoutMethod.value = draft.payoutMethod
        wizardPayoutAccount.value = draft.payoutAccount
        wizardTermsAccepted.value = draft.termsAccepted
        navigateTo(AppScreen.CAMPAIGN_CREATION)
    }

    fun loadCampaignForEditing(campaign: Campaign) {
        editingCampaignId.value = campaign.id
        wizardTitle.value = campaign.title
        wizardShortDesc.value = campaign.shortDescription
        wizardCategory.value = campaign.category
        wizardRegion.value = campaign.region
        wizardCity.value = campaign.city
        wizardBeneficiaryName.value = campaign.beneficiaryName
        wizardBeneficiaryRelation.value = campaign.beneficiaryRelation
        wizardStory.value = campaign.story
        wizardTargetAmount.value = campaign.targetAmount.toLong().toString()
        selectedCoverDrawable.value = campaign.coverDrawableName
        val items = campaign.getBudgetItems()
        if (items.isNotEmpty()) {
            dynamicBudgetItems.value = items
        }
        wizardStep.value = 1
        navigateTo(AppScreen.CAMPAIGN_CREATION)
    }

    fun submitCampaignWizard(onSuccess: (Campaign) -> Unit) {
        viewModelScope.launch {
            val target = wizardTargetAmount.value.toDoubleOrNull() ?: 50000.0
            val budgetRaw = dynamicBudgetItems.value.joinToString("|") { "${it.title}:${it.amount.toLong()}" }

            val editingId = editingCampaignId.value
            if (editingId != null) {
                // Update existing campaign
                val existing = approvedCampaigns.value.find { it.id == editingId }
                    ?: adminCampaigns.value.find { it.id == editingId }

                if (existing != null) {
                    val updated = existing.copy(
                        title = wizardTitle.value,
                        shortDescription = wizardShortDesc.value,
                        story = wizardStory.value,
                        categoryId = wizardCategory.value.id,
                        region = wizardRegion.value,
                        city = wizardCity.value,
                        targetAmount = target,
                        beneficiaryName = wizardBeneficiaryName.value,
                        beneficiaryRelation = wizardBeneficiaryRelation.value,
                        budgetBreakdownRaw = budgetRaw,
                        coverDrawableName = selectedCoverDrawable.value
                    )
                    repository.updateCampaign(updated)
                    editingCampaignId.value = null
                    wizardStep.value = 1
                    onSuccess(updated)
                    return@launch
                }
            }

            val newCampaign = repository.submitNewCampaign(
                title = wizardTitle.value.ifBlank { "Untitled Campaign" },
                shortDesc = wizardShortDesc.value.ifBlank { "Support community cause." },
                story = wizardStory.value.ifBlank { "Campaign story and background." },
                categoryId = wizardCategory.value.id,
                region = wizardRegion.value,
                city = wizardCity.value,
                targetAmount = target,
                organizerName = wizardOrganizerName.value,
                organizerPhone = wizardOrganizerPhone.value,
                beneficiaryName = wizardBeneficiaryName.value.ifBlank { "Beneficiary" },
                beneficiaryRelation = wizardBeneficiaryRelation.value,
                budgetExplanation = budgetRaw,
                coverDrawableName = selectedCoverDrawable.value
            )
            // Reset wizard
            editingCampaignId.value = null
            wizardStep.value = 1
            wizardTitle.value = ""
            wizardShortDesc.value = ""
            wizardStory.value = ""
            wizardBudgetExplanation.value = ""
            onSuccess(newCampaign)
        }
    }

    fun publishCampaignUpdate(campaignId: String, title: String, body: String) {
        viewModelScope.launch {
            repository.publishCampaignUpdate(
                campaignId = campaignId,
                title = title,
                body = body,
                author = userProfile.value.fullName
            )
        }
    }

    fun getDonationsForCampaign(campaignId: String) = repository.getDonationsForCampaign(campaignId)

    fun adminApproveCampaign(id: String) {
        viewModelScope.launch {
            repository.updateCampaignStatus(id, "APPROVED", "Approved by Hibret Admin.")
        }
    }

    fun adminRejectCampaign(id: String, reason: String) {
        viewModelScope.launch {
            repository.updateCampaignStatus(id, "REJECTED", reason)
        }
    }

    fun adminRequestInfo(id: String) {
        viewModelScope.launch {
            repository.updateCampaignStatus(id, "MORE_INFO_REQUIRED", "Please provide additional hospital or kebele documentation.")
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun showReceiptDialog(donation: Donation) {
        receiptForDialog.value = donation
    }

    fun dismissReceiptDialog() {
        receiptForDialog.value = null
    }
}
