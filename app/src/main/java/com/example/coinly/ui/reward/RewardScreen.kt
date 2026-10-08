package com.example.coinly.ui.reward

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coinly.domain.model.CoinBalance
import com.example.coinly.domain.model.TaskItem
import org.koin.androidx.compose.koinViewModel

/**
 * Jetpack Compose screen for Coinly rewards and claim flow.
 * Adheres to Material 3 design and Clean Architecture presentation layer guidelines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardScreen(
    viewModel: RewardViewModel = koinViewModel()
) {
    val coinBalance by viewModel.coinBalance.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ClaimUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            is ClaimUiState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                viewModel.resetState()
            }
            is ClaimUiState.FraudAlert -> {
                snackbarHostState.showSnackbar("Fraud Prevention Alert: ${state.reason}")
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coinly Rewards", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Security Protected")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Balance Header Card
                item {
                    BalanceCard(
                        balance = coinBalance,
                        onRedeemClick = { viewModel.redeemCoins() },
                        isLoading = uiState is ClaimUiState.Loading
                    )
                }

                // Section Title: Available Tasks
                item {
                    Text(
                        text = "Available Tasks & Offers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Task Items List
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onClaimClick = { viewModel.claimReward(task.id) },
                        isLoading = uiState is ClaimUiState.Loading
                    )
                }
            }

            // Loading Overlay when claiming
            if (uiState is ClaimUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun BalanceCard(
    balance: CoinBalance,
    onRedeemClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Coin Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${balance.totalCoins}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cash Value: $%.2f (Redeem threshold: 1,000 coins)".format(balance.cashValueUsd),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (balance.totalCoins.toFloat() / balance.redemptionThreshold).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRedeemClick,
                enabled = balance.canRedeem && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (balance.canRedeem) "Redeem $10.00 Cash Payout" else "Need ${balance.redemptionThreshold - balance.totalCoins} more coins to redeem",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskItem,
    onClaimClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "+${task.rewardCoins} Coins",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (task.isCompleted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Claimed",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Button(
                    onClick = onClaimClick,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Claim")
                }
            }
        }
    }
}
