package mobile.models.results

import mobile.models.MobileConfiguration

interface GetResult {
    data class Success(val mobileConfiguration: MobileConfiguration): GetResult
    data object Forbidden: GetResult
    data object NotFound: GetResult
}