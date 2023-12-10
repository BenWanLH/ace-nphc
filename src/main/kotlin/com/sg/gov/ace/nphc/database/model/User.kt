package com.sg.gov.ace.nphc.database.model

import com.sg.gov.ace.nphc.util.Constant
import com.sg.gov.ace.nphc.util.Utils
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import com.sg.gov.ace.nphc.database.model.User.Companion.VALIDATIONSTATUS.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity
@Table (name = "Users")
data class User (
    @Id
    @NotBlank
    @Pattern(regexp = Constant.alphaNumericRegex)
    val id: String,
    @NotBlank
    @Pattern(regexp = Constant.alphaNumericRegex)
    @Column(unique = true)
    var login: String,
    @NotBlank
    var name: String,
    var salary: Double,
    @NotBlank
    var startDate: String
){
    constructor() : this(Utils.generateUUID(), "", "", 0.0, "")

    constructor(
        login: String,
        name: String,
        salary: Double,
        startDate: String
    ) : this(Utils.generateUUID(), login, name, salary, startDate)

    fun validate(): VALIDATIONSTATUS {
        if(!id.matches(Regex(Constant.alphaNumericRegex)))
            return INVALIDID

        if(!login.matches(Regex(Constant.alphaNumericRegex)))
            return INVALIDLOGIN

        if(!startDate.matches(Regex(Constant.primaryDateRegex)) &&
            !startDate.matches(Regex(Constant.secondaryDateRegex)))
            return INVALIDDATE

        if(salary < 0)
            return INVALIDSALARY

        return VALID
    }

    fun parseDate(){
        try {
            if(startDate.matches(Regex(Constant.secondaryDateRegex))) {
                val currentDateTimeFormat = DateTimeFormatter.ofPattern("dd-MMM-yy")
                val desiredDatetimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                startDate = LocalDate.parse(startDate, currentDateTimeFormat).format(desiredDatetimeFormat)
            }
        }catch (exception: Exception) {
            throw exception
        }
    }

    companion object {
        enum class VALIDATIONSTATUS(val message: String) {
            INVALIDID("Invalid Id"),
            INVALIDLOGIN("Invalid Login"),
            INVALIDSALARY("Invalid Salary"),
            INVALIDDATE("Invalid Date"),
            VALID("Success")
        }
    }

}