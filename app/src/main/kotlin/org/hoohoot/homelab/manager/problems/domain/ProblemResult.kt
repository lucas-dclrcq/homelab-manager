package org.hoohoot.homelab.manager.problems.domain

import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity

sealed interface ProblemResult {
    data class Ok(val workflow: ProblemWorkflowEntity) : ProblemResult
    data object NotFound : ProblemResult
    data class Invalid(val message: String) : ProblemResult
    data class Conflict(val message: String) : ProblemResult
    data object GrabFailed : ProblemResult
}
