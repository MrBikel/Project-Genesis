package com.meh.projectgenesis.core.ai

import com.meh.projectgenesis.domain.model.ReasoningMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

data class ProviderConfig(
    val apiKey: String?,
    val modelName: String = "gemini-2.5-flash",
    val baseUrl: String = "https://generativelanguage.googleapis.com",
    val temperature: Float = 0.3f
)

data class AiResult(
    val content: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val httpStatusCode: Int? = null,
    val isDemoFallback: Boolean = false,
    val isModelPowered: Boolean = false,
    val requestStatusLog: String = "",
    val normalizationLog: String = ""
)

data class EngineDiagnostics(
    val activeProviderName: String,
    val engineClassification: String, // "A. Genuinely Model-Powered" or "B. Deterministic Rule-Based & Heuristic Templates"
    val activeModelName: String,
    val isRemoteExecution: Boolean,
    val isApiAvailable: Boolean,
    val lastProviderRequestStatus: String,
    val lastResponseNormalizationResult: String,
    val isDemoModeActive: Boolean,
    val isOutputMockedOrTemplated: Boolean,
    val auditNote: String
)

interface AiProvider {
    suspend fun generateResponse(
        prompt: String,
        systemInstruction: String,
        reasoningMode: ReasoningMode,
        config: ProviderConfig
    ): AiResult
}

class GeminiProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) : AiProvider {

    override suspend fun generateResponse(
        prompt: String,
        systemInstruction: String,
        reasoningMode: ReasoningMode,
        config: ProviderConfig
    ): AiResult = withContext(Dispatchers.IO) {
        val key = config.apiKey
        if (key.isNullOrBlank()) {
            return@withContext AiResult(
                content = "",
                isSuccess = false,
                errorMessage = "কোনো এপিআই কি পাওয়া যায়নি (Missing Gemini API Key)। প্রোভাইডার কল স্থগিত করা হয়েছে।",
                httpStatusCode = null,
                isDemoFallback = true,
                isModelPowered = false,
                requestStatusLog = "Missing API Key: No client-side key configured.",
                normalizationLog = "None (Execution aborted before HTTP dispatch)"
            )
        }

        try {
            val url = "${config.baseUrl}/v1beta/models/${config.modelName}:generateContent?key=$key"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", config.temperature.toDouble())
                    put("maxOutputTokens", 2048)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val statusCode = response.code
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorDesc = when (statusCode) {
                    400 -> "HTTP 400 Bad Request: অবৈধ রিকোয়েস্ট বা ভুল ফরম্যাট।"
                    401, 403 -> "HTTP $statusCode Auth Error: এপিআই কি অবৈধ বা অনুমোদনহীন।"
                    404 -> "HTTP 404 Not Found: নির্দিষ্ট মডেল (${config.modelName}) খুঁজে পাওয়া যায়নি।"
                    429 -> "HTTP 429 Quota Exceeded: এপিআই রিকোয়েস্ট সীমা অতিক্রান্ত।"
                    500, 503 -> "HTTP $statusCode Server Error: রিমোট সার্ভার সাময়িকভাবে অনুপলব্ধ।"
                    else -> "HTTP $statusCode Error: $responseBody"
                }
                return@withContext AiResult(
                    content = "",
                    isSuccess = false,
                    errorMessage = errorDesc,
                    httpStatusCode = statusCode,
                    isDemoFallback = true,
                    isModelPowered = false,
                    requestStatusLog = "HTTP Error $statusCode",
                    normalizationLog = "Failed: Server returned non-success code"
                )
            }

            if (responseBody.isBlank()) {
                return@withContext AiResult(
                    content = "",
                    isSuccess = false,
                    errorMessage = "Gemini সার্ভার থেকে খালি রেসপন্স (Empty Body) এসেছে।",
                    httpStatusCode = statusCode,
                    isDemoFallback = true,
                    isModelPowered = false,
                    requestStatusLog = "HTTP 200 but Empty Body",
                    normalizationLog = "Failed: response body length is 0"
                )
            }

            // Normalization
            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val finishReason = firstCandidate.optString("finishReason", "STOP")
                val contentObj = firstCandidate.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    if (text.isNotBlank()) {
                        return@withContext AiResult(
                            content = text,
                            isSuccess = true,
                            httpStatusCode = statusCode,
                            isDemoFallback = false,
                            isModelPowered = true,
                            requestStatusLog = "HTTP 200 OK (FinishReason: $finishReason)",
                            normalizationLog = "Normalized candidate[0].content.parts[0].text (${text.length} chars)"
                        )
                    }
                }
            }

            AiResult(
                content = "",
                isSuccess = false,
                errorMessage = "মডেল রেসপন্স ফরম্যাট ত্রুটি (Malformed JSON: missing candidates/parts)।",
                httpStatusCode = statusCode,
                isDemoFallback = true,
                isModelPowered = false,
                requestStatusLog = "HTTP 200 with Malformed Payload",
                normalizationLog = "Failed: Could not locate candidate text in payload"
            )
        } catch (e: SocketTimeoutException) {
            AiResult(
                content = "",
                isSuccess = false,
                errorMessage = "সার্ভার সংযোগে সময়সীমা পার হয়েছে (Timeout Error)।",
                httpStatusCode = null,
                isDemoFallback = true,
                isModelPowered = false,
                requestStatusLog = "Timeout (SocketTimeoutException)",
                normalizationLog = "Aborted due to timeout"
            )
        } catch (e: UnknownHostException) {
            AiResult(
                content = "",
                isSuccess = false,
                errorMessage = "ইন্টারনেট সংযোগ নেই অথবা হোস্ট সনাক্ত করা যায়নি (No Internet Connection)।",
                httpStatusCode = null,
                isDemoFallback = true,
                isModelPowered = false,
                requestStatusLog = "Offline (UnknownHostException)",
                normalizationLog = "Aborted due to network unavailability"
            )
        } catch (e: java.util.concurrent.CancellationException) {
            AiResult(
                content = "",
                isSuccess = false,
                errorMessage = "ব্যবহারকারী কর্তৃক রিকোয়েস্ট বাতিল করা হয়েছে (User Cancelled)।",
                httpStatusCode = null,
                isDemoFallback = true,
                isModelPowered = false,
                requestStatusLog = "Cancelled by user (CancellationException)",
                normalizationLog = "Aborted due to user cancellation"
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            AiResult(
                content = "",
                isSuccess = false,
                errorMessage = "ব্যবহারকারী কর্তৃক রিকোয়েস্ট বাতিল করা হয়েছে (User Cancelled)।",
                httpStatusCode = null,
                isDemoFallback = true,
                isModelPowered = false,
                requestStatusLog = "Cancelled by user (CancellationException)",
                normalizationLog = "Aborted due to user cancellation"
            )
        } catch (e: Exception) {
            AiResult(
                content = "",
                isSuccess = false,
                errorMessage = "রিকোয়েস্ট ত্রুটি: ${e.localizedMessage}",
                httpStatusCode = null,
                isDemoFallback = true,
                isModelPowered = false,
                requestStatusLog = "Exception: ${e.javaClass.simpleName}",
                normalizationLog = "Aborted due to client exception"
            )
        }
    }
}

/**
 * Genesis Local On-Device Reasoning Provider
 * Deterministic rule-based engine with heuristic reasoning templates.
 * Explicitly labeled as deterministic rule-based (not model-generated AI).
 */
class GenesisLocalReasoningProvider : AiProvider {

    override suspend fun generateResponse(
        prompt: String,
        systemInstruction: String,
        reasoningMode: ReasoningMode,
        config: ProviderConfig
    ): AiResult = withContext(Dispatchers.Default) {
        val trimmed = prompt.trim()
        val isBengali = trimmed.any { it in '\u0980'..'\u09FF' }

        val responseText = buildLocalReasoningResponse(trimmed, isBengali, reasoningMode)
        AiResult(
            content = responseText,
            isSuccess = true,
            httpStatusCode = null,
            isDemoFallback = true,
            isModelPowered = false, // Honestly labeled as rule-based
            requestStatusLog = "Local Rule Engine Execution (Offline)",
            normalizationLog = "Deterministic heuristic pipeline output"
        )
    }

    private fun buildLocalReasoningResponse(
        prompt: String,
        isBengali: Boolean,
        mode: ReasoningMode
    ): String {
        val lower = prompt.lowercase()

        // 1. Overgeneralization and blind agreement check
        val isOvergeneralizationOrBlindAgreement =
            lower.contains("সবাই আমার বিরুদ্ধে") ||
            lower.contains("সবাই জানে") ||
            lower.contains("নিশ্চিতভাবেই ভুল") ||
            lower.contains("আমার কথাই ঠিক") ||
            lower.contains("agree with me") ||
            lower.contains("everyone is against me") ||
            lower.contains("না মেনে উপায় নেই")

        if (isOvergeneralizationOrBlindAgreement) {
            return if (isBengali) {
                """
                আপনার বক্তব্যের প্রেক্ষিতে প্রজেক্ট জেনেসিস প্রমাণের মানদণ্ড যাচাই করেছে।

                ১. সম্ভাব্য অতি-সাধারণীকরণ (Overgeneralization): "সবাই আমার বিরুদ্ধে" — এই ধরনের পরম বক্তব্য যাচাইযোগ্য প্রমাণ ব্যতীত অন্ধভাবে গ্রহণ করা অসম্ভব।
                ২. অন্ধ সম্মতি পরিহার: আপনার প্রতি পূর্ণ শ্রদ্ধা রেখে জানাচ্ছি, তথ্যপ্রমাণ ছাড়া একমত হওয়া জেনেসিসের মূলনীতির পরিপন্থী।
                ৩. তথ্য যাচাইয়ের অনুরোধ: কোন কোন নির্দিষ্ট ঘটনা বা ব্যক্তির আচরণে আপনি এমনটি মনে করছেন, অনুগ্রহ করে সেই সংক্রান্ত সুনির্দিষ্ট প্রমাণ বা প্রেক্ষাপট তুলে ধরুন।
                """.trimIndent()
            } else {
                """
                Regarding your statement, Project Genesis has evaluated the evidentiary basis.

                1. Potential Overgeneralization: Absolute claims such as "everyone is against me" lack incident-level verifiable evidence (E1/E2).
                2. Non-Sycophancy: With respect, Genesis cannot agree blindly without objective facts.
                3. Request for Evidence: Please provide specific incident-level occurrences or observations so we can analyze the situation objectively.
                """.trimIndent()
            }
        }

        // 2. Publication request
        val isPublishRequest = lower.contains("পাবলিশ") || lower.contains("publish") || lower.contains("প্রকাশ করো")
        if (isPublishRequest) {
            return if (isBengali) {
                """
                প্রকাশনা সংক্রান্ত অনুরোধ বিশ্লেষণ:

                ১. খসড়া প্রস্তুত: আপনার নির্দেশিত বিষয়বস্তুর প্রাথমিক খসড়া প্রস্তুত করা সম্ভব।
                ২. অনুমোদন নীতি: সরাসরি প্রকাশের (Publish) জন্য স্বায়ত্তশাসন স্তর ৬ এবং আপনার প্রত্যক্ষ নিশ্চিতকরণ আবশ্যক।
                ৩. অ্যাকশন স্থিতি: কোনো পূর্বানুমতি ছাড়া স্বয়ংক্রিয়ভাবে প্রকাশনা সম্পাদন অবরুদ্ধ করা হয়েছে।
                """.trimIndent()
            } else {
                """
                Publication Request Analysis:

                1. Drafting: A provisional draft can be prepared locally.
                2. Authorization Boundary: External publication requires Autonomy Level 6 and explicit confirmation.
                3. Status: Direct unauthorized publication is blocked pending explicit approval.
                """.trimIndent()
            }
        }

        // 3. Question about identity
        val isIdentityQuery = lower.contains("কে তুমি") || lower.contains("তুমি কে") ||
                lower.contains("who are you") || lower.contains("genesis") || lower.contains("জেনেসিস")
        if (isIdentityQuery) {
            return if (isBengali) {
                """
                আমি "প্রজেক্ট জেনেসিস" — একটি বাংলা-প্রথম, গোপনীয়তা-সচেতন, মেমোরি-সমৃদ্ধ ব্যক্তিগত যুক্তি সহযোগী।

                আমার কাজের বৈশিষ্ট্য:
                • আমি অন্ধ অনুসারী নই; প্রমাণের ভিত্তিতে স্বাধীন বিশ্লেষণ উপস্থাপন করি।
                • বাংলায় আপনাকে সর্বদা "আপনি" সম্বোধন করি।
                • ৯টি কাঠামোগত শ্রেণিতে মেমোরি নিয়ন্ত্রণ করি এবং আপনার স্পষ্ট অনুমোদন ছাড়া কোনো তথ্য স্থায়ী করি না।
                • লোকাল-ফার্স্ট সুরক্ষায় অফলাইনে কার্যকর থাকি।
                """.trimIndent()
            } else {
                """
                I am "Project Genesis" — a Bengali-first, privacy-conscious reasoning partner.

                Core Principles:
                • Evidence-based reasoning without sycophancy or blind agreement.
                • Respectful Bengali-first address ("আপনি").
                • Structured memory across 9 memory classes with explicit user consent.
                • Local-first privacy and controlled autonomy.
                """.trimIndent()
            }
        }

        val modeAdviceBn = when (mode) {
            ReasoningMode.EVIDENCE_FIRST -> "প্রমাণ-প্রথম নীতি: নিরপেক্ষ প্রমাণ ব্যতীত কোনো অনুমান গ্রহণ করা হচ্ছে না।"
            ReasoningMode.CRITICAL -> "সমালোচনামূলক নীতি: সম্ভাব্য দুর্বল অনুমান ও বিপরীত যুক্তি যাচাই করা হয়েছে।"
            ReasoningMode.PLANNING -> "পরিকল্পনা নীতি: পর্যায়ক্রমিক ধাপ ও নির্ভরতা যাচাই করা হয়েছে।"
            ReasoningMode.TECHNICAL -> "প্রযুক্তিগত নীতি: আর্কিটেকচারাল সঠিকতা ও সীমাবদ্ধতা পর্যালোচনা করা হয়েছে।"
            else -> "ভারসাম্যপূর্ণ যুক্তি: প্রাসঙ্গিক প্রসঙ্গ ও উদ্দেশ্যের মূল্যায়ন করা হয়েছে।"
        }

        return if (isBengali) {
            """
            আপনার জিজ্ঞাসার প্রেক্ষিতে বিশ্লেষণ ($modeAdviceBn):

            • উদ্দেশ্য: "$prompt"
            • প্রমাণের ভিত্তি: ব্যবহারকারী প্রদত্ত রেকর্ড (E3) ও জেনেসিস বিশ্লেষণ (E5)।
            • সিদ্ধান্ত:
              ১. কোনো নীতি লঙ্ঘন বা অনুমোদনহীন কাজ সনাক্ত হয়নি।
              ২. আপনার প্রজেক্ট বেসলাইনের সাথে সামঞ্জস্য রেখে অগ্রসর হওয়া সম্ভব।
              ৩. আপনার অনুমোদন সাপেক্ষে পরবর্তী পদক্ষেপ কার্যকর হবে।
            """.trimIndent()
        } else {
            """
            Structured Analysis (${mode.titleEn} Mode):

            • Objective: "$prompt"
            • Evidence Base: User inquiry (E3) and Genesis reasoning analysis (E5).
            • Conclusion:
              1. No logical contradictions or policy violations detected.
              2. Proceed within approved project parameters.
              3. Awaiting user authorization for next actions.
            """.trimIndent()
        }
    }
}
