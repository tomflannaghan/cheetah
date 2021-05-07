package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ProgressTextField(
    text: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    isBusy: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    modifier: Modifier = Modifier,
) {
    val showProgressIndicator = remember { mutableStateOf(false) }
    val colors = if (isError)
        TextFieldDefaults.textFieldColors(MaterialTheme.colors.error)
    else
        TextFieldDefaults.textFieldColors()

    LaunchedEffect(isBusy) {
        if (isBusy) delay(1000)
        showProgressIndicator.value = isBusy
    }

    Box {
        Row {
            TextField(
                text,
                onValueChange = onValueChange,
                colors = colors,
                modifier = modifier.weight(1f),
                keyboardOptions = keyboardOptions
            )
        }
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.matchParentSize()
        ) {
            if (showProgressIndicator.value) {
                CircularProgressIndicator()
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}