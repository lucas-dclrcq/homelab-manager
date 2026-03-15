package org.hoohoot.homelab.manager.giphy

data class Gif(val file: ByteArray, val width: Int = 0, val height: Int = 0)

data class NoGifFoundException(val query: String) : Exception("No GIF found for query: $query")
