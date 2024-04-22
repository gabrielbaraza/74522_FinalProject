package com.stu74522.finalproject_stu74522

sealed class Screen(val route: String) {
    object SignUp : Screen("sign_up")
    object Login : Screen("login")
    object Screen0 : Screen("screen0")
    object MainScreen : Screen("mainscreen")
    object CartScreen : Screen("cartscreen")
    object SuccessfulTransactionScreen : Screen("successfultransactionscreen")
    object UserInformationScreen : Screen("userinformationscreen")
    object UserHistoricScreen : Screen("userhistoricscreen")
    object AboutScreen : Screen("AboutScreen")

}