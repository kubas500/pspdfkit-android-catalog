/*
 *   Copyright © 2020-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */
package com.pspdfkit.catalog.examples.kotlin

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.RelativeLayout
import androidx.core.view.updateLayoutParams
import com.pspdfkit.catalog.R
import com.pspdfkit.catalog.SdkExample
import com.pspdfkit.catalog.tasks.ExtractAssetTask
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.TabBarHidingMode
import com.pspdfkit.configuration.activity.UserInterfaceViewMode
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.document.editor.page.DialogNewPageFactory
import com.pspdfkit.document.editor.page.NewPageFactory
import com.pspdfkit.document.processor.NewPage
import com.pspdfkit.ui.PSPDFKitViews
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.PdfActivityIntentBuilder
import com.pspdfkit.ui.toolbar.ContextualToolbar
import com.pspdfkit.ui.toolbar.DocumentEditingToolbar
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout.OnContextualToolbarLifecycleListener

class CustomMainToolbarExample(context: Context) : SdkExample(
    context,
    R.string.customMainToolbarExampleTitle,
    R.string.customMainToolbarExampleTitle
) {

    override fun launchExample(context: Context, configuration: PdfActivityConfiguration.Builder) {
        val cos = PdfActivityConfiguration
            .Builder(context)
            .layout(R.layout.custom_example_own_toolbar_activity)
            .setTabBarHidingMode(TabBarHidingMode.HIDE)
            .disableContentEditing()

        // We use a custom utility class to extract the example document from the assets.
        ExtractAssetTask.extract(WELCOME_DOC, title, context) { documentFile ->
            // To start the `CustomLayoutActivity` create a launch intent using the builder.
            val intent = PdfActivityIntentBuilder.fromUri(context, Uri.fromFile(documentFile))
                .configuration(cos.build())
                .activityClass(CustomMainToolbarActivity::class)
                .build()
            context.startActivity(intent)
        }
    }
}

class CustomMainToolbarActivity : PdfActivity() {

    private val thumbnailGrid
        get() = findViewById<RelativeLayout>(R.id.pspdf__activity_thumbnail_grid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pspdfKitViews.thumbnailGridView?.setNewPageFactory {
            DialogNewPageFactory(supportFragmentManager).apply {
                onCreateNewPage(object : NewPageFactory.OnNewPageReadyListener {
                    override fun onNewPageReady(newPage: NewPage) {
                        pspdfKitViews.thumbnailGridView
                            ?.documentEditorSavingToolbarHandler
                            ?.onNewPageReady(newPage)
                    }

                    override fun onCancelled() {}
                })
            }
        }

        findViewById<ToolbarCoordinatorLayout>(R.id.pspdf__toolbar_coordinator).apply {
            setMainToolbarEnabled(false)
            setOnContextualToolbarPositionListener { contextualToolbar, oldPosition, newPosition ->
                newPosition.takeIf { oldPosition != null && oldPosition != newPosition }?.let {
                    contextualToolbar.updateLayout(it)
                }
            }
        }
    }

    private fun ContextualToolbar<*>.updateLayout(position: ToolbarCoordinatorLayout.LayoutParams.Position) {
        when (this) {
            is DocumentEditingToolbar -> thumbnailGrid
            else -> null
        }?.updateLayoutParams(position)
    }

    private fun View.updateLayoutParams(position: ToolbarCoordinatorLayout.LayoutParams.Position) {
        updateLayoutParams<MarginLayoutParams> {
            topMargin = when (position) {
                ToolbarCoordinatorLayout.LayoutParams.Position.TOP -> 80
                else -> -100
            }
        }
    }

    override fun onDocumentLoaded(document: PdfDocument) {
        super.onDocumentLoaded(document)

        setOnContextualToolbarLifecycleListener(onContextualToolbarLifecycleListener)

        userInterfaceViewMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_VISIBLE

        pspdfKitViews.showView(PSPDFKitViews.Type.VIEW_THUMBNAIL_GRID)
        pspdfKitViews.thumbnailGridView?.post {
            pdfFragment?.exitCurrentlyActiveMode()
            pspdfKitViews.thumbnailGridView?.enterDocumentEditingMode()
        }
    }

    private val onContextualToolbarLifecycleListener =
        object : OnContextualToolbarLifecycleListener {
            override fun onPrepareContextualToolbar(p0: ContextualToolbar<*>) {
            }

            override fun onDisplayContextualToolbar(toolbar: ContextualToolbar<*>) {
                toolbar.updateLayout(toolbar.position)
            }

            override fun onRemoveContextualToolbar(p0: ContextualToolbar<*>) {
            }
        }

    override fun onGenerateMenuItemIds(menuItems: MutableList<Int>): MutableList<Int> =
        menuItems.apply {
            clear()
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = true
}