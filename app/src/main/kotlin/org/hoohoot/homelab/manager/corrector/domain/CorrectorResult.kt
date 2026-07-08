package org.hoohoot.homelab.manager.corrector.domain

import org.hoohoot.homelab.manager.corrector.infra.CorrectorWorkflowEntity

sealed interface CorrectorResult {
    data class Ok(val workflow: CorrectorWorkflowEntity) : CorrectorResult
    data object NotFound : CorrectorResult
    data class Invalid(val message: String) : CorrectorResult
    data class Conflict(val message: String) : CorrectorResult
    data object GrabFailed : CorrectorResult
}
