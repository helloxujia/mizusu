package com.zayu.mizu.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayu.mizu.R
import com.zayu.mizu.ui.component.rebootlistpopup.RebootListPopup
import com.zayu.mizu.ui.theme.MizSUSprings
import com.zayu.mizu.ui.theme.MizuSUColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePagerMizuSU(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MizuSUColors.PinkBase,
                        MizuSUColors.PinkLight,
                        MizuSUColors.SoftWhite,
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(bottom = bottomInnerPadding)
        ) {
            // Top bar
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MizuSUColors.DeepBrown,
                        )
                        if (state.isManager) {
                            Text(
                                text = buildString {
                                    append(if (state.lkmMode == true) "LKM" else "GKI")
                                    if (state.isSafeMode) append(" · Safe")
                                    if (state.isLateLoadMode) append(" · LateLoad")
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MizuSUColors.RosePrimary,
                            )
                        }
                    }
                },
                actions = {
                    RebootListPopup()
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MizuSUColors.PinkLight.copy(alpha = 0.92f),
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status card
            MizuSUStatusCard(state = state, actions = actions)

            Spacer(modifier = Modifier.height(12.dp))

            // Quick actions row
            MizuSUQuickActions(state = state, actions = actions)

            Spacer(modifier = Modifier.height(12.dp))

            // Welcome card
            if (state.showWelcome) {
                MizuSUWelcomeCard(onDismiss = actions.onDismissWelcome)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Update card
            if (state.hasUpdate && state.checkUpdateEnabled) {
                MizuSUInfoCard(
                    icon = Icons.Filled.InstallMobile,
                    title = "检查更新",
                    subtitle = state.latestVersionInfo.toString(),
                    containerColor = MizuSUColors.GoldAccent.copy(alpha = 0.15f),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // System info card
            MizuSUSystemInfo(state = state)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MizuSUStatusCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val statusColor = when {
        !state.isManager -> MizuSUColors.BlueGrey
        state.showRequireKernelWarning -> MizuSUColors.GoldAccent
        else -> MizuSUColors.RosePrimary
    }
    val statusText = when {
        !state.isManager -> stringResource(id = R.string.home_not_installed)
        state.showRequireKernelWarning -> "需要更新内核"
        else -> stringResource(id = R.string.home_working)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MizuSUColors.PinkLight.copy(alpha = 0.85f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(statusColor, statusColor.copy(alpha = 0.4f)),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge,
                color = MizuSUColors.DeepBrown,
                fontWeight = FontWeight.SemiBold,
            )
            if (state.isManager) {
                Text(
                    text = "KernelSU v${state.ksuVersion}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MizuSUColors.DeepBrown.copy(alpha = 0.6f),
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "安装",
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MizuSUColors.RosePrimary)
                        .clickable { actions.onInstallClick }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun MizuSUQuickActions(
    state: HomeUiState,
    actions: HomeActions,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MizuSUActionChip(
            modifier = Modifier.weight(1f),
            label = "超级用户",
            count = state.superuserCount,
            containerColor = MizuSUColors.RosePrimary.copy(alpha = 0.12f),
            onClick = actions.onSuperuserClick,
        )
        MizuSUActionChip(
            modifier = Modifier.weight(1f),
            label = "模块",
            count = state.moduleCount,
            containerColor = MizuSUColors.BlueGrey.copy(alpha = 0.12f),
            onClick = actions.onModuleClick,
        )
    }
}

@Composable
private fun MizuSUActionChip(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium,
                color = MizuSUColors.DeepBrown,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MizuSUColors.DeepBrown.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun MizuSUWelcomeCard(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MizuSUColors.BlueGrey.copy(alpha = 0.1f),
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = MizuSUColors.DeepBrown,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "KernelSU 二次元美化分支 · 仅对管理器 UI 进行个性化增强",
                style = MaterialTheme.typography.bodyMedium,
                color = MizuSUColors.DeepBrown.copy(alpha = 0.65f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "了解",
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MizuSUColors.BlueGrey.copy(alpha = 0.3f))
                    .clickable { onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MizuSUColors.DeepBrown,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun MizuSUInfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color = MizuSUColors.PinkLight.copy(alpha = 0.5f),
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MizuSUColors.DeepBrown,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MizuSUColors.DeepBrown,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MizuSUColors.DeepBrown.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun MizuSUSystemInfo(state: HomeUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MizuSUColors.PinkLight.copy(alpha = 0.4f),
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "系统信息",
                style = MaterialTheme.typography.titleSmall,
                color = MizuSUColors.DeepBrown,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            MizuSUInfoRow("内核", state.kernelVersion.toString())
            MizuSUInfoRow("管理器", "v${state.currentManagerVersionCode}")
            MizuSUInfoRow("SELinux", state.systemInfo.toString())
        }
    }
}

@Composable
private fun MizuSUInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MizuSUColors.DeepBrown.copy(alpha = 0.55f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MizuSUColors.DeepBrown,
            fontWeight = FontWeight.Medium,
        )
    }
}
