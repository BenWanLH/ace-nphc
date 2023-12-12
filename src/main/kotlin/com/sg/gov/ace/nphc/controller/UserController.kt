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
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.io.InputStream

@RestController
class UserController @Autowired constructor(private val usersRepository: UsersRepository) {

    val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("${Constant.apiPrefix}/users/upload")
    fun uploadUsers(@RequestPart("file") file: MultipartFile): ResponseEntity<GenericResponse> {
        val processedFile = File(file.originalFilename);

        if (processedFile.extension != "csv") {
            throw BadRequestException("Only csv file accepted");
        }
        val inputStream: InputStream = file.inputStream;

        val reader = inputStream.bufferedReader()

        reader.readLine()
        val users =  reader.lineSequence()
                .filter { it.isNotBlank() }
                .mapNotNull {
                    val userData = it.split(',', ignoreCase = false, limit = 5)

                    if(userData.size < 5) throw BadRequestException("Not all columns are filled")

                    if (userData[0].startsWith("#")) return@mapNotNull null

                    val (id, login, name, salary, startDate) = userData

                    val user = User(id, login, name, salary.toDouble(), startDate)

                    user.parseDate()

                    log.info("processing ... [${user.id}]")

                    val validationStatus = user.validate()

                    if(validationStatus != VALIDATIONSTATUS.VALID) throw BadRequestException(validationStatus.message)

                    val existingUserWithSameLogin = usersRepository.findUserByLogin(user.login)

                    if(existingUserWithSameLogin !== null && existingUserWithSameLogin.id != user.id) throw BadRequestException("Employee login not unique")

                    user

                }.toList()

        val distinctUserIds = users.map { it.id }.distinct()

        if(distinctUserIds.size !== users.size) throw BadRequestException("Employee IDs in csv file are not unique")

        val distinctUserLogins = users.map { it.login }.distinct()

        if(distinctUserLogins.size !== users.size) throw BadRequestException("Employee logins in csv are not unique")

        usersRepository.saveAll(users)

        return ResponseEntity
            .ok()
            .body(GenericResponse(200, "Successfully uploaded users"))

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
            var minSalaryAsDouble: Double
            var maxSalaryAsDouble: Double
            var offsetAsInt: Int
            var limitAsInt: Int

            try {
                minSalaryAsDouble = minSalary.toDouble()
                maxSalaryAsDouble = maxSalary.toDouble()
                offsetAsInt = offset.toInt()
                limitAsInt = limit.toInt()
            } catch (exception: Exception) {
                throw BadRequestException("Bad Input")
            }

            val result = usersRepository.findAllUsersWithFilter(minSalaryAsDouble, maxSalaryAsDouble, limitAsInt, offsetAsInt)

            return ResponseEntity
                .ok()
                .body(GetAllUsersResponse(200, "Success", result))

        }catch (exception: Exception) {
            log.error("Error retrieving users ... [$exception]")

            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad input")
        }
    }

    @PostMapping("${Constant.apiPrefix}/users")
    fun addUser(@RequestBody user: User): ResponseEntity<GenericResponse> {
        log.info("adding user ...")

        user.parseDate()

        val validationStatus = user.validate()

        if(validationStatus != VALIDATIONSTATUS.VALID) throw BadRequestException(validationStatus.message)

        if(!usersRepository.findById(user.id).isEmpty) throw BadRequestException("Employee ID already exists")

        if(usersRepository.findUserByLogin(user.login) !== null) throw BadRequestException("Employee login not unique")

        log.info("saving user ...")

        usersRepository.save(user)

        log.info("added user")

        return ResponseEntity(GenericResponse(201, "Success"), HttpStatus.CREATED)
    }

    @PutMapping("${Constant.apiPrefix}/users/{id}")
    fun updateUser(@PathVariable id: String, @RequestBody userData: UserData): ResponseEntity<GenericResponse> {
        log.info("Updating user ...")

        if(usersRepository.findById(id).isEmpty) throw BadRequestException("Bad input - No such employee")

        val searchByLoginResult = usersRepository.findUserByLogin(userData.login)

        if(searchByLoginResult != null && searchByLoginResult.id != id) throw BadRequestException("Bad input -Employee login not unique")

        val user = User(id, userData.login, userData.name, userData.salary, userData.startDate)

        user.parseDate()

        val validationStatus = user.validate()

        if(validationStatus != VALIDATIONSTATUS.VALID) throw BadRequestException("Bad input - ${validationStatus.message}")

        log.info("Saving user ...")

        usersRepository.save(user)

        return ResponseEntity.ok().body(GenericResponse(200, "Successfully updated"))
    }

    @DeleteMapping("${Constant.apiPrefix}/users/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<GenericResponse> {
        val user = usersRepository.findById(id)

        if(user.isEmpty) throw BadRequestException("No such employee")

        usersRepository.delete(user.get())

        return ResponseEntity.ok().body(GenericResponse(200, "Successfully deleted"))
    }

    @ExceptionHandler(BadRequestException::class)
    fun badRequestExceptionHandler(exception: Exception): ResponseEntity<GenericResponse>  {
        log.error("Api Failed ... [${exception.message}]")
        return ResponseEntity.badRequest().body(GenericResponse(400, exception.message))
    }

}