import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.serialization.decodeFromString
import org.apache.commons.io.FileUtils.cleanDirectory

class DownloadRepositoriesCommand : CliktCommand(name = "downloadRepos") {
    private val repositoriesListFile by argument("repositories").file(
        mustExist = true,
        mustBeReadable = true,
        canBeDir = false
    )
    private val outputDirectory by argument("output").file(canBeFile = false, mustBeWritable = true)
    private val yaml = Yaml()

    override fun run() {
        outputDirectory.mkdirs()
        cleanDirectory(outputDirectory)

        val config = yaml.decodeFromString<Repositories>(repositoriesListFile.readText())
        config.githubRepositories.forEach { githubRepository ->
            githubRepository.clone(outputDirectory)
        }
    }
}

fun main(args: Array<String>) = DownloadRepositoriesCommand().main(args)
