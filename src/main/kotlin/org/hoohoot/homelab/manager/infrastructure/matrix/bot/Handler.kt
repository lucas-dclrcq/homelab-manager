package org.hoohoot.homelab.manager.infrastructure.matrix.bot

import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.exposed.createExposedRepositoriesModule
import okio.Path.Companion.toOkioPath
import org.jetbrains.exposed.sql.Database
import java.io.File


suspend fun createRepositoriesModule(config: MatrixBotConfiguration) =
    createExposedRepositoriesModule(database = Database.connect("jdbc:h2:${config.dataDirectory()}/database;DB_CLOSE_DELAY=-1"))

fun createMediaStore(config: MatrixBotConfiguration) =
    OkioMediaStore(File(config.dataDirectory() + "/media").toOkioPath())