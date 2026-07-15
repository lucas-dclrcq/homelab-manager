package org.hoohoot.homelab.manager.problems.domain

sealed interface Accessor {
    data class User(val username: String) : Accessor
    data object Admin : Accessor
}
