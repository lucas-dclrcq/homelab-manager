package org.hoohoot.homelab.manager.problems.domain

// Qui agit sur un workflow : l'utilisateur (limité aux siens) ou un admin (tous, sans réassignation)
sealed interface Accessor {
    data class User(val username: String) : Accessor
    data object Admin : Accessor
}
