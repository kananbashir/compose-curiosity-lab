package com.example.compose_curiosity_lab.draganddrop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DraggableLazyRow(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {

        }
    }
}

@Preview (showBackground = true)
@Composable
private fun DraggableLazyRowPreview() {
    DraggableLazyRow()
}