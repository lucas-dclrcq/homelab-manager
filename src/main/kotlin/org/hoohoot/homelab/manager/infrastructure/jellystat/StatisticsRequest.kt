package org.hoohoot.homelab.manager.infrastructure.jellystat

data class StatisticsRequest(
    val type: String,
    val days: String
)