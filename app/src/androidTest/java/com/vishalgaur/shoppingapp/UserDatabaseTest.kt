package com.vishalgaur.shoppingapp

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.local.UserDao
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDatabaseTest {
    private lateinit var userDao: UserDao
    private lateinit var userDb: ShoppingAppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        userDb =
            Room.inMemoryDatabaseBuilder(context, ShoppingAppDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        userDao = userDb.userDao()
    }

    @After
    fun closeDb() {
        userDb.clearAllTables()
        userDb.close()
    }

    @Test
    fun insertAndGetUser() {
        val user = UserData(
            "sdjm43892yfh948ehod",
            "Vishal",
            "+919999988888",
            "vishal@somemail.com",
            "dh94328hd"
        )
        runBlocking {
            userDao.insert(user)
            val result = userDao.getById("sdjm43892yfh948ehod")
            assertEquals(user, result)
        }

    }

    @Test
    fun noData_returnsNull() {
        runBlocking {
            val result = userDao.getById("1232")
            assertThat(result, `is`(nullValue()))
        }
    }

    @Test
    fun insertClear_returnsNull() {
        val user = UserData(
            "sdjm43892yfh948ehod",
            "Vishal",
            "+919999988888",
            "vishal@somemail.com",
            "dh94328hd"
        )
        runBlocking {
            userDao.insert(user)
            userDao.clear()
            val result = userDao.getById("sdjm43892yfh948ehod")
            assertThat(result, `is`(nullValue()))
        }
    }
}