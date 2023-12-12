package com.sg.gov.ace.nphc.database.model

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import com.sg.gov.ace.nphc.database.model.User.Companion.VALIDATIONSTATUS.*
import org.junit.jupiter.api.assertThrows

@SpringBootTest
class UserTests {

    @Test
    fun `validate - validates successfully`() {
        val user = User("abc", "test","test", 4000.0, "2002-02-01")

        assert(user.validate() == VALID)
    }

    @Test
    fun `validate - invalid id`() {
        val user = User("abc-123", "test","test", 4000.0, "2002-02-01")

        assert(user.validate() == INVALIDID)
    }

    @Test
    fun `validate - invalid login`() {
        val user = User("abc", "test-123","test", 4000.0, "2002-02-01")

        assert(user.validate() == INVALIDLOGIN)
    }

    @Test
    fun `validate - invalid date`() {
        val user = User("abc", "test","test", 4000.0, "11/06/2022")

        assert(user.validate() == INVALIDDATE)
    }

    @Test
    fun `validate - invalid date with exception`() {
        val user = User("abc", "test","test", 4000.0, "11/13/2022")

        assert(user.validate() == INVALIDDATE)
    }

    @Test
    fun `validate - invalid salary`() {
        val user = User("abc", "test","test", -100.0, "2002-02-01")

        assert(user.validate() == INVALIDSALARY)
    }

    @Test
    fun `parseDate - parses Date successfully`() {
        val user = User("abc", "test","test", -100.0, "01-Jan-02")

        user.parseDate()
        assert(user.startDate == "2002-01-01")
    }

    @Test
    fun `parseDate - does not parseDate`() {
        val user = User("abc", "test","test", -100.0, "01/02/2002")

        user.parseDate()

        assert(user.startDate == "01/02/2002")
    }
}