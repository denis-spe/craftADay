// The LORD is my strength, he revealed to us through his son JESUS CHRIST
package com.den.craftaday.helper

import androidx.compose.runtime.MutableState

fun validateName(
    firstNameState: CharSequence,
    lastNameState: CharSequence,
    firstNameMessage: MutableState<String>,
    lastNameMessage: MutableState<String>,
): Boolean {
    val nameText = "$firstNameState $lastNameState"
        .split(" ")
    val firstName = nameText[0]
    val lastName = nameText.getOrNull(1)

    return if (firstName.length < 2) {
        firstNameMessage.value = "First name must be at least 2 characters long"
        false
    }
    else if (lastName != null && lastName.length < 2) {
        if (firstNameMessage.value.isEmpty() && lastName.isEmpty()) {
            true
        } else {
            lastNameMessage.value = "Last name must be at least 2 characters long"
            false
        }
    }
    else {
        firstNameMessage.value = ""
        lastNameMessage.value = ""
        true
    }
}

fun validatorEmail(
    emailState: CharSequence,
    emailMessage: MutableState<String>
): Boolean {
    val validEmailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

    return if (emailState.isEmpty()) {
        emailMessage.value = "Email cannot be empty"
        false
    }
    else {
        if (validEmailRegex.matches(emailState)) {
            emailMessage.value = ""
            true
        } else {
            emailMessage.value = "Please enter a valid email address"
            false
        }
    }
}

fun validatorPassword(
    passwordState: CharSequence,
    passwordMessage: MutableState<String>
): Boolean {
    return if (passwordState.isEmpty()) {
        passwordMessage.value = "Password cannot be empty"
        false
    } else {
        if (passwordState.length < 8) {
            passwordMessage.value = "Password must be at least 8 characters long"
            false
        } else {
            passwordMessage.value = ""
            true
        }
    }
}

fun validatorPasswordWithConfirmation(
    passwordState: CharSequence,
    confirmPasswordState: CharSequence,
    passwordMessage: MutableState<String>,
    confirmPasswordMessage: MutableState<String>,
): Boolean {
    return if (passwordState.isEmpty()) {
        passwordMessage.value = "Password cannot be empty"
        false
    } else if (confirmPasswordState.isEmpty()) {
        confirmPasswordMessage.value = "Confirm password cannot be empty"
        false
    } else {
        if (passwordState.length < 8) {
            passwordMessage.value = "Password must be at least 8 characters long"
            false
        } else if (passwordState != confirmPasswordState) {
            passwordMessage.value = ""
            confirmPasswordMessage.let {
                it.value = "Passwords do not match"
            }
            false
        } else {
            passwordMessage.value = ""
            confirmPasswordMessage.value = ""
            true
        }
    }
}