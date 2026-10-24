package com.tanay.accessmap

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

data class AccessibilityAnalysis(
    val issue: String,
    val confidence: Double,
    val severity: String,
    val description: String
)

object AIAnalyzer {

   
    private const val SERVER_URL = "http://x.x.x.x:8000/predict"

    private val client = OkHttpClient()

    suspend fun analyzeImage(
        context: Context,
        uri: Uri
    ): AccessibilityAnalysis = withContext(Dispatchers.IO) {

        // Copy the selected image into a temporary file
        val inputStream =
            context.contentResolver.openInputStream(uri)
                ?: throw Exception("Could not open selected image")

        val tempFile = File.createTempFile(
            "accessmap_image",
            ".jpg",
            context.cacheDir
        )

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Create multipart image body
        val requestBody = tempFile
            .asRequestBody("image/jpeg".toMediaType())

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                tempFile.name,
                requestBody
            )
            .build()

        // Create HTTP request
        val request = Request.Builder()
            .url(SERVER_URL)
            .post(multipartBody)
            .build()

        // Send request
        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw Exception(
                    "Server error: HTTP ${response.code}"
                )
            }

            val responseBody =
                response.body?.string()
                    ?: throw Exception("Empty response from server")

            val json = JSONObject(responseBody)

            if (json.has("error")) {
                throw Exception(
                    json.getString("error")
                )
            }

            val prediction = json.getString(
                "prediction"
            )

            val confidence = json.getDouble(
                "confidence"
            )

           
            val severity: String
            val description: String

            when (prediction) {

                "CurbRamp" -> {
                    severity = "Moderate"
                    description =
                        "A curb ramp or ramp-related accessibility feature was detected."
                }

                "Crosswalk" -> {
                    severity = "Moderate"
                    description =
                        "crosswalk or pedestrian crossing was detected."
                }

                "Obstacle" -> {
                    severity = "High"
                    description =
                        "An obstacle affecting sidewalk accessibility was detected."
                }

                "SurfaceProblem" -> {
                    severity = "Moderate"
                    description =
                        "A sidewalk surface problem was detected."
                }

                else -> {
                    severity = "Unknown"
                    description =
                        "The AI detected an accessibility-related feature."
                }
            }

            AccessibilityAnalysis(
                issue = prediction,
                confidence = confidence,
                severity = severity,
                description = description
            )
        }
    }
}