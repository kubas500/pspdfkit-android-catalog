/*
 *   Copyright © 2020-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */
package com.pspdfkit.catalog.examples.kotlin

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.pspdfkit.catalog.R
import com.pspdfkit.catalog.SdkExample
import com.pspdfkit.catalog.examples.kotlin.SignatureActivityContract.Companion.RESULT_EXTRA_SIGNATURE
import com.pspdfkit.catalog.tasks.ExtractAssetTask
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.TabBarHidingMode
import com.pspdfkit.signatures.Signature
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.PdfActivityIntentBuilder

class CustomSignatureParseExample(context: Context) : SdkExample(
    context,
    R.string.customSignatureParseExampleTitle,
    R.string.customSignatureParseExampleTitle
) {

    override fun launchExample(context: Context, configuration: PdfActivityConfiguration.Builder) {
        val cos = PdfActivityConfiguration
            .Builder(context)
            .layout(R.layout.custom_example_signature_parse_activity)
            .setTabBarHidingMode(TabBarHidingMode.HIDE)
            .disableContentEditing()

        // We use a custom utility class to extract the example document from the assets.
        ExtractAssetTask.extract(WELCOME_DOC, title, context) { documentFile ->
            // To start the `CustomLayoutActivity` create a launch intent using the builder.
            val intent = PdfActivityIntentBuilder.fromUri(context, Uri.fromFile(documentFile))
                .configuration(cos.build())
                .activityClass(CustomSignatureParseActivity::class)
                .build()
            context.startActivity(intent)
        }
    }
}

class CustomSignatureParseActivity : PdfActivity() {

    private val addSignatureContract =
        registerForActivityResult(SignatureActivityContract()) { signature ->
            Log.d("CustomSignatureParseActivity", signature.toString())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.crash_button).setOnClickListener {
            addSignatureContract.launch("tag")
        }
    }
}

class SignatureActivityContract :
    ActivityResultContract<String, Signature?>() {

    override fun createIntent(
        context: Context,
        input: String,
    ): Intent = Intent(context, ResultSignatureActivity::class.java)

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Signature? = if (resultCode == RESULT_OK) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getParcelable(
                RESULT_EXTRA_SIGNATURE,
                Signature::class.java
            )
        } else {
            intent?.extras?.getParcelable<Signature>(RESULT_EXTRA_SIGNATURE)
        }
    } else null

    companion object {
        val RESULT_EXTRA_SIGNATURE = "SignatureActivityContract.RESULT_EXTRA_SIGNATURE"
    }
}

class ResultSignatureActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_example_parse_activity)

        findViewById<Button>(R.id.crash_button).setOnClickListener {
            val signatureBitmap = ContextCompat.getDrawable(
                this@ResultSignatureActivity,
                R.drawable.mock_page
            )?.toBitmap()!!

            val (width, height) = signatureBitmap.width to signatureBitmap.height
            val signature = Signature.createStampSignature(
                signatureBitmap,
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                null,
                1f
            )
            setResult(RESULT_OK, Intent().putExtra(RESULT_EXTRA_SIGNATURE, signature))
            finish()
        }
    }
}