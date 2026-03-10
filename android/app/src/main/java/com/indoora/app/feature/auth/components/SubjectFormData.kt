package com.indoora.app.feature.auth.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.indoora.app.data.model.UserCreate

class SubjectFormData {
    var username by mutableStateOf("")
    var name by mutableStateOf("")
    var surnames by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    fun isValid() = username.isNotBlank()
            && name.isNotBlank()
            && surnames.isNotBlank()
            && email.isNotBlank()
            && password.length >= 6

    fun toUserCreate() = UserCreate(
        username = username.trim(),
        name = name.trim(),
        surnames = surnames.trim(),
        email = email.trim(),
        password = password,
        userType = "SUBJECT",
        homeName = null,
        subjectUsername = null
    )
}