package com.sg.gov.ace.nphc.controller

import com.sg.gov.ace.nphc.database.model.User
import com.sg.gov.ace.nphc.model.*
import com.sg.gov.ace.nphc.database.model.User.Companion.VALIDATIONSTATUS
import com.sg.gov.ace.nphc.database.repository.UsersRepository
import com.sg.gov.ace.nphc.util.Constant
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream

@RestController
@Validated
class UserController @Autowired constructor(private val usersRepository: UsersRepository) {

    val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("${Constant.apiPrefix}/users/upload")
    fun uploadUsers(@RequestPart("file") file: MultipartFile): ResponseEntity<UploadUserResponse> {
        val processedFile = File(file.originalFilename);

        if (processedFile.extension != "csv") {
            throw BadRequestException("Only csv file accepted");
        }
        val inputStream: InputStream = file.inputStream;

        val reader = inputStream.bufferedReader()

        reader.readLine()
        val users =  reader.lineSequence()
                .filter { it.isNotBlank() }
                .map {
                    val (id, login, name, salary, startDate) = it.split(',', ignoreCase = false, limit = 5)

                    val user = User(id, login, name, salary.toDouble(), startDate)


                    val validationStatus = user.validate()

                    if(validationStatus != VALIDATIONSTATUS.VALID) throw BadRequestException(validationStatus.message)

                    if(!usersRepository.findById(user.id).isEmpty) throw BadRequestException("Employee ID already exists")

                    if(usersRepository.findUserByLogin(user.login) !== null) throw BadRequestException("Employee login not unique")

                    user.parseDate()

                    user

                }.toList()

        usersRepository.saveAll(users)

        return ResponseEntity
                .ok()
                .body(UploadUserResponse(200, "Successfully uploaded users"))

    }

    @GetMapping("${Constant.apiPrefix}/users/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<GetUserResponse> {
        val result = usersRepository.findById(id)

        if (result.isEmpty) throw BadRequestException("User not found")

        return ResponseEntity
            .ok()
            .body(GetUserResponse(200, "Success", result.get()))
    }

    @GetMapping("${Constant.apiPrefix}/users")
    fun getAllUsers(
        @RequestParam("minSalary") minSalary: String = "0.0",
        @RequestParam("maxSalary") maxSalary: String = "4000.00",
        @RequestParam("offset") offset: String = "0",
        @RequestParam("limit") limit: String = "0"
    ): ResponseEntity<GetAllUsersResponse> {
        try {
            val minSalaryAsDouble = minSalary.toDouble()
            val maxSalaryAsDouble = maxSalary.toDouble()
            val offsetAsInt: Int = offset.toInt()
            val limitAsInt: Int = limit.toInt()

            val result = usersRepository.findAllUsersWithFilter(minSalaryAsDouble, maxSalaryAsDouble, limitAsInt, offsetAsInt)

            return ResponseEntity
                .ok()
                .body(GetAllUsersResponse(200, "Success", result))

        }catch (exception: Exception) {
            log.error("Error retrieving users ... [$exception]")

            throw BadRequestException("Bad input")
        }

    }

    @PostMapping("${Constant.apiPrefix}/users")
    fun addUser(@RequestBody user: User): ResponseEntity<AddUserResponse> {
        log.info("adding user ...")

        val validationStatus = user.validate()

        if(validationStatus != VALIDATIONSTATUS.VALID) throw BadRequestException(validationStatus.message)

        if(!usersRepository.findById(user.id).isEmpty) throw BadRequestException("Employee ID already exists")

        if(usersRepository.findUserByLogin(user.login) !== null) throw BadRequestException("Employee login not unique")

        user.parseDate()

        log.info("saving user ...")

        usersRepository.save(user)

        log.info("added user")

        return ResponseEntity(AddUserResponse(201, "Success"), HttpStatus.CREATED)
    }

    @PutMapping("${Constant.apiPrefix}/users")
    fun updateUser(@RequestBody updatedUser: User): ResponseEntity<UpdateUserResponse> {
        log.info("Updating user ...")

        if(!usersRepository.findById(updatedUser.id).isEmpty) throw BadRequestException("No such employee")

        if(usersRepository.findUserByLogin(updatedUser.login) !== null) throw BadRequestException("Employee login not unique")

        val validationStatus = updatedUser.validate()

        if(validationStatus != VALIDATIONSTATUS.VALID) throw BadRequestException(validationStatus.message)

        updatedUser.parseDate()

        log.info("Saving user ...")

        usersRepository.save(updatedUser)

        return ResponseEntity.ok().body(UpdateUserResponse(200, "Success"))
    }

}