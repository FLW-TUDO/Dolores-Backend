package com.flw.dolores.factory

import com.flw.dolores.entities.Article
import com.flw.dolores.entities.ArticleDynamic
import com.google.gson.Gson
import org.bson.types.ObjectId
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.BufferedReader


class ArticleFactory {
    private val resource: Resource = ClassPathResource("entity_base/base_article.json")
    private val articleBaseFile: BufferedReader = BufferedReader(resource.inputStream.reader(Charsets.UTF_8))
    private val jsonString: String = articleBaseFile.readText()
    private val articleDynamicList: MutableList<ArticleDynamic> = Gson().fromJson(jsonString)

    fun loadDynamics(): MutableList<ArticleDynamic> {
        val articleDynamics: MutableList<ArticleDynamic> = mutableListOf()
        for (articleDynamic: ArticleDynamic in articleDynamicList) {

            val newArticle: Article = Article(
                id = ObjectId.get(),
                name = articleDynamic.article.name,
                abc_classification = articleDynamic.article.abc_classification,
                articleNumber = articleDynamic.article.articleNumber,
                purchasePrice = articleDynamic.article.purchasePrice,
                salesPrice = articleDynamic.article.salesPrice,
                minOrder = articleDynamic.article.minOrder,
                fixOrderCost = articleDynamic.article.fixOrderCost,
                discount = articleDynamic.article.discount,
                delivery = articleDynamic.article.delivery
            )

            val newArticleDynamic: ArticleDynamic = ArticleDynamic(
                id = ObjectId.get(),
                article = newArticle,
                currentStock = articleDynamic.currentStock,
                averageConsumption = articleDynamic.averageConsumption,
                pastConsumption = articleDynamic.pastConsumption.toMutableList(),
                pallet_count_processes = articleDynamic.pallet_count_processes.copyOf(),
                estimatedRange = articleDynamic.estimatedRange,
                optimalOrderQuantity = articleDynamic.optimalOrderQuantity
            )
            articleDynamics.add(newArticleDynamic)
        }
        return articleDynamics
    }
}