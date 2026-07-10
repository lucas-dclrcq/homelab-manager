package org.hoohoot.homelab.manager.problems.domain

data class Release(
    val guid: String,
    val indexerId: Int,
    val indexer: String?,
    val title: String,
    val quality: String?,
    val size: Long?,
    val age: Int?,
    val seeders: Int?,
    val leechers: Int?,
    val protocol: String?,
    val rejected: Boolean,
    val rejections: List<String>,
    val languages: List<String>,
)

data class AnnotatedRelease(
    val release: Release,
    val isFrench: Boolean,
    val isRecommended: Boolean,
)
