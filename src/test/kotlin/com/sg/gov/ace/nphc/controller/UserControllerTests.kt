package com.sg.gov.ace.nphc.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.sg.gov.ace.nphc.database.model.User
import com.sg.gov.ace.nphc.database.repository.UsersRepository
import com.sg.gov.ace.nphc.model.UserData
import com.sg.gov.ace.nphc.util.Constant
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTests(
    @Autowired val mockMvc: MockMvc,
) {

    @MockBean
    lateinit var userRepository: UsersRepository

    val objectMapper = ObjectMapper()

    val testUser = User("testid01", "test", "test", 400.0, "2002-01-01")
    val testUserData = UserData("test", "test1", 400.0, "2002-01-01")

    @Test
    fun `getUser - successfully get user`(){
        val testId = "1"
        `when`(userRepository.findById(testId)).thenReturn(Optional.of(testUser))

        mockMvc.perform(get("${Constant.apiPrefix}/users/$testId"))
            .andExpect(status().isOk)
            .andExpect(content().string("{\"status\":200," +
                "\"message\":\"Success\"," +
                "\"user\":" +
                "{\"id\":\"testid01\"," +
                "\"login\":\"test\"," +
                "\"name\":\"test\"," +
                "\"salary\":400.0," +
                "\"startDate\":\"2002-01-01\"" +
                "}}"
            ))
    }

    @Test
    fun `getUser - user not found`(){
        val testId = "1"
        `when`(userRepository.findById(testId)).thenReturn(Optional.empty())

        mockMvc.perform(get("${Constant.apiPrefix}/users/$testId"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `geAllUsers - successfully get users`(){
        `when`(userRepository.findAllUsersWithFilter(0.0, 4000.00, 0, 0)).thenReturn(listOf())

        mockMvc.perform(get("${Constant.apiPrefix}/users"))
            .andExpect(status().isOk)
            .andExpect(content().string("{\"status\":200,\"message\":\"Success\",\"users\":[]}"))

    }

    @Test
    fun `geAllUsers - bad input`(){
        `when`(userRepository.findAllUsersWithFilter(0.0, 4000.00, 0, 0)).thenReturn(listOf())

        mockMvc.perform(get("${Constant.apiPrefix}/users?minSalary=a"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `addUser - successfully add user`(){
        `when`(userRepository.findById(testUser.id)).thenReturn(Optional.empty())
        `when`(userRepository.findUserByLogin(testUser.login)).thenReturn(null)

        val contentAsString = objectMapper.writeValueAsString(testUser)
        mockMvc.perform(post("${Constant.apiPrefix}/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(contentAsString)
        )
        .andExpect(status().isCreated)
    }

    @Test
    fun `addUser - user param invalid`(){
        val userWithInvalidId = User("testid-01", "test", "test", 400.0, "2002-01-01")

        val contentAsString = objectMapper.writeValueAsString(userWithInvalidId)
        mockMvc.perform(post("${Constant.apiPrefix}/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(contentAsString)
        )
        .andExpect(status().isBadRequest)
        .andExpect(content().string("{\"status\":400,\"message\":\"Invalid Id\"}"))
    }

    @Test
    fun `addUser - Employee ID already exists`(){
        val userWithExistingId = User("testid01", "test", "test", 400.0, "2002-01-01")
        `when`(userRepository.findById(testUser.id)).thenReturn(Optional.of(userWithExistingId))

        val contentAsString = objectMapper.writeValueAsString(userWithExistingId)
        mockMvc.perform(post("${Constant.apiPrefix}/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(contentAsString)
        )
        .andExpect(status().isBadRequest)
        .andExpect(content().string("{\"status\":400,\"message\":\"Employee ID already exists\"}"))
    }

    @Test
    fun `addUser - Employee login not unique`(){
        val userWithExistingLogin = User("testid01", "test", "test", 400.0, "2002-01-01")
        `when`(userRepository.findById(testUser.id)).thenReturn(Optional.empty())
        `when`(userRepository.findUserByLogin(testUser.login)).thenReturn(userWithExistingLogin)

        val contentAsString = objectMapper.writeValueAsString(userWithExistingLogin)
        mockMvc.perform(post("${Constant.apiPrefix}/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(contentAsString)
        )
        .andExpect(status().isBadRequest)
        .andExpect(content().string("{\"status\":400,\"message\":\"Employee login not unique\"}"))
    }

    @Test
    fun `updateUser - successfully update user`(){
        `when`(userRepository.findById(testUser.id)).thenReturn(Optional.of(testUser))
        `when`(userRepository.findUserByLogin(testUser.login)).thenReturn(null)

        val contentAsString = objectMapper.writeValueAsString(testUserData)
        mockMvc.perform(put("${Constant.apiPrefix}/users/${testUser.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(contentAsString)
        )
        .andExpect(status().isOk)
    }

    @Test
    fun `updateUser - No such employee`(){
        `when`(userRepository.findById(testUser.id)).thenReturn(Optional.empty())
        `when`(userRepository.findUserByLogin(testUser.login)).thenReturn(null)

        val contentAsString = objectMapper.writeValueAsString(testUserData)
        mockMvc.perform(put("${Constant.apiPrefix}/users/${testUser.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(contentAsString)
        )
        .andExpect(status().isBadRequest)
        .andExpect(content().string("{\"status\":400,\"message\":\"Bad input - No such employee\"}"))
    }

    @Test
    fun `updateUser - Employee login not unique`(){
        val userWithSameLogin = User("testid02", "test", "test", 400.0, "2002-01-01")
        val userDataWithSameLogin = UserData("test", "test", 400.0, "2002-01-01")

        `when`(userRepository.findById(testUser.id)).thenReturn(Optional.of(testUser))
        `when`(userRepository.findUserByLogin(testUser.login)).thenReturn(userWithSameLogin)

        val contentAsString = objectMapper.writeValueAsString(userDataWithSameLogin)
        mockMvc.perform(put("${Constant.apiPrefix}/users/${testUser.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentAsString)
        )
        .andExpect(status().isBadRequest)
        .andExpect(content().string("{\"status\":400,\"message\":\"Bad input -Employee login not unique\"}"))
    }

    @Test
    fun `updateUser - user param invalid`(){
        val userWithInvalidParam = User("testid02", "test", "test", 400.0, "20/01/2002")
        val userDataWithInvalidParam = UserData("test", "test", 400.0, "20/01/2002")

        `when`(userRepository.findById(userWithInvalidParam.id)).thenReturn(Optional.of(userWithInvalidParam))
        `when`(userRepository.findUserByLogin(testUser.login)).thenReturn(null)

        val contentAsString = objectMapper.writeValueAsString(userDataWithInvalidParam)
        mockMvc.perform(put("${Constant.apiPrefix}/users/${userWithInvalidParam.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentAsString)
        )
        .andExpect(status().isBadRequest)
        .andExpect(content().string("{\"status\":400,\"message\":\"Bad input - Invalid Date\"}"))
    }

    @Test
    fun `uploadUsers - uploads successfully`() {
        val byteArr = Files.readAllBytes(Paths.get("./src/test/resources/test.csv"));
        val mockMultiPartFile = MockMultipartFile("file", "test.csv", "text/plain", byteArr)

        mockMvc.perform(multipart("${Constant.apiPrefix}/users/upload").file(mockMultiPartFile))
                .andExpect(status().isOk())
    }

    @Test
    fun `uploadUsers - non csv file`() {
        val byteArr = Files.readAllBytes(Paths.get("./src/test/resources/mock.txt"));
        val mockMultiPartFile = MockMultipartFile("file", "mock.txt", "text/plain", byteArr)

        mockMvc.perform(multipart("${Constant.apiPrefix}/users/upload").file(mockMultiPartFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"status\":400,\"message\":\"Only csv file accepted\"}"))
    }

    @Test
    fun `uploadUsers - csv with non unique id`() {
        val byteArr = Files.readAllBytes(Paths.get("./src/test/resources/non-unique-id.csv"));
        val mockMultiPartFile = MockMultipartFile("file", "non-unique-id.csv", "text/plain", byteArr)

        mockMvc.perform(multipart("${Constant.apiPrefix}/users/upload").file(mockMultiPartFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"status\":400,\"message\":\"Employee IDs in csv file are not unique\"}"))
    }

    @Test
    fun `uploadUsers - only 4 column filled`() {
        val byteArr = Files.readAllBytes(Paths.get("./src/test/resources/4-column.csv"));
        val mockMultiPartFile = MockMultipartFile("file", "4-column.csv", "text/plain", byteArr)

        mockMvc.perform(multipart("${Constant.apiPrefix}/users/upload").file(mockMultiPartFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"status\":400,\"message\":\"Not all columns are filled\"}"))
    }

}