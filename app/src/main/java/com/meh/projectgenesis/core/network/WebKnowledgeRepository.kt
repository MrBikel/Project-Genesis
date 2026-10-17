package com.meh.projectgenesis.core.network

data class WebSourceMetadata(
    val sourceTitle: String,
    val sourceUrl: String,
    val publisher: String,
    val publicationDate: String?,
    val retrievalDate: Long,
    val claimSupported: String,
    val confidence: Float,
    val contradictionNotes: String?,
    val expiryOrRecheckDate: Long?
)

sealed class WebRetrievalState {
    object DisabledByPolicy : WebRetrievalState()
    object RequiresSecureService : WebRetrievalState()
    data class Loading(val query: String) : WebRetrievalState()
    data class Success(val records: List<WebSourceMetadata>) : WebRetrievalState()
    data class Error(val message: String) : WebRetrievalState()
}

interface WebKnowledgeRepository {
    fun getServiceStatus(): WebRetrievalState
    suspend fun retrieveKnowledge(userApprovedQuery: String): WebRetrievalState
}

class GenesisWebKnowledgeRepository : WebKnowledgeRepository {
    override fun getServiceStatus(): WebRetrievalState {
        // Honest disabled state adhering to specification: "Requires secure web retrieval service. Do not fake web results."
        return WebRetrievalState.RequiresSecureService
    }

    override suspend fun retrieveKnowledge(userApprovedQuery: String): WebRetrievalState {
        return WebRetrievalState.RequiresSecureService
    }
}
