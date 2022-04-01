package org.geezer.db.migrations

import java.io.File

class ClassPathMigrationFilesLocation(val classPath: String) : MigrationFilesLocation {
    override val migrationFilesOrErrors: List<Pair<MigrationFile?, String>> by lazy { File(Thread.currentThread().contextClassLoader.getResource(classPath).path.replace("%20", " ")).listFiles().map { MigrationFile.fromFile(it) } }
}
