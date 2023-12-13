package com.sg.gov.ace.nphc.database.repository

import com.sg.gov.ace.nphc.database.model.User
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

class CustomUserRepositoryImpl : CustomUserRepository {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    override fun findAllUsersWithFilter(
        minSalary: Double,
        maxSalary: Double,
        limit: Int,
        offset: Int,
        name: String?,
        login: String?,
    ) : List<User> {
        var query = "FROM User u WHERE u.salary >= :minSalary AND u.salary < :maxSalary"

        if(!name.isNullOrBlank()) query += " AND lower(u.name) LIKE lower(:name)"

        if(!login.isNullOrBlank()) query += " AND lower(u.login) LIKE lower(:login)"

        val typedQuery = entityManager.createQuery(query, User::class.java)
            .setParameter("minSalary", minSalary)
            .setParameter("maxSalary", maxSalary)

        if(!name.isNullOrBlank()) typedQuery.setParameter("name", "%$name%")

        if(!login.isNullOrBlank()) typedQuery.setParameter("login", "%$login%")

        if(limit > 0) typedQuery.setMaxResults(limit)

        if(offset > 0) typedQuery.setFirstResult(offset)

        return typedQuery.resultList
    }

}