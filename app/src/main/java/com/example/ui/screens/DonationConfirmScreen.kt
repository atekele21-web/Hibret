package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.payment.ServerPaymentEngine
import com.example.model.HibretStrings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldSoft
import com.example.ui.theme.BackgroundWarm
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorSoft
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HibretViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DonationConfirmScreen(
    campaignId: String,
    viewModel: HibretViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val allCampaigns by viewModel.approvedCampaigns.collectAsState()
    val adminCampaigns by viewModel.adminCampaigns.collectAsState()
    val campaign = allCampaigns.find { it.id == campaignId }
        ?: adminCampaigns.find { it.id == campaignId }

    val selectedAmount by viewModel.donationAmount.collectAsState()
    val donorName by viewModel.donorName.collectAsState()
    val isAnonymous by viewModel.isAnonymousDonation.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val platformTipPercent by viewModel.platformTipPercent.collectAsState()
    val isDonating by viewModel.isDonating.collectAsState()
    val paymentError by viewModel.paymentErrorMessage.collectAsState()
    val testScenario by viewModel.selectedTestScenario.collectAsState()
    val idempotencyKey by viewModel.paymentIdempotencyKey.collectAsState()
    var showScenarioDialog by remember { mutableStateOf(false) }

    val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)
    val tipAmount = selectedAmount * (platformTipPercent / 100.0)
    val totalPayable = selectedAmount + tipAmount
    val scrollState = rememberScrollState()

    if (campaign == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Campaign not found")
        }
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWarm)
            .statusBarsPadding(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Review & Confirm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                        Text(
                            text = "Step 3 of 3 • Final Authorization",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.completeDonation(campaign) },
                        enabled = !isDonating,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_donation_btn")
                    ) {
                        if (isDonating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Authorize ${currencyFormatter.format(totalPayable.toLong())} ETB",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Clearly Labelled Sandbox / Demo Mode Banner
            Surface(
                color = AccentGoldSoft,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DEMO / SANDBOX ENVIRONMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldDark
                        )
                        Text(
                            text = "Live payment gateway keys are not provisioned. Transactions execute in a secure simulation container with cryptographic verification.",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test Scenario Selector (Success, Failure, Timeout, Duplicate)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Test Scenario:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = testScenario.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (testScenario == ServerPaymentEngine.SandboxTestScenario.SUCCESS) EmeraldPrimary else ErrorRed
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Simulate provider response: ${testScenario.description}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick scenario selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ServerPaymentEngine.SandboxTestScenario.values().take(3).forEach { scn ->
                            val isSel = testScenario == scn
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) EmeraldPrimary else BackgroundWarm,
                                border = if (!isSel) androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.selectedTestScenario.value = scn
                                        viewModel.paymentErrorMessage.value = null
                                    }
                            ) {
                                Text(
                                    text = scn.name.take(7),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ServerPaymentEngine.SandboxTestScenario.values().drop(3).forEach { scn ->
                            val isSel = testScenario == scn
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) EmeraldPrimary else BackgroundWarm,
                                border = if (!isSel) androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.selectedTestScenario.value = scn
                                        viewModel.paymentErrorMessage.value = null
                                    }
                            ) {
                                Text(
                                    text = scn.name.take(8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Payment Error Banner (if any failure occurred)
            paymentError?.let { err ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = ErrorSoft,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Payment Authorization Failed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tip: Change scenario above to 'SUCCESS' and press Authorize again to retry.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Transaction Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Please review your payment details before authorizing on your mobile device.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SummaryRow("Campaign Cause", campaign.getDisplayTitle(language), isHighlighted = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                    SummaryRow("Beneficiary", campaign.beneficiaryName)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                    SummaryRow("Donation Amount", "${currencyFormatter.format(selectedAmount.toLong())} ETB", isBold = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                    SummaryRow("Payment Provider", selectedPaymentMethod)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                    SummaryRow("Donor Name", if (isAnonymous) "Anonymous Donor (Hidden)" else donorName)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                    SummaryRow("Platform Fee (Hibret)", "0 ETB (100% to Cause)")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                    SummaryRow("Idempotency Key", idempotencyKey.take(13) + "...", isBold = false)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                    SummaryRow("Server Verification", "HMAC-SHA256 Signed", isBold = true, isHighlighted = true)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optional Support Tip Segment
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldSoft)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Optional Hibret Support Tip",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hibret charges 0% platform fee to patients. Your voluntary tip supports hosting and verification.",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 5, 10).forEach { pct ->
                            val isSelected = platformTipPercent == pct
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.platformTipPercent.value = pct }
                            ) {
                                Text(
                                    text = if (pct == 0) "0% (None)" else "$pct% (${currencyFormatter.format((selectedAmount * pct / 100).toLong())} ETB)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Payable Highlight Card
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Payable Amount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Including 0% platform fee",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "${currencyFormatter.format(totalPayable.toLong())} ETB",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Encrypted connection • Real-time SMS and digital receipt provided",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold || isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) EmeraldDark else if (isBold) EmeraldPrimary else TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(0.62f)
        )
    }
}
