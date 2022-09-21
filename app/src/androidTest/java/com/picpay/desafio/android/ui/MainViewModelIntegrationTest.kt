package com.picpay.desafio.android.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.whenever
import com.picpay.desafio.android.MainActivityTest
import com.picpay.desafio.android.data.model.User
import com.picpay.desafio.android.data.remote.UserService
import com.picpay.desafio.android.db.UserDAO
import com.picpay.desafio.android.db.UserDatabase
import com.picpay.desafio.android.repository.UserRepository
import com.picpay.desafio.android.repository.UserRepositoryImpl
import com.picpay.desafio.android.util.constants.Constants
import com.picpay.desafio.android.utils.androidTestContants
import com.picpay.desafio.android.utils.androidTestContants.errorResponse
import com.picpay.desafio.android.utils.androidTestContants.serverPort
import com.picpay.desafio.android.utils.androidTestContants.successResponse
import com.picpay.desafio.android.utils.androidTestContants.userReponseData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.declareMock
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

@MediumTest
@RunWith(AndroidJUnit4::class)
class MainViewModelIntegrationTest : KoinTest {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockWebServer = MockWebServer()


    private val server = MockWebServer()
    private lateinit var userDatabase: UserDatabase
    private lateinit var userDao: UserDAO
    private lateinit var viewModel: MainViewModel
    private lateinit var api: UserService

    @Before
    fun setup() {
        userDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            UserDatabase::class.java
        ).allowMainThreadQueries().build()

        userDao = userDatabase.getUserDao()

        api = Retrofit.Builder()
            .baseUrl(Constants.URL)
            .client(
                OkHttpClient.Builder()
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserService::class.java)


        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/user" -> successResponse
                    else -> errorResponse
                }

            }
        }

        viewModel = MainViewModel(UserRepositoryImpl(api, userDao))
    }

    @After
    fun closeDb(){
        userDatabase.close()
    }

    @Test
    fun getUsersSaveItemsIntoDbOnSuccess() = runBlocking {
        server.start(serverPort)

            viewModel.insertContactListIntoDb(listOf(userReponseData)).apply {
                assertThat(userDao.getContacts()).isNotEmpty()
            }

        server.close()
    }

    @Test
    fun getUsersSaveItemsIntoDbOnSuccessWithNetwork() = runBlocking {

        server.start(serverPort).apply {
            viewModel.getUsers().apply {
                assertThat(userDao.getContacts()).isNotEmpty()
            }
        }

        server.close()
    }
}