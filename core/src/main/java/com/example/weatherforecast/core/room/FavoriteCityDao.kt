package com.example.weatherforecast.core.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_cities ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteCityEntity): Long

    @Query("DELETE FROM favorite_cities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE favorite_cities SET alias = :alias, note = :note, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFields(id: Long, alias: String?, note: String?, updatedAt: Long)

    @Query(
        """
            SELECT COUNT(*) FROM favorite_cities
            WHERE LOWER(name) = LOWER(:name)
            AND COALESCE(LOWER(country), '') = COALESCE(LOWER(:country), '')
            AND COALESCE(LOWER(state),   '') = COALESCE(LOWER(:state),   '')
"""
    )
    fun countByIdentity(
        name: String,
        country: String?,
        state: String?
    ): kotlinx.coroutines.flow.Flow<Int>

    @Query(
        """
        DELETE FROM favorite_cities
        WHERE LOWER(name) = LOWER(:name)
        AND COALESCE(LOWER(country), '') = COALESCE(LOWER(:country), '')
        AND COALESCE(LOWER(state),   '') = COALESCE(LOWER(:state),   '')
    """
    )
    suspend fun deleteByIdentity(
        name: String,
        country: String?,
        state: String?
    )
}
