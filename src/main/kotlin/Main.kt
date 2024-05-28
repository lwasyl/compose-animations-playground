import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.flow.map

@Composable
fun AnimationIssue(
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val item = UiModel(
        itemsCount = if (expanded) 10 else 3,
        onClicked = { expanded = !expanded },
    )

    AnimatedContent(
        modifier = Modifier,
        targetState = item,
        transitionSpec = { animatedContentTransform },
        label = "OuterAnimationLabel",
        contentKey = { "constant key" },
    ) { model ->

        val obj = Object()
        val data = NewData(
            items = model.itemsCount,
            lambdaWithCapture = { obj },
        )
        Box(
            modifier = modifier
                .wrapContentHeight()
                .padding(horizontal = 16.dp)
                .background(Color.Green)
                .clickable(onClick = { expanded = !expanded }),
        ) {

            AnimatedContent(
                targetState = data,
                modifier = Modifier,
                transitionSpec = {
                    // if (initialState::class != targetState::class) {
                    animatedContentTransform
                    // } else {
                    //     animatedContentTransform.using(sizeTransform = null)
                    // }
                },
                label = "MiddleAnimationLabel",
                contentKey = { it::class },
            ) { targetData ->
                AnimatedContent(
                    targetState = targetData.items,
                    transitionSpec = { bothStatesVisibleContentTransform },
                    label = "InnerAnimationLabel",
                ) { targetItems ->
                    Box(
                        modifier = Modifier.width(150.dp).height(48.dp * targetItems).background(Color.Gray),
                    )
                }
            }
        }
    }
}

@Immutable
data class NewData(
    val items: Int,
    val lambdaWithCapture: Any,
)

@Immutable
data class UiModel(
    val itemsCount: Int,
    val onClicked: suspend () -> Unit,
)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 400.dp, height = 800.dp)
    ) {
        MaterialTheme {
            CompositionLocalProvider(LocalIndication provides CircleIndicator) {
                AnimationIssue()
            }
        }
    }
}

const val CONTENT_TRANSFORM_DURATION = 1000

val animatedContentTransform = fadeIn(tween(durationMillis = 500, delayMillis = 500)).togetherWith(fadeOut(tween(durationMillis = 500)))
val bothStatesVisibleContentTransform = fadeIn(snap(0)) togetherWith fadeOut(snap(CONTENT_TRANSFORM_DURATION))

object CircleIndicator : Indication {

    private class Instance(
        private val pressedInteraction: State<PressInteraction.Press?>,
    ) : IndicationInstance {

        override fun ContentDrawScope.drawIndication() {
            drawContent()

            pressedInteraction.value
                ?.let { drawCircle(Color.Red, radius = 5.dp.toPx(), center = it.pressPosition) }
        }
    }

    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
        val pressInteractions = interactionSource.interactions
            .map { it as? PressInteraction.Press }
            .collectAsState(null)

        return remember(interactionSource) {
            Instance(pressInteractions)
        }
    }
}
