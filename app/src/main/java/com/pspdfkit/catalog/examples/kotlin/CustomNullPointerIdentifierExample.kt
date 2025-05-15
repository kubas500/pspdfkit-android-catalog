/*
 *   Copyright © 2020-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */
package com.pspdfkit.catalog.examples.kotlin

import android.content.Context
import android.net.Uri
import com.pspdfkit.catalog.R
import com.pspdfkit.catalog.SdkExample
import com.pspdfkit.catalog.tasks.ExtractAssetTask
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.TabBarHidingMode
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.configuration.sharing.ShareFeatures
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.PdfActivityIntentBuilder

class CustomNullPointerIdentifierExample(context: Context) : SdkExample(
    context,
    R.string.customNullPointerIdentifierExampleTitle,
    R.string.customNullPointerIdentifierExampleTitle
) {

    override fun launchExample(context: Context, configuration: PdfActivityConfiguration.Builder) {
        val cos = PdfActivityConfiguration
            .Builder(context)
            .layout(R.layout.custom_example_null_pointer_identifier_activity)
            .setTabBarHidingMode(TabBarHidingMode.HIDE)
            .disableContentEditing()
            .scrollDirection(PageScrollDirection.HORIZONTAL)
            .scrollDirection(PageScrollDirection.VERTICAL)
            .scrollMode(PageScrollMode.PER_PAGE)
            .layoutMode(PageLayoutMode.SINGLE)
            .fitMode(PageFitMode.FIT_TO_WIDTH)
            .showPageNumberOverlay()
            .showThumbnailGrid()
            .restoreLastViewedPage(false)
            .autosaveEnabled(false)
            .setMultithreadedRenderingEnabled(true)
            .setTabBarHidingMode(TabBarHidingMode.HIDE)
            .hideDocumentTitleOverlay()
            .hideSettingsMenu()
            .disableSearch()
            .setEnabledShareFeatures(ShareFeatures.none())
            .disableDocumentInfoView()
            .disablePrinting()
            .autosaveEnabled(false)

        // We use a custom utility class to extract the example document from the assets.
        ExtractAssetTask.extract("OoPdfFormExamplerTough.pdf", title, context) { documentFile ->
            // To start the `CustomLayoutActivity` create a launch intent using the builder.
            val intent = PdfActivityIntentBuilder.fromUri(context, Uri.fromFile(documentFile))
                .configuration(cos.build())
                .activityClass(CustomNullPointerIdentifierActivity::class)
                .build()
            context.startActivity(intent)
        }
    }
}

class CustomNullPointerIdentifierActivity : PdfActivity()