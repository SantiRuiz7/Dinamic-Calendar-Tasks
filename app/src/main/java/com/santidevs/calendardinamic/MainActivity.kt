package com.santidevs.calendardinamic

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.santidevs.calendardinamic.ui.theme.gray
import com.santidevs.calendardinamic.ui.theme.orange
import com.santidevs.calendardinamic.ui.theme.white
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val calendarInputList by remember {
                mutableStateOf(createCalendarList())
            }
            var clickedCalendarElem by remember {
                mutableStateOf<CalendarInput?>(null)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gray),
                contentAlignment = Alignment.TopCenter
            ){
                Calendar(
                    calendarInput = calendarInputList,
                    onDayClick = { day->
                        clickedCalendarElem = calendarInputList.first { it.day == day }
                    },
                    month = "September",
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .align(Alignment.Center)
                ){
                    clickedCalendarElem?.toDos?.forEach { toDo ->
                        if (toDo.showButton) {
                            Card(
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = toDo.text,
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    Button(
                                        onClick = { /* Acción cuando se hace clic en el botón */ },
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(text = "Hecha")
                                    }
                                }
                            }
                        } else {

                            Text(
                                modifier = Modifier
                                    .padding(40.dp),
                                text = toDo.text,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }


    private fun createCalendarList(): List<CalendarInput> {
        val calendarInputs = mutableListOf<CalendarInput>()

        for (i in 1..31) {
            val toDos = mutableListOf(
                ToDoItem("Day $i:", false),
                ToDoItem("8 hs. Hacer Service", true),
                ToDoItem("10 hs. Cambiar Correa", true)
            )
            calendarInputs.add(CalendarInput(i, toDos))
        }
        return calendarInputs

}

private val CALENDAR_ROWS = 5
private val CALENDAR_COLUMNS = 7

@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    calendarInput: List<CalendarInput>,
    onDayClick:(Int)->Unit,
    strokeWidth:Float = 10f,
    month:String
) {

    var canvasSize by remember {
        mutableStateOf(Size.Zero)
    }
    var clickAnimationOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    var animationRadius by remember {
        mutableStateOf(0f)
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = month,
            fontWeight = FontWeight.SemiBold,
            color = white,
            fontSize = 20.sp
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectTapGestures(
                        onTap = { offset ->
                            val column =
                                (offset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
                            val row = (offset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1
                            val day = column + (row - 1) * CALENDAR_COLUMNS
                            if (day <= calendarInput.size) {
                                onDayClick(day)
                                clickAnimationOffset = offset
                                scope.launch {
                                    animate(0f, 225f, animationSpec = tween(300)) { value, _ ->
                                        animationRadius = value
                                    }
                                }
                            }

                        }
                    )
                }
        ){
            val canvasHeight = size.height
            val canvasWidth = size.width
            canvasSize = Size(canvasWidth,canvasHeight)
            val ySteps = canvasHeight/ CALENDAR_ROWS
            val xSteps = canvasWidth/ CALENDAR_COLUMNS

            val column = (clickAnimationOffset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
            val row = (clickAnimationOffset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1

            val path = Path().apply {
                moveTo((column-1)*xSteps,(row-1)*ySteps)
                lineTo(column*xSteps,(row-1)*ySteps)
                lineTo(column*xSteps,row*ySteps)
                lineTo((column-1)*xSteps,row*ySteps)
                close()
            }

            clipPath(path){
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(orange.copy(0.8f), orange.copy(0.2f)),
                        center = clickAnimationOffset,
                        radius = animationRadius + 0.1f
                    ),
                    radius = animationRadius + 0.1f,
                    center = clickAnimationOffset
                )
            }

            drawRoundRect(
                orange,
                cornerRadius = CornerRadius(5f,5f),
                style = Stroke(
                    width = strokeWidth
                )
            )

            for(i in 1 until CALENDAR_ROWS){
                drawLine(
                    color = orange,
                    start = Offset(0f,ySteps*i),
                    end = Offset(canvasWidth, ySteps*i),
                    strokeWidth = strokeWidth
                )
            }
            for(i in 1 until CALENDAR_COLUMNS){
                drawLine(
                    color = orange,
                    start = Offset(xSteps*i,0f),
                    end = Offset(xSteps*i, canvasHeight),
                    strokeWidth = strokeWidth
                )
            }
            val textHeight = 12.dp.toPx()
            for(i in calendarInput.indices){
                val textPositionX = xSteps * (i% CALENDAR_COLUMNS) + strokeWidth
                val textPositionY = (i / CALENDAR_COLUMNS) * ySteps + textHeight + strokeWidth/2
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${i+1}",
                        textPositionX,
                        textPositionY,
                        Paint().apply {
                            textSize = textHeight
                            color = white.toArgb()
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }
}

}

data class ToDoItem(
    val text: String,
    val showButton: Boolean,
)



data class CalendarInput(
    val day:Int,
    val toDos:List<ToDoItem> = emptyList()
)