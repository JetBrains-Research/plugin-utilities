import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class GithubRepositoryTest {
    @Test
    fun `full name should be correct for BaseRecyclerViewAdapterHelper`() {
        val repo = GithubRepository("https://github.com/CymChad/BaseRecyclerViewAdapterHelper", "")
        assertEquals("CymChad#BaseRecyclerViewAdapterHelper", repo.fullName)
    }

    @Test
    fun `full name should be correct for http BaseRecyclerViewAdapterHelper`() {
        val repo = GithubRepository("http://github.com/CymChad/BaseRecyclerViewAdapterHelper", "")
        assertEquals("CymChad#BaseRecyclerViewAdapterHelper", repo.fullName)
    }
}
