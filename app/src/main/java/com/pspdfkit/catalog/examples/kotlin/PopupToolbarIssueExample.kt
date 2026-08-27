/*
 *   Copyright © 2025-2026 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.catalog.examples.kotlin

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.MaterialToolbar
import com.pspdfkit.catalog.R
import com.pspdfkit.catalog.SdkExample
import com.pspdfkit.catalog.tasks.ExtractAssetTask
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.TabBarHidingMode
import com.pspdfkit.configuration.activity.UserInterfaceViewMode
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.configuration.sharing.ShareFeatures
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.ui.PSPDFKitViews
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.PdfActivityIntentBuilder
import com.pspdfkit.ui.PdfUi
import com.pspdfkit.ui.special_mode.controller.AnnotationTool
import java.util.EnumSet

class PopupToolbarIssueExample(context: Context) :
    SdkExample(
        context,
        R.string.popupToolbarIssueExampleActivityTitle,
        R.string.popupToolbarIssueExampleActivityTitle,
    ) {
    override fun launchExample(context: Context, configuration: PdfActivityConfiguration.Builder) {
        val configurationOverride = PdfActivityConfiguration
            .Builder(context).apply {
            layout(R.layout.activity_popup_issue)
            scrollDirection(PageScrollDirection.HORIZONTAL)
            scrollDirection(PageScrollDirection.VERTICAL)
            scrollMode(PageScrollMode.PER_PAGE)
            layoutMode(PageLayoutMode.SINGLE)
            fitMode(PageFitMode.FIT_TO_WIDTH)
            title("empty.pdf")
            pageNumberOverlayEnabled(true)
            thumbnailGridEnabled(true)
            restoreLastViewedPage(false)
            autosaveEnabled(false)
            setMultithreadedRenderingEnabled(true)
            setTabBarHidingMode(TabBarHidingMode.HIDE)
            documentTitleOverlayEnabled(false)
            settingsMenuEnabled(false)
            searchEnabled(false)
            setEnabledShareFeatures(EnumSet.noneOf(ShareFeatures::class.java))
            documentInfoViewEnabled(false)
            printingEnabled(false)
            contentEditingEnabled(false)
            annotationEditingEnabled(true)
            formEditingEnabled(true)
            documentEditorEnabled(true)
            navigationButtonsEnabled(false)
        }
        ExtractAssetTask.extract("empty.pdf", title, context) { documentFile ->
            val intent =
                PdfActivityIntentBuilder
                    .fromUri(context, Uri.fromFile(documentFile))
                    .configuration(configurationOverride.build())
                    .activityClass(PopupToolbarIssueExampleActivity::class.java)
                    .build()
            context.startActivity(intent)
        }
    }
}

class PopupToolbarIssueExampleActivity : PdfActivity() {

    private val toolbar
        get() = findViewById<MaterialToolbar>(R.id.toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            insets.getInsets(typeMask).let { systemInsets ->
                toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = systemInsets.top
                }
                insets.inset(
                    0,
                    systemInsets.top,
                    0,
                    0
                )
            }
        }

        toolbar.setOnClickListener {
            if (pspdfKitViews.activeViewType != PSPDFKitViews.Type.VIEW_NONE) {
                pspdfKitViews.toggleView(PSPDFKitViews.Type.VIEW_NONE)
            }
            pspdfKitViews.thumbnailBarView?.post {
                enterAnnotationCreationMode()
            }
        }
    }

    override fun onGenerateMenuItemIds(menuItems: MutableList<Int>): MutableList<Int> =
        menuItems.apply {
            clear()
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = true

    override fun onDocumentLoaded(document: PdfDocument) {
        super.onDocumentLoaded(document)
        //this line causes the issue
        userInterfaceViewMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_VISIBLE
    }
}

fun PdfUi.enterAnnotationCreationMode(vararg additionalConditions: Boolean, annotationTool: AnnotationTool? = null) {
    if(!isAnnotationToolActive() && isAnnotationToolEnabled() && additionalConditions.all { it }) {
        pdfFragment?.exitCurrentlyActiveMode()
        annotationTool?.let {
            pdfFragment?.enterAnnotatingMode(it)
        } ?: pdfFragment?.enterAnnotatingMode()
    }
}

fun PdfUi.isAnnotationToolActive(): Boolean = pdfFragment?.activeAnnotationTool != null
        || pdfFragment?.activeAnnotationToolVariant != null

fun PdfUi.isAnnotationToolEnabled(): Boolean = pdfFragment?.configuration?.isAnnotationEditingEnabled ?: false