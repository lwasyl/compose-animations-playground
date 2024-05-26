import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var uiModel by remember { mutableStateOf(UiModel(contentSize = UiModel.ContentSize.Medium)) }

    Box(
        modifier = Modifier
            .width(width = 300.dp)
            .wrapContentHeight()
            .background(Color.Red)
            .clickable { uiModel = uiModel.next() }
            .padding(8.dp)
    ) {

        Box(
            modifier = Modifier
                .background(Color.Green)
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Yellow)
            ) {

                AnimatedContent(
                    modifier = Modifier
                        .wrapContentHeight(),
                    targetState = uiModel,
                    transitionSpec = {
                        if (targetState.contentSize.size > initialState.contentSize.size) {
                            newStateVisibleImmediately
                        } else {
                            newStateVisibleAtTheEnd
                        }
                            .using(SizeTransform(sizeAnimationSpec = { _, _ -> tween(500) }))
                    },
                    contentKey = { it.contentSize },
                    label = "Inner container",
                ) { targetModel ->
                    Text(
                        modifier = Modifier,
                        text = loremIpsum(),
                        maxLines = if (targetModel.contentSize == UiModel.ContentSize.Large) 10 else 1
                    )
                }

                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "Bottom text"
                )
            }
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

val enterTransition = fadeIn(tween(durationMillis = 500, delayMillis = 500))
val exitTransition = fadeOut(tween(durationMillis = 500))

val animatedContentTransform = enterTransition togetherWith exitTransition

const val CONTENT_TRANSFORM_DURATION = 1500
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
