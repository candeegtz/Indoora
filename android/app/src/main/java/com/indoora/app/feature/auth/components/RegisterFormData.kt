package com.indoora.app.feature.auth.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.indoora.app.data.model.UserCreate

class RegisterFormData {
    var username by mutableStateOf("")
    var name by mutableStateOf("")
    var surnames by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var homeName by mutableStateOf("")
    var subjectUsername by mutableStateOf("")

    fun isBasicFormValid() = username.isNotBlank()
            && name.isNotBlank()
            && surnames.isNotBlank()
            && email.isNotBlank()
            && password.length >= 6

    fun toUserCreate(isSupervisorCreator: Boolean) = UserCreate(
        username = username.trim(),
        name = name.trim(),
        surnames = surnames.trim(),
        email = email.trim(),
        password = password,
        userType = if (isSupervisorCreator) "SUPERVISOR_CREATOR" else "SUPERVISOR",
        homeName = if (isSupervisorCreator) homeName.trim() else null,
        subjectUsername = if (!isSupervisorCreator) subjectUsername.trim() else null
    )
}

enum class RegisterStep {
    CHOOSE_TYPE,
    FILL_FORM,
    HOME_SETUP
}
