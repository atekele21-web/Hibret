package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.Campaign
import com.example.model.HibretStrings
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BackgroundWarm
import com.example.ui.theme.CbePurple
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TelebirrBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HibretViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DonationCheckoutScreen(
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

    val step by viewModel.donationStep.collectAsState()
    val selectedAmount by viewModel.donationAmount.collectAsState()
    val customAmountInput by viewModel.customAmountInput.collectAsState()
    val donorName by viewModel.donorName.collectAsState()
    val donorPhone by viewModel.donorPhone.collectAsState()
    val donorEmail by viewModel.donorEmail.collectAsState()
    val isAnonymous by viewModel.isAnonymousDonation.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val platformTipPercent by viewModel.platformTipPercent.collectAsState()
    val latestReceipt by viewModel.latestDonationReceipt.collectAsState()
    val isDonating by viewModel.isDonating.collectAsState()

    val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)

    if (campaign == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Campaign not found")
        }
        return
    }

    val imageRes = when (campaign.coverDrawableName) {
        "img_medical_aid" -> R.drawable.img_medical_aid
        "img_education_aid" -> R.drawable.img_education_aid
        "img_community_aid" -> R.drawable.img_community_aid
        else -> R.drawable.img_community_aid
    }

    val presetAmounts = listOf(100.0, 250.0, 500.0, 1000.0, 2000.0, 5000.0)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWarm)
            .statusBarsPadding(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step < 5) {
                        IconButton(
                            onClick = {
                                if (step > 1) {
                                    viewModel.donationStep.value = step - 1
                                } else {
                                    onBack()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (step == 5) "Donation Confirmed" else "Make a Contribution",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                        if (step < 5) {
                            Text(
                                text = "Step $step of 4",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (step < 5) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                when (step) {
                                    1 -> viewModel.donationStep.value = 2
                                    2 -> viewModel.donationStep.value = 3
                                    3 -> viewModel.donationStep.value = 4
                                    4 -> viewModel.completeDonation(campaign)
                                }
                            },
                            enabled = when (step) {
                                1 -> selectedAmount >= 10.0
                                2 -> donorPhone.isNotBlank()
                                else -> !isDonating
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("checkout_next_btn")
                        ) {
                            if (isDonating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text(
                                    text = when (step) {
                                        1 -> "${HibretStrings.next.get(language)} • ${currencyFormatter.format(selectedAmount.toLong())} ETB"
                                        2 -> HibretStrings.next.get(language)
                                        3 -> HibretStrings.next.get(language)
                                        4 -> "Confirm & Pay ${currencyFormatter.format((selectedAmount + selectedAmount * (platformTipPercent / 100.0)).toLong())} ETB"
                                        else -> HibretStrings.next.get(language)
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Mini Campaign Header Summary
            if (step < 5) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = campaign.title,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = campaign.getDisplayTitle(language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "Beneficiary: ${campaign.beneficiaryName}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            when (step) {
                // ==========================================
                // STEP 1: CHOOSE YOUR CONTRIBUTION
                // ==========================================
                1 -> {
                    Text(
                        text = HibretStrings.chooseContribution.get(language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select an amount to make a direct impact. 100% goes directly to the verified campaign.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Cards in 2 columns
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (i in presetAmounts.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val amt1 = presetAmounts[i]
                                AmountCard(
                                    amount = amt1,
                                    isSelected = selectedAmount == amt1 && customAmountInput.isEmpty(),
                                    onClick = {
                                        viewModel.donationAmount.value = amt1
                                        viewModel.customAmountInput.value = ""
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                if (i + 1 < presetAmounts.size) {
                                    val amt2 = presetAmounts[i + 1]
                                    AmountCard(
                                        amount = amt2,
                                        isSelected = selectedAmount == amt2 && customAmountInput.isEmpty(),
                                        onClick = {
                                            viewModel.donationAmount.value = amt2
                                            viewModel.customAmountInput.value = ""
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Amount Input
                    Text(
                        text = HibretStrings.enterCustomAmount.get(language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customAmountInput,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            viewModel.customAmountInput.value = clean
                            val num = clean.toDoubleOrNull()
                            if (num != null && num > 0) {
                                viewModel.donationAmount.value = num
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g. 3,500", fontSize = 13.sp) },
                        trailingIcon = { Text("ETB", fontWeight = FontWeight.Bold, color = EmeraldDark, modifier = Modifier.padding(end = 12.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_amount_input")
                    )
                }

                // ==========================================
                // STEP 2: DONOR DETAILS
                // ==========================================
                2 -> {
                    Text(
                        text = HibretStrings.donorDetails.get(language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "We use your details to generate your official Hibret donation receipt and SMS transaction confirmation.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Name
                    Text(text = HibretStrings.fullName.get(language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = donorName,
                        onValueChange = { viewModel.donorName.value = it },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donor_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Number (+251)
                    Text(text = HibretStrings.phoneNumber.get(language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = donorPhone,
                        onValueChange = { viewModel.donorPhone.value = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donor_phone_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email (optional)
                    Text(text = HibretStrings.emailOptional.get(language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = donorEmail,
                        onValueChange = { viewModel.donorEmail.value = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Anonymous Toggle Card
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = HibretStrings.donateAnonymously.get(language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = HibretStrings.anonymousNote.get(language),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = isAnonymous,
                                onCheckedChange = { viewModel.isAnonymousDonation.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldPrimary),
                                modifier = Modifier.testTag("anonymous_switch")
                            )
                        }
                    }
                }

                // ==========================================
                // STEP 3: PAYMENT METHOD
                // ==========================================
                3 -> {
                    Text(
                        text = HibretStrings.paymentMethod.get(language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose a licensed Ethiopian mobile wallet or bank for secure payment processing.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Options
                    PaymentMethodCard(
                        title = HibretStrings.telebirrTitle.get(language),
                        subtitle = HibretStrings.telebirrDesc.get(language),
                        isSelected = selectedPaymentMethod == "Telebirr",
                        badge = "POPULAR",
                        badgeColor = TelebirrBlue,
                        icon = Icons.Default.PhoneAndroid,
                        onClick = { viewModel.selectedPaymentMethod.value = "Telebirr" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PaymentMethodCard(
                        title = HibretStrings.cbeBirrTitle.get(language),
                        subtitle = HibretStrings.cbeBirrDesc.get(language),
                        isSelected = selectedPaymentMethod == "CBE Birr",
                        icon = Icons.Default.AccountBalance,
                        onClick = { viewModel.selectedPaymentMethod.value = "CBE Birr" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PaymentMethodCard(
                        title = HibretStrings.awashBirrTitle.get(language),
                        subtitle = HibretStrings.awashBirrDesc.get(language),
                        isSelected = selectedPaymentMethod == "Awash Bank",
                        icon = Icons.Default.AccountBalance,
                        onClick = { viewModel.selectedPaymentMethod.value = "Awash Bank" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PaymentMethodCard(
                        title = HibretStrings.internationalCardTitle.get(language),
                        subtitle = HibretStrings.internationalCardDesc.get(language),
                        isSelected = selectedPaymentMethod == "International Card",
                        icon = Icons.Default.CreditCard,
                        onClick = { viewModel.selectedPaymentMethod.value = "International Card" }
                    )
                }

                // ==========================================
                // STEP 4: REVIEW DONATION
                // ==========================================
                4 -> {
                    Text(
                        text = HibretStrings.reviewDonation.get(language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Review your contribution breakdown before authorizing payment.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Review Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ReviewRow("Donation Amount", "${currencyFormatter.format(selectedAmount.toLong())} ETB", isBold = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceBorder)

                            ReviewRow("Recipient Cause", campaign.getDisplayTitle(language))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceBorder)

                            ReviewRow("Payment Method", selectedPaymentMethod)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceBorder)

                            ReviewRow("Donor", if (isAnonymous) "Anonymous Donor" else donorName)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceBorder)

                            ReviewRow("Platform Fee (Hibret)", "0 ETB (Free)")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Optional Tip Segment
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldSoft)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = HibretStrings.platformContribution.get(language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = HibretStrings.platformTipDesc.get(language),
                                fontSize = 11.sp,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0, 5, 10).forEach { pct ->
                                    val isSelected = platformTipPercent == pct
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.platformTipPercent.value = pct }
                                    ) {
                                        Text(
                                            text = if (pct == 0) "0% (None)" else "$pct%",
                                            fontSize = 12.sp,
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

                    // Total Payable Highlight
                    val tipAmt = selectedAmount * (platformTipPercent / 100.0)
                    val totalPayable = selectedAmount + tipAmt
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = HibretStrings.totalPayable.get(language),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${currencyFormatter.format(totalPayable.toLong())} ETB",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldDark
                            )
                        }
                    }
                }

                // ==========================================
                // STEP 5: PAYMENT RESULT (SUCCESS)
                // ==========================================
                5 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(EmeraldSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = HibretStrings.successThankYou.get(language),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = HibretStrings.donationSuccessDesc.get(language),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Transaction Details Card
                        latestReceipt?.let { donation ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Amount Contributed", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            text = "${currencyFormatter.format(donation.amount.toLong())} ETB",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Transaction Ref", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            text = donation.transactionReference,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Payment Method", fontSize = 12.sp, color = TextSecondary)
                                        Text(text = donation.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Download / View Receipt Button
                            Button(
                                onClick = { viewModel.showReceiptDialog(donation) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("view_receipt_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = HibretStrings.viewReceipt.get(language), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.navigateTo(AppScreen.HOME)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(text = HibretStrings.returnToCampaign.get(language), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountCard(
    amount: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) EmeraldSoft else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) EmeraldPrimary else SurfaceBorder
        ),
        modifier = modifier
            .height(68.dp)
            .clickable(onClick = onClick)
            .testTag("preset_amount_${amount.toInt()}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currencyFormatter.format(amount.toLong())} ETB",
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isSelected) EmeraldDark else TextPrimary
            )
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    badge: String? = null,
    badgeColor: Color = EmeraldPrimary
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) EmeraldSoft else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) EmeraldPrimary else SurfaceBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) EmeraldPrimary else Color(0xFFF1F3F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) EmeraldDark else TextPrimary
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = badgeColor) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (isBold) EmeraldDark else TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(0.65f)
        )
    }
}
