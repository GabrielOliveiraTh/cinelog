package com.gabriel.cinelog.core.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.gabriel.cinelog.core.designsystem.R as DesignSystemR

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(DesignSystemR.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.core_ui_back),
        )
    }
}
