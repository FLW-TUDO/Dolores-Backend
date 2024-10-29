package com.flw.dolores.factory

import com.flw.dolores.calculator.ThroughputCalculator
import com.flw.dolores.entities.ArticleDynamic
import com.flw.dolores.entities.Pallet
import com.flw.dolores.entities.StockGround
import com.flw.dolores.entities.Storage
import com.google.gson.Gson
import org.bson.types.ObjectId
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.BufferedReader


class StorageFactory {
    private val resource: Resource = ClassPathResource("entity_base/base_storage.json")
    private val palletsInStorageFile: BufferedReader = BufferedReader(resource.inputStream.reader(Charsets.UTF_8))

    private val throughputCalculator: ThroughputCalculator = ThroughputCalculator()
    private val jsonString: String = palletsInStorageFile.readText()
    private val palletList: MutableList<Pallet> = Gson().fromJson(jsonString)


    private fun loadPalletsInStorage(): MutableList<Pallet> {
        val palletsInStorage: MutableList<Pallet> = mutableListOf()
        for (pallet: Pallet in palletList) {
            val newPallet: Pallet = Pallet(
                id = ObjectId.get(),
                articleNumber = pallet.articleNumber,
                process = pallet.process.copy(),
                usedUnitSecurityDevices = pallet.usedUnitSecurityDevices,
                error = pallet.error,
                isStored = pallet.isStored,
                demandRound = pallet.demandRound
            )
            palletsInStorage.add(newPallet)
        }
        return palletsInStorage
    }

    fun loadStorage(articleDynamics: List<ArticleDynamic>): Storage {
        val storage = Storage(
            freeStocks = loadFreeStocks()
        )
        val palletsInStorage = loadPalletsInStorage()
        for (pallet in palletsInStorage) {
            val free = throughputCalculator.getFreeStockGround(storage.freeStocks, pallet, "C", 0)
            throughputCalculator.stockPallet(storage, free, pallet)
            throughputCalculator.moveArticleDynamicPalletCount(articleDynamics, pallet.articleNumber, -1, 2)
            throughputCalculator.increaseArticleStock(articleDynamics, pallet.articleNumber)
        }
        return storage
    }

    companion object {
        /**
         * Creates a new pallet object with a specific article number
         * @param articleNumber - article number
         * @param usd - unit security device used
         * @param errorState - pallet error state
         * @returns pallet object
         */
        fun createNewPallet(articleNumber: Int, usd: Boolean, errorState: Int = 0): Pallet {
            return Pallet(
                articleNumber = articleNumber,
                usedUnitSecurityDevices = usd,
                error = errorState,
            )
        }
    }

    private fun loadFreeStocks(): MutableList<StockGround> {
        val distSources0 = 53.75
        val distDrain0 = 27.75
        val distAvg0 = 40.75
        val freeStocks = mutableListOf<StockGround>()
        for (index in 0..3071) {
            val offset = index % 48
            val level = (index % (4 * 48)) % 4
            val abc = if (offset < 2) "A" else if (offset < 17) "B" else "C"

            val articleNumberOffset = index % 7
            val articleNumber =
                if (articleNumberOffset < 2) 100101
                else if (articleNumberOffset < 3) 100102
                else if (articleNumberOffset < 4) 100103
                else 100104
            freeStocks.add(
                StockGround(
                    distSource = distSources0 + offset,
                    distDrain = distDrain0 + offset,
                    distAvg = distAvg0 + offset,
                    level = level,
                    abc = abc,
                    articleNumber = articleNumber
                )
            )
        }
        return freeStocks
    }
}