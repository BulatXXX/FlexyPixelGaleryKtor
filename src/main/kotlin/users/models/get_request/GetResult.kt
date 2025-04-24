package users.models.get_request

sealed interface GetResult {
    data class SuccessFull(val userResponse: UserResponse) : GetResult
    data class SuccessPublic(val userResponse: PublicUserResponse) : GetResult
    data object NotFound : GetResult
    data object DataBaseError : GetResult
}