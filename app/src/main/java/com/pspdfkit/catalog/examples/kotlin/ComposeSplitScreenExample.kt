/*
 *   Copyright © 2026 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.catalog.examples.kotlin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pspdfkit.catalog.R
import com.pspdfkit.catalog.SdkExample
import com.pspdfkit.catalog.tasks.ExtractAssetTask
import com.pspdfkit.catalog.ui.theming.CatalogTheme
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.ThumbnailBarMode
import com.pspdfkit.configuration.activity.UserInterfaceViewMode
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.jetpack.compose.interactors.rememberDocumentState
import com.pspdfkit.jetpack.compose.views.DocumentView
import com.pspdfkit.utils.getSupportParcelableExtra

/**
 * Shows two documents side by side, each in its own [DocumentView] composable. The two views are fully independent:
 * they show different documents and each one is created with its own [PdfActivityConfiguration], so scrolling,
 * zooming, searching or annotating in one pane leaves the other untouched.
 */
class ComposeSplitScreenExample(context: Context) :
    SdkExample(context, R.string.composeSplitScreenExampleTitle, R.string.composeSplitScreenExampleDescription) {
    override fun launchExample(context: Context, configuration: PdfActivityConfiguration.Builder) {
        // Both documents live in the app's assets and need to be extracted to internal storage before they can be
        // opened. Each pane gets its own document, so we extract both before launching the activity.
        ExtractAssetTask.extract(WELCOME_DOC, title, context) { leadingFile ->
            ExtractAssetTask.extract(ANNUAL_REPORT_DOC, title, context) { trailingFile ->
                val intent = Intent(context, ComposeSplitScreenActivity::class.java)
                intent.putExtra(ComposeSplitScreenActivity.EXTRA_LEADING_URI, Uri.fromFile(leadingFile))
                intent.putExtra(ComposeSplitScreenActivity.EXTRA_TRAILING_URI, Uri.fromFile(trailingFile))
                context.startActivity(intent)
            }
        }
    }

    private companion object {
        const val ANNUAL_REPORT_DOC = "AnnualReport.pdf"
    }
}

class ComposeSplitScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val leadingUri = intent.getSupportParcelableExtra(EXTRA_LEADING_URI, Uri::class.java)!!
        val trailingUri = intent.getSupportParcelableExtra(EXTRA_TRAILING_URI, Uri::class.java)!!

        setContent {
            CatalogTheme {
                // A plain Row is all that's needed to place two documents next to each other: each DocumentView
                // hosts its own PDF fragment, and Compose keeps both of them alive side by side.
                Row(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                    // The leading view scrolls continuously in the vertical direction.
                    DocumentView(
                        documentState = rememberDocumentState(leadingUri, continuousVerticalConfiguration()),
                        modifier = Modifier.weight(1f),
                    )

                    VerticalDivider(modifier = Modifier.fillMaxHeight())

                    // The trailing view scrolls page by page in the horizontal direction.
                    DocumentView(
                        documentState = rememberDocumentState(trailingUri, perPageHorizontalConfiguration()),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    /**
     * Configuration shared by both panes. Each pane is only half as wide as the window, so the thumbnail bar is
     * disabled to leave as much room as possible for the page itself.
     */
    @Composable
    private fun splitPaneConfiguration() = PdfActivityConfiguration
        .Builder(LocalContext.current)
        .layoutMode(PageLayoutMode.SINGLE)
        .setThumbnailBarMode(ThumbnailBarMode.THUMBNAIL_BAR_MODE_NONE)
        // Keep both toolbars on screen so it's obvious that each pane has its own independent UI.
        .setUserInterfaceViewMode(UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_VISIBLE)

    @Composable
    private fun continuousVerticalConfiguration() = splitPaneConfiguration()
        .scrollDirection(PageScrollDirection.VERTICAL)
        .scrollMode(PageScrollMode.CONTINUOUS)
        .fitMode(PageFitMode.FIT_TO_WIDTH)
        .build()

    @Composable
    private fun perPageHorizontalConfiguration() = splitPaneConfiguration()
        .scrollDirection(PageScrollDirection.HORIZONTAL)
        .scrollMode(PageScrollMode.PER_PAGE)
        .fitMode(PageFitMode.FIT_TO_SCREEN)
        .build()

    companion object {
        const val EXTRA_LEADING_URI = "ComposeSplitScreenActivity.LeadingDocumentUri"
        const val EXTRA_TRAILING_URI = "ComposeSplitScreenActivity.TrailingDocumentUri"
    }
}
