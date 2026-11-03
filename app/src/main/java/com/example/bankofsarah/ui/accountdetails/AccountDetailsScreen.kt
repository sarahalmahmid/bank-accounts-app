package com.example.bankofsarah.ui.accountdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankofsarah.data.remote.AccountDto
import com.example.bankofsarah.ui.common.formatBalance
import com.example.bankofsarah.ui.theme.BankOfSarahTheme

@Composable
fun AccountDetailsScreen(
    onBack: () -> Unit,
    viewModel: AccountDetailsViewModel = viewModel(factory = AccountDetailsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AccountDetailsContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::loadAccount,
        onEditNickname = viewModel::openEditor,
    )

    val editor = state.editor
    val account = state.account
    if (editor != null && account != null) {
        NicknameDialog(
            isNew = account.nickname == null,
            editor = editor,
            onNicknameChange = viewModel::onNicknameChange,
            onSave = viewModel::saveNickname,
            onDismiss = viewModel::dismissEditor,
        )
    }
}

@Composable
fun AccountDetailsContent(
    state: AccountDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEditNickname: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        ) {
            Text("← Back")
        }

        Text(
            text = "Account details",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val account = state.account
        when {
            state.isLoading && account == null -> CenteredBox {
                CircularProgressIndicator()
            }

            account == null -> CenteredBox {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.loadError ?: "Something went wrong.",
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Try again")
                    }
                }
            }

            else -> AccountDetailsCard(
                account = account,
                onEditNickname = onEditNickname,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun AccountDetailsCard(
    account: AccountDto,
    onEditNickname: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DetailItem(
                    label = "Nickname",
                    value = account.nickname ?: "No nickname",
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onEditNickname) {
                    Text(if (account.nickname == null) "Add" else "Edit")
                }
            }
            HorizontalDivider()
            DetailItem(label = "Account number", value = account.accountNumber)
            HorizontalDivider()
            DetailItem(label = "Balance", value = formatBalance(account.balance))
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun NicknameDialog(
    isNew: Boolean,
    editor: NicknameEditorState,
    onNicknameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var fieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = editor.text,
                selection = TextRange(editor.text.length),
            ),
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add nickname" else "Edit nickname") },
        text = {
            Column {
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = { newValue ->
                        val limited = newValue.copy(text = newValue.text.take(MAX_NICKNAME_LENGTH))
                        fieldValue = limited
                        onNicknameChange(limited.text)
                    },
                    label = { Text("Nickname") },
                    singleLine = true,
                    enabled = !editor.isSaving,
                    isError = editor.error != null,
                    supportingText = {
                        Text(
                            text = "${editor.text.length} / $MAX_NICKNAME_LENGTH",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSave() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
                if (editor.error != null) {
                    Text(
                        text = editor.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (!isNew) {
                    Text(
                        text = "Leave empty to remove the nickname.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !editor.isSaving) {
                if (editor.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !editor.isSaving) {
                Text("Cancel")
            }
        },
    )
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
private fun AccountDetailsPreview() {
    BankOfSarahTheme {
        AccountDetailsContent(
            state = AccountDetailsUiState(
                isLoading = false,
                account = AccountDto("200000001", 2000.0, "Salary"),
            ),
            onBack = {},
            onRetry = {},
            onEditNickname = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NicknameDialogPreview() {
    BankOfSarahTheme {
        NicknameDialog(
            isNew = false,
            editor = NicknameEditorState(text = "Salary"),
            onNicknameChange = {},
            onSave = {},
            onDismiss = {},
        )
    }
}