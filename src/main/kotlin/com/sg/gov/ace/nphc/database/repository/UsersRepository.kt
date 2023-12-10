package com.sg.gov.ace.nphc.database.repository

import com.sg.gov.ace.nphc.database.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UsersRepository : JpaRepository<User, String>, CustomUserRepository {

    fun findUserByLogin(login: String): User?

}