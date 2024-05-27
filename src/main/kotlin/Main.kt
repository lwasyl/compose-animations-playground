import androidx.compose.animation.*
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

data class UiModel(
    val contentSize: ContentSize,
) {
    enum class ContentSize(val size: Dp) {
        Small(100.dp),
        Medium(200.dp),
        Large(300.dp),
    }

    fun next() = copy(
        contentSize = if (contentSize == ContentSize.Medium) ContentSize.Large else ContentSize.Medium,
    )
}

@Composable
@Preview
fun App() {
    var uiModel by remember {
        mutableStateOf(
            UiModel(
                contentSize = UiModel.ContentSize.Medium,
            )
        )
    }

//    AnimatedContent(
//        targetState = uiModel,
//        transitionSpec = { fadeIn().togetherWith(fadeOut()).using(null) },
//        contentKey = { "constant" },
//        label = "Root container",
//    ) { outerTarget ->
    Box(
        modifier = Modifier
            .width(
                width = 300.dp,
            )
            .wrapContentHeight()
            .background(Color.Red)
            .clickable { uiModel = uiModel.next() }
            .padding(16.dp)
    ) {

        AnimatedContent(
            modifier = Modifier
//                    .animateContentSize(animationSpec = tween(2000))
                .fillMaxWidth(),
            targetState = uiModel,
            transitionSpec = {
                if (targetState.contentSize.ordinal > initialState.contentSize.ordinal) {
                    newStateVisibleAtTheEnd
                } else {
                    newStateVisibleImmediately
                }
                    .using(SizeTransform(
                        clip = false,
                        sizeAnimationSpec = { initialSize, targetSize ->
                            println("going $initialSize -> $targetSize")
                            tween(CONTENT_TRANSFORM_DURATION)
                        }
                    ))
//                    .using(null)
            },

            contentKey = { it.contentSize },
            label = "Inner container",
        ) { innerTarget ->

            Box(
                modifier = Modifier
                    .background(Color.Green)
                    .animateContentSize(animationSpec = tween(CONTENT_TRANSFORM_DURATION))
                    .fillMaxWidth()
                    .clickable { uiModel = uiModel.next() }
            ) {
//                Text(
//                    modifier = Modifier
//                        .wrapContentHeight()
//                        .sizeIn(minWidth = 200.dp, minHeight = 100.dp)
////                            .height(animateDpAsState(textHeight.dp).value)
//                        .padding(bottom = 16.dp),
//                    text = loremIpsum(),
//                    maxLines = if (innerTarget.contentSize == UiModel.ContentSize.Medium) 1 else Int.MAX_VALUE,
//                )

                Box(
                    modifier = Modifier
                        .animateContentSize(animationSpec = tween(CONTENT_TRANSFORM_DURATION))
                        .size(if (innerTarget.contentSize == UiModel.ContentSize.Medium) 200.dp else 100.dp)
                        .background(Color.Yellow)
                ) {

                    Text(
                        modifier = Modifier.align(Alignment.BottomStart),
                        text = "Bottom ttext"
                    )
                }
            }
//            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 400.dp)
    ) {
        MaterialTheme {
            CompositionLocalProvider(LocalIndication provides NoIndication) {
                App()
            }
        }
    }
}

fun loremIpsum() = """
    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
    Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor.
    Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi.
    Proin porttitor, orci nec nonummy molestie, enim est eleifend mi, non fermentum diam velit sed nibh.
    Quisque volutpat condimentum velit. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.
    Nam nec ante. Sed lacinia, urna non tincidunt mattis, tortor neque adipiscing diam, a cursus ipsum ante quis turpis.
    Nulla facilisi. Ut fringilla. Suspendisse potenti. Nunc feugiat mi a tellus consequat imperdiet. Vestibulum sapien. Proin quam. Etiam ultrices. Suspendisse in justo eu magna luctus suscipit.
    """.trimIndent()

object NoIndication : Indication {

    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource) = Instance

    object Instance : IndicationInstance {
        override fun ContentDrawScope.drawIndication() {
            drawContent()
        }
    }
}

val enterTransition = fadeIn(tween(durationMillis = 1500, delayMillis = 1500))
val exitTransition = fadeOut(tween(durationMillis = 1500))

val animatedContentTransform = enterTransition togetherWith exitTransition

const val CONTENT_TRANSFORM_DURATION = 3500
val animatedContentTransformWithoutClip = ContentTransform(
    targetContentEnter = fadeIn(
        tween(
            durationMillis = CONTENT_TRANSFORM_DURATION,
            delayMillis = CONTENT_TRANSFORM_DURATION
        )
    ),
    initialContentExit = fadeOut(tween(durationMillis = CONTENT_TRANSFORM_DURATION)),
    targetContentZIndex = 0f,
    sizeTransform = SizeTransform(clip = false),
)

val bothStatesVisibleContentTransform = fadeIn(snap(0)) togetherWith fadeOut(snap(CONTENT_TRANSFORM_DURATION))

val newStateVisibleImmediately = fadeIn(snap(0)) togetherWith fadeOut(snap(0))
val newStateVisibleAtTheEnd =
    fadeIn(snap(CONTENT_TRANSFORM_DURATION)) togetherWith fadeOut(snap(delayMillis = CONTENT_TRANSFORM_DURATION))