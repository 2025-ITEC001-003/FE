package com.example.jesafe.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val REGISTER = "register"
    const val CHANGE_NAME = "change_name"
    const val CHANGE_PHONE_NUMBER = "change_phone_number"
    const val CHANGE_EMAIL = "change_email"
    const val UNDERLYING_DISEASE = "underlying_disease"
    const val CHANGE_PASSWORD = "change_password"
    const val NOTIFICATION_SETTINGS = "notification_settings"

    // 파라미터가 있는 경로
    const val ADDITIONAL_INFO = "additional_info/{name}"
    const val REGISTRATION_COMPLETE = "registration_complete/{name}"

    // 추가 정보 입력 단계별 경로
    const val ADDITIONAL_INFO_CONTACT = "additional_info_contact"
    const val ADDITIONAL_INFO_DISEASE = "additional_info_disease"

    fun additionalInfo(name: String) = "additional_info/$name"
    fun registrationComplete(name: String) = "registration_complete/$name"
}
