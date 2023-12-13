package com.sg.gov.ace.nphc.database.repository

import com.sg.gov.ace.nphc.database.model.User

interface CustomUserRepository {
    fun findAllUsersWithFilter(
        minSalary: Double,
        maxSalary: Double,
        limit: Int,
        offset: Int,
        name: String? = "",
        login: String? = ""
    ) : List<User>

}