package com.sg.gov.ace.nphc.database.repository

import com.sg.gov.ace.nphc.database.model.User
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import jakarta.persistence.TypedQuery
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CustomUserRepositoryImplTests {

    @Mock
    lateinit var entityManager: EntityManager

    @Spy
    lateinit var query: TypedQuery<User>

    @InjectMocks
    var customUserRepositoryImpl = CustomUserRepositoryImpl()

    @Test
    fun `findAllUsersWithFilter - query sets max results`() {
        `when`(entityManager.createQuery<User>(anyString(), any())).thenReturn(query)
        `when`(query.setParameter(anyString(), anyDouble())).thenReturn(query)
        `when`(query.resultList).thenReturn(listOf<User>())

        customUserRepositoryImpl.findAllUsersWithFilter(4000.0, 5000.0, 1, 0)

        verify(query).setMaxResults(1)
    }

    @Test
    fun `findAllUsersWithFilter - query sets offset`() {
        `when`(entityManager.createQuery<User>(anyString(), any())).thenReturn(query)
        `when`(query.setParameter(anyString(), anyDouble())).thenReturn(query)
        `when`(query.resultList).thenReturn(listOf<User>())

        customUserRepositoryImpl.findAllUsersWithFilter(4000.0, 5000.0, 0, 1)

        verify(query).setFirstResult(1)
    }

}