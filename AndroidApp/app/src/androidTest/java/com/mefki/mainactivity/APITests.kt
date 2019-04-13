package com.mefki.mainactivity

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mefki.mainactivity.datasource.API
import com.mefki.mainactivity.datasource.APIImpl
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class APITests {

    private lateinit var api: API

    @Before
    fun start(){
        api = APIImpl()
    }

    @Test
    fun getStations() {
        api.getStationsLocalizations().subscribe { stationsList ->
            Assert.assertNotEquals(stationsList, null)
        }
    }
    fun getNumberFromStations(){
        api.getAvailableBikes(11111,11110).subscribe { stationsList ->
            Assert.assertEquals(stationsList.size, 2)
        }
    }
}