/*
 * Copyright © 2021 YUMEMI Inc. All rights reserved.
 */
package jp.co.yumemi.android.code_check

import android.content.Context
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jp.co.yumemi.android.code_check.TopActivity.Companion.lastSearchDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * TwoFragment で使う
 */
class SearchViewModel : ViewModel() {

    // 新機能の追加の部分にもかかわるが、検索結果を保持しておく関係もあり、かなり書き換える。
    private val client = HttpClient(Android)
    private val _searchResults = MutableLiveData<List<Items>>()
    val searchResults: LiveData<List<Items>> get() = _searchResults

    // suspend関数としておくことにより、非同期処理を効率的に行う。
    // 検索結果
    suspend fun searchResults(context: Context, query: String) { 
        withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = 
                    client.get("https://api.github.com/search/repositories") {
                        header("Accept", "application/vnd.github.v3+json")
                        parameter("q", query)
                    }
                // ここはnullチェックは不要であると判断した。
                val jsonBody = JSONObject(response.receive<String>())
                val jsonItems = jsonBody.optJSONArray("items")!!

                val items = mutableListOf<Items>()
                for (i in 0 until jsonItems.length()) {
                    val jsonItem = jsonItems.optJSONObject(i)!!
                    val name = jsonItem.optString("full_name", "N/A")
                    val ownerIconUrl =
                        jsonItem.optJSONObject("owner")?.optString("avatar_url") ?: ""
                    val language = jsonItem.optString("language", "N/A")
                    val stargazersCount = jsonItem.optLong("stargazers_count", 0)
                    val watchersCount = jsonItem.optLong("watchers_count", 0)
                    val forksCount = jsonItem.optLong("forks_count", 0)
                    val openIssuesCount = jsonItem.optLong("open_issues_count", 0)

                    items.add(
                        Items(
                            name = name,
                            ownerIconUrl = ownerIconUrl,
                            language = context.getString(R.string.written_language, language),
                            stargazersCount = stargazersCount,
                            watchersCount = watchersCount,
                            forksCount = forksCount,
                            openIssuesCount = openIssuesCount
                        )
                    )
                }
                lastSearchDate = Date()
                _searchResults.postValue(items)
            } catch (e: IOException) {
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    // clientをcloseする
    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}

@Parcelize
data class Items(
    val name: String,
    val ownerIconUrl: String,
    val language: String,
    val stargazersCount: Long,
    val watchersCount: Long,
    val forksCount: Long,
    val openIssuesCount: Long,
) : Parcelable
