package ru.rtuitlab.itlab.presentation.ui.components.markdown

import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import ru.rtuitlab.itlab.presentation.ui.theme.AppColors




@Composable
fun MarkdownTextArea(
	modifier: Modifier = Modifier,
	textMd: String
) {

	val textColor = LocalContentColor.current.toArgb()
	val linkColor = AppColors.accent.collectAsState().value

	val mdRenderer = Markwon.builder(LocalContext.current)
		.usePlugin(
			object : AbstractMarkwonPlugin() {
				override fun configureTheme(builder: MarkwonTheme.Builder) {
					builder
						.linkColor(linkColor.toArgb())
						.isLinkUnderlined(false)
				}
			}
		)
		.build()

	Box(
		modifier = Modifier
			.fillMaxSize()
			.then(modifier)
	) {
		AndroidView(
			modifier = Modifier
				.fillMaxSize()
				.align(Alignment.TopStart),
			factory = {
				TextView(it).apply {
					setTextColor(textColor)
				}
			}
		) {
			mdRenderer.setMarkdown(it, textMd)
		}
	}
}