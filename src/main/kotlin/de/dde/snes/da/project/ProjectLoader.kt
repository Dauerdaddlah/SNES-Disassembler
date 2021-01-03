package de.dde.snes.da.project

import java.nio.file.Path

interface ProjectLoader {
    val path: Path

    fun save(project: Project)
    fun load(): Project

    companion object {
        fun getAllLoaders(): List<ProjectLoaderFactory> {
            return listOf(
                    ProjectLoaderSda.FACTORY,
                    ProjectLoaderDiztinguish.FACTORY,
                    ProjectLoaderDiztinguish.FACTORY_RAW
            )
        }
    }
}

interface ProjectLoaderFactory {
    val translation: String
    val fileExtension: String

    fun buildLoader(path: Path): ProjectLoader
}