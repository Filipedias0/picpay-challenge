package com.picpay.desafio.android.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.picpay.desafio.android.data.model.User
import retrofit2.http.GET

@Dao
interface UserDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactList(user: List<User>)

    @Query("SELECT * FROM user_table")
    suspend fun getContacts(): List<User>

    @Delete()
    suspend fun deleteUser(user: User)


}