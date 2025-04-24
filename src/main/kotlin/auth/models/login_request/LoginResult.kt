package auth.models.login_request

sealed interface LoginResult {
    data class Success(val loginResponse: LoginResponse) : LoginResult
    data object NotFound : LoginResult
    data object IncorrectPassword : LoginResult
}