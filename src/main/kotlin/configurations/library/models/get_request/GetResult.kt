package configurations.library.models.get_request

import configurations.library.models.ConfigurationFullResponse

sealed interface GetResult {
    data class Success(val configurationFullResponse: ConfigurationFullResponse) : GetResult
    data object Forbidden : GetResult
    data object NotFound : GetResult
}