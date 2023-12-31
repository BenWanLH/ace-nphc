package com.sg.gov.ace.nphc.model

import com.sg.gov.ace.nphc.database.model.User

interface Response {
    val status: Int
    val message: String?
}

data class GetUserResponse(
        override val status: Int,
        override val message: String?,
        val user: User? = null,
): Response

data class GetAllUsersResponse(
        override val status: Int,
        override val message: String,
        val users: List<User>? = listOf()
): Response

data class GenericResponse(
        override val status: Int,
        override val message: String?,
): Response


