package com.example.weatherforecast.core.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteCityDaoTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var dao: FavoriteCityDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // In-memory DB for integration testing. No files are persisted.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // OK for tests; do not use on production code.
            .build()
        dao = db.favoriteCityDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        name: String,
        country: String? = null,
        state: String? = null,
        lat: Double = 0.0,
        lon: Double = 0.0,
        alias: String? = null,
        note: String? = null,
        createdAt: Long = 0L,
        updatedAt: Long = 0L
    ) = FavoriteCityEntity(
        id = 0, // autoGenerate by Room
        name = name,
        alias = alias,
        note = note,
        country = country,
        state = state,
        lat = lat,
        lon = lon,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    @Test
    fun insert_and_observeAll_orders_by_createdAt_desc() = runTest {
        val a = entity(name = "A", country = "X", createdAt = 1_000)
        val b = entity(name = "B", country = "X", createdAt = 2_000)
        val c = entity(name = "C", country = "X", createdAt = 1_500)
        dao.insert(a); dao.insert(b); dao.insert(c)

        val list = dao.observeAll().first()
        // Should be ordered by createdAt DESC: B(2000), C(1500), A(1000)
        assertEquals(listOf("B", "C", "A"), list.map { it.name })
    }

    @Test
    fun updateFields_updates_alias_note_and_updatedAt() = runTest {
        val id = dao.insert(entity(name = "Taipei", country = "TW", alias = null, note = null))
        dao.updateFields(id = id, alias = "Home", note = "Fav", updatedAt = 9_999)

        val updated = dao.observeAll().first().first { it.id == id }
        assertEquals("Home", updated.alias)
        assertEquals("Fav", updated.note)
        assertEquals(9_999, updated.updatedAt)
    }

    @Test
    fun countByIdentity_emits_on_insert_and_delete() = runTest {
        // Subscribe first, then insert & delete; the Flow should emit 0 -> 1 -> 0.
        dao.countByIdentity(name = "Taipei", country = "TW", state = null).test {
            Assert.assertEquals(0, awaitItem()) // initial

            dao.insert(entity(name = "Taipei", country = "TW", state = null))
            Assert.assertEquals(1, awaitItem()) // after insert

            dao.deleteByIdentity(name = "Taipei", country = "TW", state = null)
            Assert.assertEquals(0, awaitItem()) // after delete

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun deleteById_removes_only_that_row() = runTest {
        val id1 = dao.insert(entity(name = "Taipei", country = "TW"))
        val id2 = dao.insert(entity(name = "Tokyo", country = "JP"))

        dao.deleteById(id1)

        val names = dao.observeAll().first().map { it.name }
        assertEquals(1, names.size)
        assertTrue("Tokyo" in names)
    }

    @Test
    fun deleteByIdentity_removes_only_matching_tuple() = runTest {
        dao.insert(entity(name = "Taipei", country = "TW", state = null))
        // Same city name but different country/state should remain after deletion.
        dao.insert(entity(name = "Taipei", country = "US", state = "OH"))

        dao.deleteByIdentity(name = "Taipei", country = "TW", state = null)

        val remaining = dao.observeAll().first()
        assertEquals(1, remaining.size)
        assertEquals("US", remaining.first().country)
        assertEquals("OH", remaining.first().state)
    }
}
