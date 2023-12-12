package com.sg.gov.ace.nphc.database.repository

import com.sg.gov.ace.nphc.database.model.User
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

class CustomUserRepositoryImpl : CustomUserRepository {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    override fun findAllUsersWithFilter(minSalary: Double, maxSalary: Double, limit: Int, offset: Int) : List<User> {

        val query = entityManager.createQuery("FROM User u WHERE u.salary >= :minSalary " +
            "AND u.salary < :maxSalary",
            User::class.java)
            .setParameter("minSalary", minSalary)
            .setParameter("maxSalary", maxSalary)

        if(limit > 0) query.setMaxResults(limit)

        if(offset > 0) query.setFirstResult(offset)

        return query.resultList

    }

}