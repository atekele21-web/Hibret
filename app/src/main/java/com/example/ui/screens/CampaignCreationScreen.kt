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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.model.BudgetItem
import com.example.model.CampaignCategory
import com.example.model.HibretStrings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldSoft
import com.example.ui.theme.BackgroundWarm
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HibretViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CampaignCreationScreen(
    viewModel: HibretViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val step by viewModel.wizardStep.collectAsState()
    val category by viewModel.wizardCategory.collectAsState()
    val title by viewModel.wizardTitle.collectAsState()
    val shortDesc by viewModel.wizardShortDesc.collectAsState()
    val region by viewModel.wizardRegion.collectAsState()
    val city by viewModel.wizardCity.collectAsState()
    val beneficiaryName by viewModel.wizardBeneficiaryName.collectAsState()
    val beneficiaryRelation by viewModel.wizardBeneficiaryRelation.collectAsState()
    val story by viewModel.wizardStory.collectAsState()
    val targetAmount by viewModel.wizardTargetAmount.collectAsState()
    val durationDays by viewModel.wizardDurationDays.collectAsState()
    val organizerName by viewModel.wizardOrganizerName.collectAsState()
    val organizerPhone by viewModel.wizardOrganizerPhone.collectAsState()
    val payoutMethod by viewModel.wizardPayoutMethod.collectAsState()
    val payoutAccount by viewModel.wizardPayoutAccount.collectAsState()
    val termsAccepted by viewModel.wizardTermsAccepted.collectAsState()
    val saveMsg by viewModel.wizardSaveMessage.collectAsState()

    val selectedCover by viewModel.selectedCoverDrawable.collectAsState()
    val attachedDocs by viewModel.attachedDocuments.collectAsState()
    val budgetItems by viewModel.dynamicBudgetItems.collectAsState()
    val editingId by viewModel.editingCampaignId.collectAsState()

    var showLivePreviewDialog by remember { mutableStateOf(false) }
    var newBudgetItemTitle by remember { mutableStateOf("") }
    var newBudgetItemAmount by remember { mutableStateOf("") }
    var newDocName by remember { mutableStateOf("") }
    var showAddDocDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)

    LaunchedEffect(saveMsg) {
        saveMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.wizardSaveMessage.value = null
        }
    }

    val regionsList = listOf(
        "Addis Ababa",
        "Oromia",
        "Amhara",
        "Sidama",
        "Tigray",
        "Somali",
        "South Ethiopia",
        "Dire Dawa",
        "Afar",
        "Benishangul-Gumuz",
        "Gambella",
        "Harari"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWarm)
            .statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (step > 1) {
                                        viewModel.wizardStep.value = step - 1
                                    } else {
                                        onBack()
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (editingId != null) "Edit Campaign" else "Start a Campaign",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                                Text(
                                    text = "Step $step of 6 • ${getStepTitle(step)}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Live Preview Button
                            IconButton(
                                onClick = { showLivePreviewDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = "Preview", tint = EmeraldPrimary)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Save Draft Button
                            OutlinedButton(
                                onClick = { viewModel.saveDraftProgress() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("save_draft_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = HibretStrings.saveDraft.get(language), fontSize = 11.sp, color = EmeraldPrimary)
                            }
                        }
                    }

                    // Wizard Step Progress Bar
                    LinearProgressIndicator(
                        progress = { step / 6f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = EmeraldPrimary,
                        trackColor = EmeraldSoft
                    )
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { viewModel.wizardStep.value = step - 1 },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.45f)
                        ) {
                            Text(text = HibretStrings.back.get(language))
                        }
                    }

                    Button(
                        onClick = {
                            if (step < 6) {
                                viewModel.wizardStep.value = step + 1
                                viewModel.saveDraftProgress()
                            } else {
                                viewModel.submitCampaignWizard { newCamp ->
                                    viewModel.selectCampaign(newCamp.id)
                                }
                            }
                        },
                        enabled = when (step) {
                            1 -> true
                            2 -> title.isNotBlank() && shortDesc.isNotBlank() && beneficiaryName.isNotBlank()
                            3 -> story.length >= 30
                            4 -> true
                            5 -> targetAmount.toDoubleOrNull() != null && (targetAmount.toDoubleOrNull() ?: 0.0) >= 1000.0
                            6 -> termsAccepted
                            else -> true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("wizard_next_btn")
                    ) {
                        Text(
                            text = if (step == 6) {
                                if (editingId != null) "Save & Publish Changes" else HibretStrings.submitCampaign.get(language)
                            } else HibretStrings.next.get(language),
                            fontSize = 14.sp,
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
            when (step) {
                // ==========================================
                // STEP 1: SELECT CAUSE
                // ==========================================
                1 -> {
                    Text(
                        text = "Step 1: Select Your Cause",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Choose the category that best represents the purpose of your fundraising.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val categories = listOf(
                        CampaignCategory.MEDICAL to ("Medical & Urgent Surgeries" to Icons.Default.LocalHospital),
                        CampaignCategory.EDUCATION to ("Schools, Students & STEM" to Icons.Default.School),
                        CampaignCategory.EMERGENCY to ("Drought & Crisis Relief" to Icons.Default.Warning),
                        CampaignCategory.COMMUNITY to ("Clean Water & Village Projects" to Icons.Default.VolunteerActivism),
                        CampaignCategory.FAMILY to ("Family & Child Support" to Icons.Default.People),
                        CampaignCategory.ENVIRONMENT to ("Reforestation & Climate" to Icons.Default.Security),
                        CampaignCategory.OTHER to ("Other Community Causes" to Icons.Default.Description)
                    )

                    categories.forEach { (cat, info) ->
                        val (desc, icon) = info
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) EmeraldSoft else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) EmeraldPrimary else SurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.wizardCategory.value = cat
                                    // auto-select fitting cover drawable
                                    viewModel.selectedCoverDrawable.value = when (cat) {
                                        CampaignCategory.MEDICAL -> "img_medical_aid"
                                        CampaignCategory.EDUCATION -> "img_education_aid"
                                        else -> "img_community_aid"
                                    }
                                }
                                .testTag("wizard_cat_${cat.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) EmeraldPrimary else Color(0xFFF0F3F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else EmeraldDark,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.localizedName.get(language),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EmeraldDark else TextPrimary
                                    )
                                    Text(text = desc, fontSize = 11.sp, color = TextSecondary)
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // STEP 2: CAMPAIGN DETAILS
                // ==========================================
                2 -> {
                    Text(
                        text = "Step 2: Campaign Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Give your campaign a clear, trustworthy title and specify the Ethiopian location and beneficiary.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Campaign Title *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.wizardTitle.value = it },
                        placeholder = { Text("e.g. Urgent Pediatric Surgery for Baby Eldana") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wizard_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Short Summary (1-2 sentences) *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = shortDesc,
                        onValueChange = { viewModel.wizardShortDesc.value = it },
                        placeholder = { Text("Describe who needs help and the immediate objective") },
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Country (Locked to Ethiopia)
                    Text(text = "Country", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = "Ethiopia (ኢትዮጵያ)",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Text("🇪🇹", fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Region Dropdown
                    var showRegionDropdown by remember { mutableStateOf(false) }
                    Text(text = "Region / City Administration *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedTextField(
                            value = region,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Text(
                                    text = "Select",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(end = 12.dp).clickable { showRegionDropdown = true }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRegionDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showRegionDropdown,
                            onDismissRequest = { showRegionDropdown = false }
                        ) {
                            regionsList.forEach { reg ->
                                DropdownMenuItem(
                                    text = { Text(reg) },
                                    onClick = {
                                        viewModel.wizardRegion.value = reg
                                        showRegionDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "City / Woreda / Hospital / Kebele *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = city,
                        onValueChange = { viewModel.wizardCity.value = it },
                        placeholder = { Text("e.g. Addis Ababa, Tikur Anbessa Hospital") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Beneficiary Name *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = beneficiaryName,
                        onValueChange = { viewModel.wizardBeneficiaryName.value = it },
                        placeholder = { Text("e.g. Eldana Tesfaye or Wotera Primary School") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Your Relationship to Beneficiary *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = beneficiaryRelation,
                        onValueChange = { viewModel.wizardBeneficiaryRelation.value = it },
                        placeholder = { Text("e.g. Mother, Father, Doctor, Committee Chair, Myself") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ==========================================
                // STEP 3: TELL YOUR STORY
                // ==========================================
                3 -> {
                    Text(
                        text = "Step 3: Tell Your Story",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Explain what happened, why support is urgent, and how donations will be directly utilized.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Suggested structure tip
                    Surface(
                        color = EmeraldSoft,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Recommended Story Structure:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldDark)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "1. Introduce the person or community needing help.", fontSize = 11.sp, color = TextPrimary)
                            Text(text = "2. Clearly describe the medical diagnosis, school need, or emergency.", fontSize = 11.sp, color = TextPrimary)
                            Text(text = "3. Explain what steps the family or community has already taken.", fontSize = 11.sp, color = TextPrimary)
                            Text(text = "4. Detail how every Birr raised will be paid directly to providers.", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = story,
                        onValueChange = { viewModel.wizardStory.value = it },
                        placeholder = { Text("Write your story in detail...") },
                        minLines = 8,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wizard_story_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (story.length < 30) "Minimum 30 characters required (${story.length}/30)" else "Sufficient story length",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (story.length < 30) ErrorRed else EmeraldPrimary
                        )
                        Text(
                            text = "${story.length} chars",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // ==========================================
                // STEP 4: ADD MEDIA & VERIFICATION DOCUMENTS
                // ==========================================
                4 -> {
                    Text(
                        text = "Step 4: Campaign Media & Proof",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Select a high-resolution campaign banner and attach supporting documents (e.g. clinical quote, Kebele letter).",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Select Cover Photograph *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val coverOptions = listOf(
                        Triple("img_medical_aid", "Medical Setting (Addis Ababa Hospital)", R.drawable.img_medical_aid),
                        Triple("img_education_aid", "Rural School Classroom (Hawassa)", R.drawable.img_education_aid),
                        Triple("img_community_aid", "Community Water Spring (Debre Birhan)", R.drawable.img_community_aid)
                    )

                    coverOptions.forEach { (drawableName, label, resId) ->
                        val isSelected = selectedCover == drawableName
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) EmeraldSoft else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) EmeraldPrimary else SurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.selectedCoverDrawable.value = drawableName }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = label,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = if (isSelected) "Selected Cover Photo" else "Tap to choose", fontSize = 10.sp, color = if (isSelected) EmeraldPrimary else TextSecondary)
                                }
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Supporting Document Attachments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Attached Documents (${attachedDocs.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        TextButton(onClick = { showAddDocDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Add Document", fontSize = 11.sp, color = EmeraldPrimary)
                        }
                    }

                    attachedDocs.forEach { doc ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = doc, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text(text = "Verified Document Scan • PDF/PNG", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // ==========================================
                // STEP 5: FUNDRAISING GOAL & ITEMIZED BUDGET BUILDER
                // ==========================================
                5 -> {
                    Text(
                        text = "Step 5: Goal & Itemized Budget",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Set your fundraising target in ETB and itemize expenses. Donors trust campaigns with transparent budgets.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Fundraising Target (ETB) *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = { viewModel.wizardTargetAmount.value = it.filter { c -> c.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("ETB", fontWeight = FontWeight.Bold, color = EmeraldDark, modifier = Modifier.padding(end = 12.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wizard_goal_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Campaign Duration (Days)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = durationDays,
                        onValueChange = { viewModel.wizardDurationDays.value = it.filter { c -> c.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("Days", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Itemized Budget Builder Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Itemized Budget Breakdown", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = "Total: ${currencyFormatter.format(budgetItems.sumOf { it.amount }.toLong())} ETB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Existing budget items
                    budgetItems.forEachIndexed { index, item ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(text = "${currencyFormatter.format(item.amount.toLong())} ETB", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { viewModel.removeBudgetItem(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add new budget item inputs
                    Surface(
                        color = Color(0xFFF9FAF8),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Add Itemized Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = newBudgetItemTitle,
                                onValueChange = { newBudgetItemTitle = it },
                                placeholder = { Text("Expense item (e.g. ICU Bed, Textbooks)", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newBudgetItemAmount,
                                    onValueChange = { newBudgetItemAmount = it.filter { c -> c.isDigit() } },
                                    placeholder = { Text("Cost in ETB", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val amt = newBudgetItemAmount.toDoubleOrNull() ?: 0.0
                                        if (newBudgetItemTitle.isNotBlank() && amt > 0) {
                                            viewModel.addBudgetItem(newBudgetItemTitle.trim(), amt)
                                            newBudgetItemTitle = ""
                                            newBudgetItemAmount = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // STEP 6: BENEFICIARY, VERIFICATION & PREVIEW
                // ==========================================
                6 -> {
                    Text(
                        text = "Step 6: Verification & Final Submission",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Confirm payout account and review all details before submitting for moderation.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campaign Live Preview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(6.dp), color = EmeraldSoft) {
                                    Text(
                                        text = category.localizedName.get(language).uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Goal: ${currencyFormatter.format(targetAmount.toLongOrNull() ?: 0)} ETB",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = title.ifBlank { "Untitled Campaign" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = shortDesc.ifBlank { "No description entered." },
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 2
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Beneficiary: $beneficiaryName ($beneficiaryRelation)", fontSize = 11.sp, color = TextPrimary)
                                Text(text = "$city, $region", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payout Account Details
                    Text(text = "Payout Mobile Money or Bank Account *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = payoutMethod,
                            onValueChange = { viewModel.wizardPayoutMethod.value = it },
                            placeholder = { Text("Telebirr / CBE") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.45f)
                        )
                        OutlinedTextField(
                            value = payoutAccount,
                            onValueChange = { viewModel.wizardPayoutAccount.value = it },
                            placeholder = { Text("Account / Phone #") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.55f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verification Checklist
                    Surface(
                        color = EmeraldSoft,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Hibret Trust Guarantee Protocol", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "• Identity is matched against National ID records.", fontSize = 11.sp, color = TextPrimary)
                            Text(text = "• Hospital letters and quotes are directly validated.", fontSize = 11.sp, color = TextPrimary)
                            Text(text = "• Payouts disburse directly to verified service providers.", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terms Acceptance Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { viewModel.wizardTermsAccepted.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary),
                            modifier = Modifier.testTag("terms_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I certify that all claims, hospital estimates, and beneficiary details are accurate under Hibret Community Rules.",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Add Document Dialog
    if (showAddDocDialog) {
        AlertDialog(
            onDismissRequest = { showAddDocDialog = false },
            title = { Text("Attach Supporting Document") },
            text = {
                Column {
                    Text("Enter the name of your Kebele ID, Hospital Bill, or Committee letter:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDocName,
                        onValueChange = { newDocName = it },
                        placeholder = { Text("e.g. Tikur_Anbessa_Clinical_Letter.pdf") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDocName.isNotBlank()) {
                            viewModel.addAttachedDocument(newDocName.trim())
                            newDocName = ""
                            showAddDocDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Attach")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Live Preview Dialog
    if (showLivePreviewDialog) {
        AlertDialog(
            onDismissRequest = { showLivePreviewDialog = false },
            title = { Text("Live Campaign Preview") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = title.ifBlank { "Campaign Title" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Target: ${currencyFormatter.format(targetAmount.toLongOrNull() ?: 0)} ETB", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Beneficiary: $beneficiaryName • Location: $city, $region", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = story.ifBlank { "Your narrative story will appear here." }, fontSize = 12.sp, lineHeight = 17.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showLivePreviewDialog = false }) {
                    Text("Close Preview")
                }
            }
        )
    }
}

private fun getStepTitle(step: Int): String = when (step) {
    1 -> "Select Cause"
    2 -> "Details"
    3 -> "Story"
    4 -> "Media & Proof"
    5 -> "Goal & Budget"
    6 -> "Review & ID"
    else -> ""
}
