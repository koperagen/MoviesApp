package com.example.moviesapp.shared

inline class Page(val value: Int) {

    fun prev(): Page? {
        return if (value > 1) {
            Page(value - 1)
        } else {
            null
        }
    }

    companion object {

        fun from(value: Int): Page? {
            return if (value > 1) {
                Page(value)
            } else {
                null
            }
        }

        val FIRST = Page(1)

    }

}

fun Page?.next(totalPages: Int = Int.MAX_VALUE): Page? {
    if (this == null) return Page.FIRST
    return if (value == totalPages) {
        null
    } else {
        Page(value + 1)
    }
}

