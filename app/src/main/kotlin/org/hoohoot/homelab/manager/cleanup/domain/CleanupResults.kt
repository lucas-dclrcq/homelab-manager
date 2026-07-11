package org.hoohoot.homelab.manager.cleanup.domain

import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity

sealed interface ScanResult {
    data class Started(val campaign: CleanupCampaignEntity, val candidateCount: Int) : ScanResult
    data object AlreadyActive : ScanResult
    data class ThresholdNotReached(val freeBytes: Long) : ScanResult
    data object NoCandidates : ScanResult
    data object DiskSpaceUnknown : ScanResult
}

sealed interface VetoResult {
    data class Ok(val candidate: CleanupCandidateEntity) : VetoResult
    data object NotFound : VetoResult
    data class Invalid(val message: String) : VetoResult
}

sealed interface VetoByTitleResult {
    data class Ok(val protectedTitles: List<String>) : VetoByTitleResult
    data object NoCampaign : VetoByTitleResult
    data class NoMatch(val proposedTitles: List<String>) : VetoByTitleResult
    data class Ambiguous(val titles: List<String>) : VetoByTitleResult
}

sealed interface ProtectResult {
    data class Ok(val protection: CleanupProtectionEntity) : ProtectResult
    data class AlreadyProtected(val protection: CleanupProtectionEntity) : ProtectResult
    data class Invalid(val message: String) : ProtectResult
    data object MediaNotFound : ProtectResult
}

sealed interface UnprotectResult {
    data object Ok : UnprotectResult
    data object NotFound : UnprotectResult
    data object Forbidden : UnprotectResult
}

sealed interface CampaignActionResult {
    data class Ok(val campaign: CleanupCampaignEntity) : CampaignActionResult
    data object NotFound : CampaignActionResult
    data class Invalid(val message: String) : CampaignActionResult
}

sealed interface RetryResult {
    data class Ok(val candidate: CleanupCandidateEntity) : RetryResult
    data object NotFound : RetryResult
    data class Invalid(val message: String) : RetryResult
    data class StillFailing(val candidate: CleanupCandidateEntity) : RetryResult
}
