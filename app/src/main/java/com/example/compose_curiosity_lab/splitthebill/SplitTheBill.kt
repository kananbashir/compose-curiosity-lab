package com.example.compose_curiosity_lab.splitthebill

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose_curiosity_lab.R

/**
 * Created on 8/9/2024
 * @author Kanan Bashir
 */

@Composable
fun SplitTheBill(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            text = "Fair Share",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif
        )

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth(),
            columns = GridCells.Fixed(3)
        ) {
            items(personList, key = { it.name }) { person ->
                PersonItem(person)
            }
        }
    }
}

@Composable
fun PersonItem(
    item: PersonItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    .size(100.dp)
                    .shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        borderRadius = 70.dp,
                        blurRadius = 20.dp,
                        offsetY = 80.dp,
                        spread = 3.dp
                    )
                    .clip(RoundedCornerShape(60f)),
                painter = painterResource(id = item.photo),
                contentDescription = "Person photo",
                contentScale = ContentScale.Crop
            )

            Text(
                text = item.name,
                color = Color.Gray
            )

            SplitAmountBubble()
        }
    }
}

@Composable
fun SplitAmountBubble(modifier: Modifier = Modifier) {
    
}

private fun Modifier.shadow(
    color: Color = Color.Black,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    spread: Dp = 0f.dp,
    modifier: Modifier = Modifier
) = this.then(
    modifier.drawBehind {
        this.drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            val spreadPixel = spread.toPx()
            val leftPixel = (0f - spreadPixel) + offsetX.toPx()
            val topPixel = (0f - spreadPixel) + offsetY.toPx()
            val rightPixel = (this.size.width + spreadPixel)
            val bottomPixel = (this.size.height + spreadPixel)

            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter =
                    (BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL))
            }

            frameworkPaint.color = color.toArgb()

            it.drawRoundRect(
                left = leftPixel,
                top = topPixel,
                right = rightPixel,
                bottom = bottomPixel,
                radiusX = borderRadius.toPx(),
                radiusY = borderRadius.toPx(),
                paint
            )
        }
    }
)


//-------------------------------- Preview related things --------------------------------
val personList: List<PersonItem> = listOf(
    PersonItem(
        id = 1,
        name = "Emily",
        surname = "Carter",
        photo = R.drawable.stb_photo_emily
    ),

    PersonItem(
        id = 2,
        name = "John",
        surname = "Mitchell",
        photo = R.drawable.stb_photo_john
    ),

    PersonItem(
        id = 3,
        name = "Sofia",
        surname = "Hernandez",
        photo = R.drawable.stb_photo_sofia
    ),

    PersonItem(
        id = 4,
        name = "Michael",
        surname = "Thompson",
        photo = R.drawable.stb_photo_michael
    ),

    PersonItem(
        id = 5,
        name = "Olivia",
        surname = "Garcia",
        photo = R.drawable.stb_photo_olivia
    ),

    PersonItem(
        id = 6,
        name = "James",
        surname = "Smith",
        photo = R.drawable.stb_photo_james
    ),

    PersonItem(
        id = 7,
        name = "Isabella",
        surname = "Roberts",
        photo = R.drawable.stb_photo_isabella
    ),

    PersonItem(
        id = 8,
        name = "David",
        surname = "Johnson",
        photo = R.drawable.stb_photo_david
    ),
)

@Preview (showBackground = true)
@Composable
private fun SplitTheBillPreview() {
    SplitTheBill()
}