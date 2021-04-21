package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressTextField(
    text: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    isBusy: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions()
) {
    val colors = if (isError)
        TextFieldDefaults.textFieldColors(MaterialTheme.colors.error)
    else
        TextFieldDefaults.textFieldColors()

    Box {
        Row {
            TextField(
                text,
                onValueChange = onValueChange,
                colors = colors,
                modifier = Modifier.weight(1f),
                keyboardOptions = keyboardOptions
            )
        }
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.matchParentSize()
        ) {
            if (isBusy) {
                CircularProgressIndicator()
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}