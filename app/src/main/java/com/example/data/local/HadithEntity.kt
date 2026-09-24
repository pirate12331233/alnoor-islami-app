package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.HadithData

@Entity(
    tableName = "hadiths",
    indices = [
        Index(value = ["bookKey"]),
        Index(value = ["hadithNumber"])
    ]
)
data class HadithEntity(
    @PrimaryKey val id: String,
    val bookKey: String,
    val book: String,
    val hadithNumber: String,
    val chapter: String,
    val narrator: String,
    val arabicText: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val grade: String = "Sahih (صحیح)",
    val reference: String = "",
    val isBundled: Boolean = true
) {
    fun toHadithData(): HadithData {
        return HadithData(
            id = id,
            book = book,
            hadithNumber = hadithNumber,
            chapter = chapter,
            narrator = narrator,
            arabicText = arabicText,
            urduTranslation = urduTranslation,
            englishTranslation = englishTranslation,
            grade = grade,
            reference = reference
        )
    }

    companion object {
        fun fromHadithData(h: HadithData, bookKey: String = "bukhari", isBundled: Boolean = true): HadithEntity {
            return HadithEntity(
                id = h.id,
                bookKey = bookKey,
                book = h.book,
                hadithNumber = h.hadithNumber,
                chapter = h.chapter,
                narrator = h.narrator,
                arabicText = h.arabicText,
                urduTranslation = h.urduTranslation,
                englishTranslation = h.englishTranslation,
                grade = h.grade,
                reference = h.reference,
                isBundled = isBundled
            )
        }
    }
}
