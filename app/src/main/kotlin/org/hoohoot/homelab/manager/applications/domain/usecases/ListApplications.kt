package org.hoohoot.homelab.manager.applications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.applications.domain.ports.Applications
import org.hoohoot.homelab.manager.applications.infra.ApplicationSummary

@ApplicationScoped
class ListApplications(private val applications: Applications) {
    suspend operator fun invoke(): List<ApplicationSummary> = applications.listSummaries()
}
