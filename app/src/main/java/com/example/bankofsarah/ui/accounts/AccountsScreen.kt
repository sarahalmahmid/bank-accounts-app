package com.example.bankofsarah.ui.accounts

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankofsarah.data.remote.AccountDto
import com.example.bankofsarah.ui.common.formatBalance
import com.example.bankofsarah.ui.theme.BankOfSarahTheme

@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = viewModel(factory = AccountsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AccountsContent(
        state = state,
        onRetry = viewModel::loadAccounts,
        onLogout = viewModel::logout,
    )
}

@Composable
fun AccountsContent(
    state: AccountsUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "My accounts",
                style = MaterialTheme.typography.headlineSmall,
            )
            TextButton(onClick = onLogout) {
                Text("Log out")
            }
        }

        when (state) {
            AccountsUiState.Loading -> CenteredBox {
                CircularProgressIndicator()
            }

            is AccountsUiState.Error -> CenteredBox {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Try again")
                    }
                }
            }

            is AccountsUiState.Success -> {
                if (state.accounts.isEmpty()) {
                    CenteredBox {
                        Text("You don't have any accounts yet.")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.accounts, key = { it.accountNumber }) { account ->
                            AccountCard(
                                account = account,
                                onAddNicknameClick = { },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: AccountDto,
    onAddNicknameClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val nickname = account.nickname
            if (nickname != null) {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.titleMedium,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Account ${account.accountNumber.takeLast(4)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "+ Add nickname",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(
                            role = Role.Button,
                            onClick = onAddNicknameClick,
                        ),
                    )
                }
            }
            Text(
                text = account.accountNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatBalance(account.balance),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsContentPreview() {
    BankOfSarahTheme {
        AccountsContent(
            state = AccountsUiState.Success(
                listOf(
                    AccountDto("200000001", 2000.0, "Salary"),
                    AccountDto("200000002", 4444.99, "Fun money"),
                    AccountDto("200000004", 10000.0, null),
                ),
            ),
            onRetry = {},
            onLogout = {},
        )
    }
}