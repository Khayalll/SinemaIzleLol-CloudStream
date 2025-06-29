import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class SinemaIzleLol : MainAPI() {
    override var mainUrl = "https://sinema.izle.lol"
    override var name = "SinemaIzleLol"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie)

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=${query.replace(" ", "+")}").document
        return doc.select(".filmler .film a").map {
            val title = it.selectFirst(".title")?.text() ?: return@map null
            val href = it.attr("href")
            MovieSearchResponse(title, href, name, TvType.Movie)
        }.filterNotNull()
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: return ErrorLoadResponse
        val iframe = doc.select("iframe[src]").firstOrNull { it.hasAttr("src") }
        val src = iframe?.attr("src")?.trim() ?: return ErrorLoadResponse

        val sources = mutableListOf<ExtractorLink>()
        loadExtractor(src, name, sources)
        return MovieLoadResponse(title, url, name, TvType.Movie, sources = sources)
    }

    override fun getMainPage(): HomePageResponse {
        val list = listOf(
            HomePageList("Yeni Filmler", mainUrl, listOf(HomePageList.GridType.Popular)) {
                search("")
            }
        )
        return HomePageResponse(list)
    }
}
