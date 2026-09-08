/*
 *   Copyright © 2025-2026 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.catalog.examples.kotlin

import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pspdfkit.catalog.R
import com.pspdfkit.catalog.SdkExample
import com.pspdfkit.catalog.tasks.ExtractAssetTask
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.forms.SignatureFormConfiguration
import com.pspdfkit.listeners.DocumentListener
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.utils.getSupportParcelableExtra

class SignatureMultiDocumentIssueExample(context: Context) :
    SdkExample(context, R.string.multiDocIssueExampleActivityTitle, R.string.multiDocIssueExampleActivityTitle) {
    override fun launchExample(context: Context, configuration: PdfActivityConfiguration.Builder) {
        ExtractAssetTask.extract("empty.pdf", title, context) { documentFile1 ->

            ExtractAssetTask.extract("empty2.pdf", title, context) { documentFile2 ->

                val intent = Intent(context, SignatureMultiDocumentIssueExampleActivity::class.java)

                intent.putExtra(SignatureMultiDocumentIssueExampleActivity.EXTRA_URI_1, Uri.fromFile(documentFile1))
                intent.putExtra(SignatureMultiDocumentIssueExampleActivity.EXTRA_URI_2, Uri.fromFile(documentFile2))

                intent.putExtra(
                    SignatureMultiDocumentIssueExampleActivity.EXTRA_CONFIGURATION,
                    PdfConfiguration.Builder()
                        .scrollDirection(PageScrollDirection.VERTICAL)
                        .scrollMode(PageScrollMode.CONTINUOUS)
                        .fitMode(PageFitMode.FIT_TO_WIDTH)
                        .restoreLastViewedPage(false)
                        .layoutMode(PageLayoutMode.SINGLE)
                        .annotationEditingEnabled(false)
                        .setMultithreadedRenderingEnabled(true)
                        .build(),
                )
                context.startActivity(intent)
            }

        }
    }
}

class SignatureMultiDocumentIssueExampleActivity :
    AppCompatActivity(),
    DocumentListener {
    private lateinit var fragment: PdfFragment
    private lateinit var configuration: PdfConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_fragment)

        val documentUri1 = intent.getSupportParcelableExtra(EXTRA_URI_1, Uri::class.java)

        val documentUri2 = intent.getSupportParcelableExtra(EXTRA_URI_2, Uri::class.java)

        configuration = intent.getSupportParcelableExtra(EXTRA_CONFIGURATION, PdfConfiguration::class.java)
            ?: throw IllegalStateException("Activity Intent was missing configuration extra!")

        fragment = PdfFragment.newInstance(listOfNotNull(documentUri1, documentUri2), configuration)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        fragment.addDocumentListener(this)
    }

    override fun onDocumentLoaded(document: PdfDocument) {
        val formProvider = document.formProvider
        val formElements = formProvider.formElements

        formElements
            .forEach { element ->
                formProvider.removeFormElementFromPage(element)
            }

        createSignatureFormFieldForDocument1()
    }

    private fun createSignatureFormFieldForDocument1() {
        val rectFSignatureFormConfiguration = RectF(379.7325f, 65.2541f, 539.7325f, 129.2541f)

        val signatureFormConfiguration =
            SignatureFormConfiguration
                .Builder(0, rectFSignatureFormConfiguration)
                .build()

        fragment.document?.formProvider?.addFormElementToPage("signaturefield-1", signatureFormConfiguration)
    }

    companion object {
        const val EXTRA_CONFIGURATION = "SignatureMultiDocumentIssueExampleActivity.EXTRA_CONFIGURATION"
        const val EXTRA_URI_1 = "SignatureMultiDocumentIssueExampleActivity.EXTRA_URI_1"
        const val EXTRA_URI_2 = "SignatureMultiDocumentIssueExampleActivity.EXTRA_URI_2"
    }
}