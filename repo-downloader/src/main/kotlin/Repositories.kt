import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Represents a Github repository at a certain commit
 * @param url HTTPS url to a Github repository
 * @param commit Commit hash
 */
@Serializable
data class GithubRepository(val url: String, val commit: String) {
    /**
     * String in the format <repo-author>#<repo-name>
     */
    val fullName: String
        get() = "https?://github.com/".toRegex().replace(url, "").replace("/", "#")

    /**
     * Create a subdirectory in [parentFolder] named [fullName] and clone the repository there.
     * Only clones the specified commit with depth of 1
     */
    fun clone(parentFolder: File) {
        logger.info("Cloning $fullName")
        val repositoryFolder = parentFolder.resolve(fullName).also { it.mkdir() }
        with(repositoryFolder) {
            // https://stackoverflow.com/questions/3489173/how-to-clone-git-repository-with-specific-revision-changeset
            runCommand("git", "init")
            runCommand("git", "remote", "add", "origin", url)
            runCommand("git", "fetch", "origin", commit, "--depth", "1")
            runCommand("git", "reset", "--hard", "FETCH_HEAD")
        }
        logger.info("Cloned $fullName")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GithubRepository::class.java)
    }
}

/**
 * A list of repositories
 */
@Serializable
data class Repositories(val githubRepositories: List<GithubRepository>)

private fun File.runCommand(vararg arguments: String) {
    ProcessBuilder(*arguments)
        .directory(this)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(1, TimeUnit.HOURS)
}
