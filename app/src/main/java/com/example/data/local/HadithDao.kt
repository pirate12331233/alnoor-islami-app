package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {
    @Query("SELECT * FROM hadiths WHERE id = :id LIMIT 1")
    suspend fun getHadithById(id: String): HadithEntity?

    @Query("SELECT * FROM hadiths WHERE bookKey = :bookKey ORDER BY CAST(hadithNumber AS INTEGER) ASC LIMIT :limit OFFSET :offset")
    suspend fun getHadithsByBookPaged(bookKey: String, limit: Int, offset: Int): List<HadithEntity>

    @Query("SELECT * FROM hadiths WHERE bookKey = :bookKey AND hadithNumber = :hadithNumber LIMIT 1")
    suspend fun getHadithByBookAndNumber(bookKey: String, hadithNumber: String): HadithEntity?

    @Query("SELECT * FROM hadiths ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomHadith(): HadithEntity?

    @Query("SELECT * FROM hadiths WHERE id != :currentId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomHadithExcluding(currentId: String): HadithEntity?

    @Query("SELECT * FROM hadiths WHERE bookKey = :bookKey AND id != :currentId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomHadithByBookExcluding(bookKey: String, currentId: String): HadithEntity?

    @Query("SELECT COUNT(*) FROM hadiths")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM hadiths WHERE bookKey = :bookKey")
    suspend fun getCountByBook(bookKey: String): Int

    @Query("SELECT COUNT(*) FROM hadiths WHERE bookKey = :bookKey")
    fun observeCountByBook(bookKey: String): Flow<Int>

    @Query("""
        SELECT * FROM hadiths 
        WHERE arabicText LIKE '%' || :query || '%' 
           OR urduTranslation LIKE '%' || :query || '%' 
           OR englishTranslation LIKE '%' || :query || '%' 
           OR narrator LIKE '%' || :query || '%'
           OR chapter LIKE '%' || :query || '%'
        ORDER BY CAST(hadithNumber AS INTEGER) ASC
        LIMIT 100
    """)
    suspend fun searchHadiths(query: String): List<HadithEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadiths(hadiths: List<HadithEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadith(hadith: HadithEntity)

    @Query("DELETE FROM hadiths WHERE bookKey = :bookKey AND isBundled = 0")
    suspend fun deleteDownloadedBook(bookKey: String)

    @Query("SELECT DISTINCT bookKey FROM hadiths")
    suspend fun getAvailableBookKeys(): List<String>
}
