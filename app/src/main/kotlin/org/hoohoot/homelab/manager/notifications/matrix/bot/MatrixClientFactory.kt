package org.hoohoot.homelab.manager.notifications.matrix.bot

import de.connect2x.trixnity.client.CryptoDriverModule
import de.connect2x.trixnity.client.MediaStoreModule
import de.connect2x.trixnity.client.RepositoriesModule
import de.connect2x.trixnity.client.cryptodriver.vodozemac.vodozemac
import de.connect2x.trixnity.client.media.okio.okio
import de.connect2x.trixnity.client.store.repository.exposed.exposed
import okio.Path.Companion.toOkioPath
import org.jetbrains.exposed.sql.Database
import java.io.File
import javax.sql.DataSource


fun createRepositoriesModule(dataSource: DataSource) =
    RepositoriesModule.exposed(
        database = Database.connect(dataSource, setupConnection = { it.schema = "trixnity" })
    )

fun createMediaStore(config: MatrixBotConfiguration) =
    MediaStoreModule.okio(basePath = File(config.dataDirectory() + "/media").toOkioPath())

fun createCryptoDriverModule() = CryptoDriverModule.vodozemac()
