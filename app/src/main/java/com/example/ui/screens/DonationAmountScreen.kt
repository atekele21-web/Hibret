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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.HibretStrings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BackgroundWarm
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HibretViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DonationAmountScreen(
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
    val customAmountInput by viewModel.customAmountInput.collectAsState()

    val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)
    val presetAmounts = listOf(100.0, 250.0, 500.0, 1000.0, 2000.0, 5000.0)

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
                            text = "Choose Contribution",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                        Text(
                            text = "Step 1 of 3 • Select Amount",
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
                        onClick = { viewModel.proceedToPayment() },
                        enabled = selectedAmount >= 10.0,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("amount_screen_next_btn")
                    ) {
                        Text(
                            text = "Next • Pay ${currencyFormatter.format(selectedAmount.toLong())} ETB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Campaign Thumbnail Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = campaign.title,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = campaign.getDisplayTitle(language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Target: ${currencyFormatter.format(campaign.targetAmount.toLong())} ETB • ${campaign.donorsCount} supporters",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Heading
            Text(
                text = HibretStrings.chooseContribution.get(language),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "100% of your gift goes directly to the verified recipient. Select an Ethiopian Birr denomination below:",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 6 Selectable Amount Cards (inspired by Telebirr package interaction pattern)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in presetAmounts.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val amt1 = presetAmounts[i]
                        TelebirrStyleAmountCard(
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
                            TelebirrStyleAmountCard(
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

            Spacer(modifier = Modifier.height(18.dp))

            // Custom Amount Field
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
                placeholder = { Text("e.g. 750 or 3,000", fontSize = 13.sp) },
                leadingIcon = {
                    Text(
                        text = "ETB",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 14.dp, end = 6.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_amount_text_field")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Impact Insight Card
            Surface(
                color = EmeraldSoft,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when {
                            selectedAmount >= 2000 -> "Your gift of ${currencyFormatter.format(selectedAmount.toLong())} ETB significantly covers essential surgical supplies or student tablet libraries."
                            selectedAmount >= 500 -> "Your gift of ${currencyFormatter.format(selectedAmount.toLong())} ETB covers prescription medications and clinic bed admission."
                            else -> "Every Birr counts. Small contributions unite to save lives and build futures."
                        },
                        fontSize = 11.sp,
                        color = EmeraldDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TelebirrStyleAmountCard(
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
            .height(72.dp)
            .clickable(onClick = onClick)
            .testTag("amount_card_${amount.toInt()}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${currencyFormatter.format(amount.toLong())} ETB",
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isSelected) EmeraldDark else TextPrimary
                )
                Text(
                    text = "One-time contribution",
                    fontSize = 10.sp,
                    color = if (isSelected) EmeraldPrimary else TextSecondary
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
