package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HibretBottomNav
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.CampaignCreationScreen
import com.example.ui.screens.CampaignDetailScreen
import com.example.ui.screens.DonationAmountScreen
import com.example.ui.screens.DonationConfirmScreen
import com.example.ui.screens.DonationPaymentScreen
import com.example.ui.screens.DonationSuccessScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MyActivityScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HibretViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HibretApp()
            }
        }
    }
}

@Composable
fun HibretApp(
    viewModel: HibretViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val selectedCampaignId by viewModel.selectedCampaignId.collectAsState()
    val receiptForDialog by viewModel.receiptForDialog.collectAsState()

    // Primary destinations where bottom navigation is displayed
    val isPrimaryDestination = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.EXPLORE,
        AppScreen.MY_ACTIVITY,
        AppScreen.PROFILE
    )

    // Android Hardware & Gesture Back Handler
    BackHandler(enabled = !isPrimaryDestination || currentScreen != AppScreen.HOME) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isPrimaryDestination) {
                HibretBottomNav(
                    currentScreen = currentScreen,
                    language = language,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isPrimaryDestination) Modifier.padding(bottom = innerPadding.calculateBottomPadding()) else Modifier)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(viewModel = viewModel)
                    }

                    AppScreen.ONBOARDING -> {
                        OnboardingScreen(viewModel = viewModel)
                    }

                    AppScreen.LOGIN -> {
                        LoginScreen(viewModel = viewModel)
                    }

                    AppScreen.REGISTRATION -> {
                        RegistrationScreen(viewModel = viewModel)
                    }

                    AppScreen.HOME -> {
                        HomeScreen(viewModel = viewModel)
                    }

                    AppScreen.EXPLORE -> {
                        ExploreScreen(viewModel = viewModel)
                    }

                    AppScreen.CAMPAIGN_DETAIL -> {
                        CampaignDetailScreen(
                            campaignId = selectedCampaignId ?: "camp_001",
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.DONATION_AMOUNT -> {
                        DonationAmountScreen(
                            campaignId = selectedCampaignId ?: "camp_001",
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.DONATION_PAYMENT -> {
                        DonationPaymentScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.DONATION_CONFIRM -> {
                        DonationConfirmScreen(
                            campaignId = selectedCampaignId ?: "camp_001",
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.DONATION_SUCCESS -> {
                        DonationSuccessScreen(viewModel = viewModel)
                    }

                    AppScreen.CAMPAIGN_CREATION -> {
                        CampaignCreationScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.MY_ACTIVITY -> {
                        MyActivityScreen(viewModel = viewModel)
                    }

                    AppScreen.PROFILE -> {
                        ProfileScreen(viewModel = viewModel)
                    }

                    AppScreen.NOTIFICATIONS -> {
                        NotificationsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }

    // Official Receipt Dialog (available globally whenever invoked)
    receiptForDialog?.let { donation ->
        ReceiptDialog(
            donation = donation,
            language = language,
            onDismiss = { viewModel.dismissReceiptDialog() },
            onShare = { /* Share intent */ }
        )
    }
}
