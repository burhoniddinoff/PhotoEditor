package com.example.photoeditor.domain

import com.example.photoeditor.R
import com.example.photoeditor.data.EmojiData

class AppRepository {

    private val arrayOf = arrayOf(
        EmojiData(0, R.drawable.ic_glasses),
    )

    fun getAllEmojiData() = arrayOf

}