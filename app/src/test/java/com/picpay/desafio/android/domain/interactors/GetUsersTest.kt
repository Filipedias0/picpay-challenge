package com.picpay.desafio.android.domain.interactors

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.picpay.desafio.android.data.remote.UserMock.listOfMockedUserDTO
import com.picpay.desafio.android.data.remote.UserMock.listOfMockedUserModel
import com.picpay.desafio.android.utils.MainCoroutineRule
import com.picpay.desafio.android.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetUsersTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var repository: UserRepository
    private lateinit var getUsers: GetUsers

    @Before
    fun setUp() {
        repository = mock()
        getUsers = GetUsers(repository)
    }

    @Test
    fun getUsersReturnListOnSuccess() = runBlocking{
        whenever(repository.getUsersFromRemote()).thenReturn(Result.success(listOfMockedUserModel))
        val response = getUsers().getOrNull()
        assertThat(response).isEqualTo(listOfMockedUserModel)
    }

    @Test
    fun getUsersGetListFromDb() = runBlocking{
        whenever(repository.getUsersFromRemote()).thenReturn(Result.failure(Throwable("")))
        whenever(repository.getContactListFromDb()).thenReturn(listOfMockedUserDTO)

        val response = getUsers().getOrNull()

        assertThat(response).isEqualTo(listOfMockedUserModel)
    }

    @Test
    fun getUser() = runBlocking{
        whenever(repository.getUsersFromRemote()).thenReturn(Result.success(listOfMockedUserModel))
        getUsers()
        verify(repository).insertContactListIntoDb(any())
    }

}