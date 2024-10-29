package com.flw.dolores.calculator

import com.flw.dolores.entities.*
import com.flw.dolores.factory.MessageFactory
import com.flw.dolores.factory.StorageFactory
import kotlin.random.Random

class ThroughputCalculator {

    /**
     * Calculates the probability distribution for the determination of pallet damage.
     * Each index represents one form of damage.
     * @param roundValues current round values
     * @returns damage probability array
     */
    private fun calculateDamageProbability(roundValues: RoundValues): Array<Double> {
        val usedUnitSecurityDevices = roundValues.unit_security_devices_used
        val currentLoadingEquipmentCostIndex =
            GameValues.loading_equipment_level.indexOf(roundValues.loading_equipment_level)
        val currentLoadingEquipmentCrashChance =
            GameValues.loading_equipment_crash_chance[currentLoadingEquipmentCostIndex]

        val damageProbability =
            GameValues.error_damage / GameValues.error_sum * roundValues.avg_error_chance_processes[1] * (1 - roundValues.pallet_we_factor)
        val wrongDeliveryProbability =
            GameValues.error_wrong_delivered / GameValues.error_sum * roundValues.avg_error_chance_processes[1] * (1 - roundValues.pallet_we_factor)
        val wrongRetrievalProbability =
            GameValues.errorWrongRetrieval / GameValues.error_sum * roundValues.avg_error_chance_processes[roundValues.avg_error_chance_processes.size - 2] * (1 - roundValues.pallet_wa_factor)

        val transportDamageEnProbability: Double
        val transportDamageLaProbability: Double
        val transportDamageVeProbability: Double
        if (usedUnitSecurityDevices) {
            transportDamageEnProbability =
                GameValues.error_transport_damage_en / GameValues.error_sum * (currentLoadingEquipmentCrashChance * GameValues.global_crash_factor_with_loading_equipment + GameValues.probability_crash_with_unit_safety_devices * GameValues.global_crash_factor_with_security_devices + roundValues.avg_error_chance_processes[0] * GameValues.global_crash_factor_employee)
            transportDamageLaProbability =
                GameValues.error_transport_damage_la / GameValues.error_sum * (currentLoadingEquipmentCrashChance * GameValues.global_crash_factor_with_loading_equipment + GameValues.probability_crash_with_unit_safety_devices * GameValues.global_crash_factor_with_security_devices + roundValues.avg_error_chance_processes[2] * GameValues.global_crash_factor_employee)
            transportDamageVeProbability =
                GameValues.error_transport_damage_ve / GameValues.error_sum * (currentLoadingEquipmentCrashChance * GameValues.global_crash_factor_with_loading_equipment + GameValues.probability_crash_with_unit_safety_devices * GameValues.global_crash_factor_with_security_devices + roundValues.avg_error_chance_processes[4] * GameValues.global_crash_factor_employee)
        } else {
            transportDamageEnProbability =
                GameValues.error_transport_damage_en / GameValues.error_sum * (currentLoadingEquipmentCrashChance * GameValues.global_crash_factor_with_loading_equipment + GameValues.probability_crash_without_unit_safety_devices * GameValues.global_crash_factor_with_security_devices + roundValues.avg_error_chance_processes[0] * GameValues.global_crash_factor_employee)
            transportDamageLaProbability =
                GameValues.error_transport_damage_la / GameValues.error_sum * (currentLoadingEquipmentCrashChance * GameValues.global_crash_factor_with_loading_equipment + GameValues.probability_crash_without_unit_safety_devices * GameValues.global_crash_factor_with_security_devices + roundValues.avg_error_chance_processes[2] * GameValues.global_crash_factor_employee)
            transportDamageVeProbability =
                GameValues.error_transport_damage_ve / GameValues.error_sum * (currentLoadingEquipmentCrashChance * GameValues.global_crash_factor_with_loading_equipment + GameValues.probability_crash_without_unit_safety_devices * GameValues.global_crash_factor_with_security_devices + roundValues.avg_error_chance_processes[4] * GameValues.global_crash_factor_employee)
        }

        val accumulatedProbability = arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        accumulatedProbability[6] = damageProbability
        accumulatedProbability[5] = accumulatedProbability[6] + wrongDeliveryProbability
        accumulatedProbability[4] = accumulatedProbability[5] + wrongRetrievalProbability
        accumulatedProbability[3] = accumulatedProbability[4] + transportDamageEnProbability
        accumulatedProbability[2] = accumulatedProbability[3] + transportDamageLaProbability
        accumulatedProbability[1] = accumulatedProbability[2] + transportDamageVeProbability
        accumulatedProbability[0] = 1.0

        return accumulatedProbability
    }

    /**
     * Calculates the order cost and order fix cost for an order and updates the round values
     * @param roundValues current round values
     * @param order current order
     */
    private fun updateOrderCosts(roundValues: RoundValues, order: Order) {
        val index = order.articleNumber - 100101
        roundValues.order_costs_article[index] += (order.realPurchasePrice + order.deliveryCosts) * order.deliveredQuantity
        roundValues.order_fix_costs_article[index] += order.fixCosts
        roundValues.current_order_costs = order.deliveredQuantity * (order.realPurchasePrice + order.deliveryCosts)
    }

    /**
     * Calculates the damage to a new pallet based on the damage probability array
     * @param damageProbability  damage probability array
     * @returns error state of the pallet
     */
    private fun getPalletErrorState(damageProbability: Array<Double>): Int {
        val random = Random.nextDouble()
        return damageProbability.indexOfLast { random <= it }
    }

    /**
     * Informs the player of important events regarding the current orders.
     * Create messages to inform the player.
     * @param messages  current game messages
     * @param orders  current game orders
     * @param roundNumber  current round number
     */
    private fun createMessagesForOrders(
        messages: MutableList<Message>,
        orders: MutableList<Order>,
        roundNumber: Int
    ) {
        for (order in orders) {
            if (order.deliveryRound < roundNumber) {
                //message to late delivery
                if (order.quantity == order.deliveredQuantity && order.deliveryRound > order.deliveryWishRound) {
                    val textDE =
                        "Die Bestellung des Artikels ${order.articleNumber} ist vollständig eingetroffen mit einer Verspätung von ${order.deliveryRound - order.deliveryWishRound} Runden."
                    val textEN =
                        "The order of article ${order.articleNumber} arrived completely with a delay of ${order.deliveryRound - order.deliveryWishRound} rounds."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    messages.add(0, message)
                }
                //message delivered
                else if (order.quantity == order.deliveredQuantity && order.deliveryRound == order.deliveryWishRound) {
                    val textDE =
                        "Die Bestellung des Artikels ${order.articleNumber} ist vollständig und pünktlich eingetroffen."
                    val textEN = "The order of article ${order.articleNumber} fully arrived on time."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    messages.add(0, message)
                }
                //message delivered not complete and late
                else if (order.quantity != order.deliveredQuantity && order.deliveryRound > order.deliveryWishRound) {
                    val textDE =
                        "Die Bestellung des Artikels ${order.articleNumber} ist eingetroffen. Fehlende Paletten: ${order.quantity - order.deliveredQuantity}. Verspätung: ${order.deliveryRound - order.deliveryWishRound}"
                    val textEN =
                        "The order of article ${order.articleNumber} arrived. Missing pallets: ${order.quantity - order.deliveredQuantity}. Delay: ${order.deliveryRound - order.deliveryWishRound}"
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    messages.add(0, message)
                }
                //message delivered not complete
                else if (order.quantity != order.deliveredQuantity && order.deliveryRound == order.deliveryWishRound) {
                    val textDE =
                        "Die Bestellung des Artikels ${order.articleNumber} ist eingetroffen. Fehlende Paletten: ${order.quantity - order.deliveredQuantity}"
                    val textEN =
                        "The order of article ${order.articleNumber} arrived. Missing pallets: ${order.quantity - order.deliveredQuantity}"
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    messages.add(0, message)
                }
            }
        }
    }

    /**
     * Calculates the current technology factor based on the currently chosen technology
     * @param roundValues  current round values
     * @returns  current technology factor
     */
    private fun calculateItFactor(roundValues: RoundValues): Double {
        val currentItCosts = roundValues.itCosts
        val index = GameValues.technology_cost.indexOf(currentItCosts)
        return GameValues.technology_factor[index]
    }

    /**
     * Calculates the take-up and release time for all conveyor
     * @param roundValues  current round values
     * @returns  take up and release time for all conveyor
     */
    private fun calculateTakeUpAndReleaseTime(roundValues: RoundValues): Double {
        val currentLoadingEquipmentCosts = roundValues.loading_equipment_level
        val index = GameValues.loading_equipment_level.indexOf(currentLoadingEquipmentCosts)
        val currentLoadingEquipmentFactor = GameValues.loading_equipment_factor[index]
        return GameValues.timeTakeUpRelease * (2 - currentLoadingEquipmentFactor)
    }

    /**
     * Calculates the game time of the current action
     * @param track distance to the target
     * @param speed  speed of the conveyor
     * @param f_it current technology factor
     * @param t_pick  current pickup time of the conveyor
     * @param t_raise  current raise time of the conveyor
     * @returns  game time of the current action
     */
    private fun calculateTime(track: Double, speed: Double, f_it: Double, t_pick: Double, t_raise: Int): Double {
        return if (speed == 0.0)
            -1.0
        else
            track / speed * (2 - f_it) + 2.0 * t_pick + t_raise
    }

    /**
     * Moves the pallet count from one process to another.
     * Increases the pallet count for the target and decreases for the source process.
     * @param articleDynamics  current article dynamics
     * @param articleNumber  current article number
     * @param startId  source process
     * @param targetId  target process
     */
    fun moveArticleDynamicPalletCount(
        articleDynamics: List<ArticleDynamic>,
        articleNumber: Int,
        startId: Int,
        targetId: Int
    ) {
        val articleDynamic = articleDynamics.first { it.article.articleNumber == articleNumber }
        if (startId > -1)
            articleDynamic.pallet_count_processes[startId] -= 1
        if (targetId < 5)
            articleDynamic.pallet_count_processes[targetId] += 1
    }

    /**
     * Moves a pallet to the next process and updates the capacities and articleDynamics accordingly.
     * @param roundValues  current round values
     * @param articleDynamics  current article dynamics
     * @param pallet  currently moved pallet
     * @param time  current game time
     */
    private fun movePalletToNextLevel(
        roundValues: RoundValues,
        articleDynamics: MutableList<ArticleDynamic>,
        pallet: Pallet,
        time: Double
    ) {
        val processID = pallet.process.toInt()
        pallet.process = Process.fromInt(processID + 1)
        this.moveArticleDynamicPalletCount(articleDynamics, pallet.articleNumber, processID, processID + 1)
        roundValues.capacity_processes[processID] -= time
        roundValues.pallets_transported_process[processID] += 1
    }

    /**
     * Calculates the currently needed control steps.
     * @param roundValues  current round values
     * @returns control steps
     */
    private fun calculateControlDistance(roundValues: RoundValues): Int {
        return if (roundValues.pallet_we_factor == 0.0) -1 else kotlin.math.floor((1.0 / roundValues.pallet_we_factor))
            .toInt()
    }

    /**
     * Moves a pallet into storage and updates storage accordingly.
     * Removes the previous free stock ground and places it in the occupied stock grounds.
     * @param storage  game state storage
     * @param free  free stock ground chosen for the pallet
     * @param pallet  currently moved pallet
     */
    fun stockPallet(storage: Storage, free: StockGround, pallet: Pallet) {
        val freeStocks = storage.freeStocks
        val occStocks = storage.occStocks

        val index = freeStocks.indexOf(free)
        freeStocks.removeAt(index)
        free.pallet = pallet
        occStocks.add(free)
    }

    /**
     * Processes a newly ordered pallet and moves it to the first process.
     * Creates new pallets based on current orders.
     * @param roundValues  current round values
     * @param storage current game storage
     * @param orders  current orders
     * @param articleDynamics current article dynamics
     */
    private fun palletThroughputTruckToEn(
        roundValues: RoundValues,
        roundNumber: Int,
        storage: Storage,
        orders: MutableList<Order>,
        articleDynamics: MutableList<ArticleDynamic>
    ) {
        val damageProbability = this.calculateDamageProbability(roundValues)
        val usedUnitSecurityDevices = roundValues.unit_security_devices_used
        for (order in orders) {
            if (order.deliveryRound < roundNumber) {
                this.updateOrderCosts(roundValues, order)
                for (index in 0 until order.deliveredQuantity) {
                    val errorState = getPalletErrorState(damageProbability)
                    val pallet =
                        StorageFactory.createNewPallet(order.articleNumber, usedUnitSecurityDevices, errorState)
                    storage.pallets_not_in_storage.add(pallet)
                    moveArticleDynamicPalletCount(articleDynamics, order.articleNumber, -1, 0)
                }
            }
        }
    }

    /**
     * Moves all pallets in the first process to the second process if the capacity permits.
     * @param roundValues current round values
     * @param storage current game storage
     * @param articleDynamics current article dynamics
     */
    private fun palletThroughputEnToWv(
        roundValues: RoundValues,
        storage: Storage,
        articleDynamics: MutableList<ArticleDynamic>
    ) {
        val currentItFactor = calculateItFactor(roundValues)
        val trueTakeUpReleaseTime = calculateTakeUpAndReleaseTime(roundValues)

        for (pallet: Pallet in storage.pallets_not_in_storage) {
            if (pallet.process != Process.UNLOADING) continue
            var time = calculateTime(
                GameValues.transport_time[0],
                roundValues.avg_speed_processes[0],
                currentItFactor,
                trueTakeUpReleaseTime,
                GameValues.lift_layer_duration[0]
            )
            if (pallet.error == 3 && time != -1.0) {
                time += GameValues.time_crash
                roundValues.crash_time_processes[0] += GameValues.time_crash
            }
            if (time == -1.0 || roundValues.capacity_processes[0] < time) {
                roundValues.pallets_not_transported_process[0] += 1
            } else {
                movePalletToNextLevel(roundValues, articleDynamics, pallet, time)
            }
        }
    }

    /**
     * Moves all pallets in the second process to the storage input if the capacity permits.
     * @param roundValues current round values
     * @param storage current game storage
     * @param articleDynamics current article dynamics
     */
    private fun palletThroughputWvToLe(
        roundValues: RoundValues,
        storage: Storage,
        articleDynamics: MutableList<ArticleDynamic>
    ) {
        var controlDistance = calculateControlDistance(roundValues)
        var controlCounter = controlDistance

        for (pallet: Pallet in storage.pallets_not_in_storage) {
            if (pallet.process != Process.COLLECTION) continue
            var time = GameValues.pallet_control_time_static_we

            if (controlCounter == 1)
                time += GameValues.pallet_control_time_dynamic_we
            if (roundValues.capacity_processes[1] < time)
                roundValues.pallets_not_transported_process[1] += 1
            else {
                if (roundValues.unit_security_devices_used) {
                    time += GameValues.pallet_unit_time_security_devices
                    roundValues.costs_usd += GameValues.usd_costs_per_pallet
                }
                movePalletToNextLevel(roundValues, articleDynamics, pallet, time.toDouble())
                if (controlCounter == 1) controlDistance += 1
                controlCounter -= 1
            }
        }
    }

    /**
     * Determines the designated stock ground for an incoming pallet based on the chosen strategies.
     * @param freeStocks all free stock ground
     * @param pallet currently moved pallet
     * @param abc abc value of the stock ground
     * @param strategyStorage strategy for storing pallets
     * @returns designated stock ground
     */
    fun getFreeStockGround(
        freeStocks: MutableList<StockGround>,
        pallet: Pallet,
        abc: String,
        strategyStorage: Int
    ): StockGround {
        try {
            return freeStocks.first {
                strategyStorage == 0
                        || (strategyStorage == 1 && it.articleNumber == pallet.articleNumber)
                        || (strategyStorage == 2 && it.abc == abc)
            }
            // No pallets are matching the search criteria
        } catch (e: NoSuchElementException) {
            // If the storage is not yet full just return the first free StockGround
            return if (freeStocks.isNotEmpty()) {
                freeStocks[0]
                // If the storage is full just create a new StockGround and add it to the free Storage
            } else {
                val stg = StockGround(
                    distSource = 2.0,
                    distDrain = 2.0,
                    distAvg = 2.0,
                    level = 1,
                    abc = abc,
                    articleNumber = pallet.articleNumber
                )
                freeStocks.add(stg)
                stg
            }
        }
    }

    /**
     * Increases the overall stock of an article
     * @param articleDynamics list of all article dynamics
     * @param articleNumber specific article number of the moved pallet
     */
    fun increaseArticleStock(articleDynamics: List<ArticleDynamic>, articleNumber: Int) {
        val articleDynamic = articleDynamics.first { it.article.articleNumber == articleNumber }
        articleDynamic.currentStock += 1
    }

    /**
     * Decreases the overall stock of an article
     * @param articleDynamics list of all article dynamics
     * @param articleNumber specific article number of the moved pallet
     */
    private fun decreaseArticleStock(articleDynamics: List<ArticleDynamic>, articleNumber: Int) {
        val articleDynamic = articleDynamics.first { it.article.articleNumber == articleNumber }
        articleDynamic.currentStock -= 1
    }

    /**
     * Moves all pallets in the storage input to the storage if the capacity permits.
     * @param roundValues current round values
     * @param storage current game storage
     * @param articleDynamics current article dynamics
     */
    private fun palletThroughputLeToSt(
        roundValues: RoundValues,
        storage: Storage,
        articleDynamics: MutableList<ArticleDynamic>
    ) {
        val currentItFactor = calculateItFactor(roundValues)
        val trueTakeUpReleaseTime = calculateTakeUpAndReleaseTime(roundValues)
        val strategyStorage = roundValues.strategy_storage
        val strategyIn = roundValues.strategy_incoming


        if (strategyIn == 1)
            storage.freeStocks.sortBy { it.distSource }
        else if (strategyIn == 0)
            storage.freeStocks.shuffle()

        for (pallet: Pallet in storage.pallets_not_in_storage) {
            if (pallet.process != Process.STORAGE) continue
            if (!pallet.isStored) {
                val abc = articleDynamics[pallet.articleNumber - 100101].article.abc_classification
                val free = getFreeStockGround(storage.freeStocks, pallet, abc, strategyStorage)
                var time = calculateTime(
                    free.distSource,
                    roundValues.avg_speed_processes[2],
                    currentItFactor,
                    trueTakeUpReleaseTime,
                    GameValues.lift_layer_duration[free.level]
                )
                if (pallet.error == 2 && time != -1.0) {
                    time += GameValues.time_crash
                    roundValues.crash_time_processes[2] += GameValues.time_crash
                }
                if (time == -1.0 || roundValues.capacity_storage_in < time) {
                    roundValues.pallets_not_transported_process[2] += 1
                    roundValues.not_transported_pallets_la_in += 1
                } else {
                    roundValues.capacity_processes[2] -= time
                    roundValues.capacity_storage_in -= time

                    increaseArticleStock(articleDynamics, pallet.articleNumber)

                    stockPallet(storage, free, pallet)
                    pallet.isStored = true
                    roundValues.pallets_transported_process[2] += 1
                    roundValues.pallets_transported_la_in += 1
                }
            }
        }
    }

    /**
     * Removes a newly empty stock ground from the list of occupied stock grounds.
     * @param storage current game storage
     * @param occStock newly unoccupied stock ground
     */
    private fun removeOccPallet(storage: Storage, occStock: StockGround) {
        val occStocks = storage.occStocks
        val freeStocks = storage.freeStocks
        val index = occStocks.indexOf(occStock)
        occStocks.removeAt(index)
        freeStocks.add(occStock)
    }

    /**
     * Determines a designated stock ground which will be chosen to satisfy a customer job.
     * @param occStocks currently occupied stock grounds
     * @param jobArticleNumber desired article number
     * @returns designated stock ground for a customer job
     */
    private fun unStockPallet(occStocks: MutableList<StockGround>, jobArticleNumber: Int): StockGround {
        return occStocks.first { it.pallet?.articleNumber == jobArticleNumber }
    }

    /**
     * Moves all pallets in storage to the storage output if the capacity permits.
     * @param roundValues current round values
     * @param storage current game storage
     * @param customerJobs current customer jobs
     * @param articleDynamics current article dynamics
     */
    private fun palletThroughputStToLa(
        roundValues: RoundValues,
        storage: Storage,
        customerJobs: MutableList<CustomerJob>,
        articleDynamics: MutableList<ArticleDynamic>
    ) {
        val currentItFactor = calculateItFactor(roundValues)
        val trueTakeUpReleaseTime = calculateTakeUpAndReleaseTime(roundValues)
        val strategyOut = roundValues.strategy_outgoing
        val occStocks = storage.occStocks

        for (articleDynamic: ArticleDynamic in articleDynamics) {
            articleDynamic.pastConsumption.add(0)
        }

        if (strategyOut == 2)
            occStocks.sortBy { it.distSource }
        else if (strategyOut == 3)
            occStocks.shuffle()

        var capacityIsFullyUsed = false
        for (job in customerJobs) {
            val articleNumber = job.articleNumber
            val dynamic = articleDynamics[articleNumber - 100101]
            val currentStock = dynamic.currentStock

            if (currentStock <= 0) continue

            if (!capacityIsFullyUsed) {
                val outputPalletsForJob = kotlin.math.min(job.remainingQuantity, currentStock)

                for (index in 0 until outputPalletsForJob) {
                    val occStock = unStockPallet(occStocks, articleNumber)
                    val way = occStock.distDrain
                    val time = calculateTime(
                        way,
                        roundValues.avg_speed_processes[2],
                        currentItFactor,
                        trueTakeUpReleaseTime,
                        GameValues.lift_layer_duration[occStock.level]
                    )
                    if (time == -1.0 || time > roundValues.capacity_storage_out) {
                        capacityIsFullyUsed = true
                        roundValues.not_transported_pallets_la_out++
                        roundValues.pallets_not_transported_process[2]++
                    } else {
                        val pallet = occStock.pallet
                        if (pallet != null) {
                            job.remainingQuantity--
                            movePalletToNextLevel(roundValues, articleDynamics, pallet, time)
                            roundValues.pallets_transported_la_out++
                            pallet.demandRound = job.demandRound
                            dynamic.pastConsumption[dynamic.pastConsumption.size - 1]++

                            decreaseArticleStock(articleDynamics, pallet.articleNumber)
                            pallet.isStored = false
                            storage.pallets_not_in_storage.add(pallet)
                            removeOccPallet(storage, occStock)
                            roundValues.capacity_storage_out -= time
                        }
                    }
                }
            } else {
                roundValues.pallets_not_transported_process[2] += job.quantity
                roundValues.not_transported_pallets_la_out += job.quantity
            }
        }
    }

    /**
     * Moves all pallets in the storage output to the fourth process if the capacity permits.
     * @param roundValues current round values
     * @param storage current game storage
     * @param articleDynamics current article dynamics
     */
    private fun palletThroughputLaToVe(
        roundValues: RoundValues,
        storage: Storage,
        articleDynamics: MutableList<ArticleDynamic>
    ) {
        val palletsNotInStorage = storage.pallets_not_in_storage
        palletsNotInStorage.sortBy { it.demandRound }

        val controlDistance =
            if (roundValues.pallet_wa_factor != 0.0) kotlin.math.floor(1 / roundValues.pallet_wa_factor).toInt() else -1
        var controlCounter = controlDistance

        for (pallet: Pallet in palletsNotInStorage) {
            if (pallet.process != Process.CONTROL) continue
            val time =
                if (controlCounter == 1) GameValues.pallet_control_time_static_wa + GameValues.pallet_control_time_dynamic_wa else GameValues.pallet_control_time_static_wa

            if (roundValues.capacity_processes[3] < time) {
                roundValues.pallets_not_transported_process[3] += 1
            } else {
                movePalletToNextLevel(roundValues, articleDynamics, pallet, time.toDouble())
                if (controlCounter == 1)
                    controlCounter = controlDistance
                else
                    controlCounter -= 1
            }
        }
    }

    /**
     * Checks the error status of the pallet.
     * @param roundValues current round values
     * @param pallet a pallet
     */
    private fun checkPalletForError(roundValues: RoundValues, pallet: Pallet) {
        for (errorType in 0 until 7) {
            if (pallet.error == errorType)
                roundValues.pallet_quantity_per_errors[errorType] += 1
        }
    }

    /**
     * Moves all pallets in the fourth process to the fifth process if the capacity permits.
     * @param roundValues current round values
     * @param storage current game storage
     * @param customerJobs current customer jobs
     * @param articleDynamics current article dynamics
     */
    private fun palletThroughputVeToTruck(
        roundValues: RoundValues,
        roundNumber: Int,
        storage: Storage,
        customerJobs: MutableList<CustomerJob>,
        articleDynamics: MutableList<ArticleDynamic>
    ) {
        val currentItFactor = calculateItFactor(roundValues)
        val trueTakeUpReleaseTime = calculateTakeUpAndReleaseTime(roundValues)

        for (pallet: Pallet in storage.pallets_not_in_storage) {
            if (pallet.process != Process.LOADING) continue
            var time = calculateTime(
                GameValues.transport_time[1],
                roundValues.avg_speed_processes[4],
                currentItFactor,
                trueTakeUpReleaseTime,
                GameValues.lift_layer_duration[0]
            )
            if (pallet.error == 1 && time != -1.0) {
                time += GameValues.time_crash
                roundValues.crash_time_processes[4] += GameValues.time_crash
            }
            if (time == -1.0 || roundValues.capacity_processes[4] < time) {
                roundValues.pallets_not_transported_process[4] += 1
            } else {
                val uncompletedJob = customerJobs.first { it.articleNumber == pallet.articleNumber && it.quantity > 0 }

                uncompletedJob.quantity -= 1

                this.movePalletToNextLevel(roundValues, articleDynamics, pallet, time)
                this.checkPalletForError(roundValues, pallet)

                if (uncompletedJob.demandRound >= roundNumber - 1)
                    roundValues.accurate_delivered_pallets += 1
                else
                    roundValues.late_delivered_pallets += 1

                val articleNumber = uncompletedJob.articleNumber - 100101
                val currentDynamic = articleDynamics[articleNumber]
                roundValues.sales_income_article[articleNumber] += currentDynamic.article.salesPrice
            }
        }
    }

    /**
     * Filters the currently moved pallets. Removes stored pallets from the list.
     * @param storage storage object of the current state
     * @return filtered pallets not in storage
     */
    private fun removeStoredPallets(storage: Storage) {
        val palletsNotInStorage = storage.pallets_not_in_storage
        palletsNotInStorage.removeAll { it.isStored }
    }

    /**
     * Filters the current orders. Removes received orders,
     * @param orders current orders
     * @param roundNumber current round number
     * @returns filtered orders
     */
    private fun removeDeliveredOrders(orders: MutableList<Order>, roundNumber: Int) {
        orders.removeAll { it.deliveryRound < roundNumber }
    }

    /**
     * Filters the currently moved pallets. Removes all pallets that are loaded into trucks and shipped to the customer.
     * @param storage storage object of current state
     */
    private fun removeDeliveredPallets(storage: Storage) {
        val palletsNotInStorage = storage.pallets_not_in_storage
        palletsNotInStorage.removeAll { it.process.toInt() >= 5 }
    }

    /**
     * Filters the current customer jobs. Removes all jobs that are completed.
     * @param roundValues current round values
     * @param customerJobs current customer jobs
     * @returns filtered customer jobs
     */
    private fun removeFinishedJobs(roundValues: RoundValues, roundNumber: Int, customerJobs: MutableList<CustomerJob>) {
        roundValues.current_customerJobs = customerJobs.size
        for (job in customerJobs) {
            if (job.quantity == 0) {
                if (job.demandRound >= roundNumber - 1)
                    roundValues.accurate_finished_jobs += 1
                else
                    roundValues.late_finished_jobs += 1
            }
        }
        customerJobs.removeAll { it.quantity == 0 }
    }

    /**
     * Calculates the estimated incoming and outgoing pallets for the next round.
     * Currently, not used
     * @param roundValues current round values
     * @param orders current orders
     * @param customerJobs current customer jobs
     */
    private fun calculateEstimateConsumptionForNextRound(
        roundValues: RoundValues,
        roundNumber: Int,
        orders: MutableList<Order>,
        customerJobs: MutableList<CustomerJob>
    ) {
        val estimates: MutableMap<String, Int> = mutableMapOf()

        val newPallets = orders
            .filter { it.deliveryRound == roundNumber }
            .sumOf { it.deliveredQuantity }

        val toTransportEn = newPallets + roundValues.pallets_not_transported_process[0]
        val toTransportWv = toTransportEn + roundValues.pallets_not_transported_process[1]
        val toTransportLaIn = toTransportWv + roundValues.not_transported_pallets_la_in

        estimates["estimated_pallets_en"] = toTransportEn
        estimates["estimated_pallets_wv"] = toTransportWv
        estimates["estimated_pallets_la_in"] = toTransportLaIn

        val palletsForJobs = customerJobs.sumOf { it.quantity }

        val toTransportLaOut = palletsForJobs + roundValues.not_transported_pallets_la_out
        val toTransportWk = toTransportLaOut + roundValues.pallets_not_transported_process[3]
        val toTransportVe = toTransportWk + roundValues.pallets_not_transported_process[4]
        val toTransportLa = toTransportLaIn + toTransportLaOut

        estimates["estimated_pallets_la_out"] = toTransportLaOut
        estimates["estimated_pallets_wk"] = toTransportWk
        estimates["estimated_pallets_ve"] = toTransportVe
        estimates["estimated_pallets_la"] = toTransportLa
    }

    /**
     * Reset function to zero all roundValues that are summed throughout the calculations to avoid previous non-zero inits
     * @param roundValues current round values
     */
    private fun resetValues(roundValues: RoundValues) {
        roundValues.sales_income_article = Array(4) { 0.0 }
        roundValues.pallets_transported_process = Array(7) { 0 }
        roundValues.pallets_not_transported_process = Array(7) { 0 }
        roundValues.pallets_transported_la_in = 0
        roundValues.pallets_transported_la_out = 0
        roundValues.not_transported_pallets_la_in = 0
        roundValues.not_transported_pallets_la_out = 0
        roundValues.pallet_quantity_per_errors = Array(7) { 0 }
        roundValues.order_costs_article = Array(4) { 0.0 }
        roundValues.order_fix_costs_article = Array(4) { 0.0 }
        roundValues.accurate_finished_jobs = 0
        roundValues.late_finished_jobs = 0
        roundValues.accurate_delivered_pallets = 0
        roundValues.late_delivered_pallets = 0
        roundValues.crash_time_processes = Array(5) { 0 }
        roundValues.costs_usd = 0.0
        roundValues.current_order_costs = 0.0
    }

    /**
     * Main calculation function.
     * Processes the current state and all processes of the company.
     * Moves the pallet from arriving orders through to the outgoing customer jobs.
     * @param currentState current game state
     */
    fun calculate(currentState: GameState) {
        val roundValues = currentState.roundValues
        val storage = currentState.storage
        val messages = currentState.messages
        val orders = currentState.orders
        val articleDynamics = currentState.articleDynamics
        val customerJobs = currentState.customerJobs

        this.resetValues(roundValues)

        this.createMessagesForOrders(messages, orders, currentState.roundNumber)
        this.palletThroughputTruckToEn(roundValues, currentState.roundNumber, storage, orders, articleDynamics)
        this.removeDeliveredOrders(orders, currentState.roundNumber)
        this.palletThroughputEnToWv(roundValues, storage, articleDynamics)
        this.palletThroughputWvToLe(roundValues, storage, articleDynamics)
        this.palletThroughputLeToSt(roundValues, storage, articleDynamics)
        this.removeStoredPallets(storage)
        this.palletThroughputStToLa(roundValues, storage, customerJobs, articleDynamics)
        this.palletThroughputLaToVe(roundValues, storage, articleDynamics)
        this.palletThroughputVeToTruck(roundValues, currentState.roundNumber, storage, customerJobs, articleDynamics)
        this.removeDeliveredPallets(storage)
        this.removeFinishedJobs(roundValues, currentState.roundNumber, customerJobs)
    }

    /**
     * Informs the player of exhausted article stocks
     * @param currentState current game state
     */
    fun prepareNextRound(currentState: GameState) {
        val roundValues = currentState.roundValues

        val orders = currentState.orders
        val customerJobs = currentState.customerJobs
        calculateEstimateConsumptionForNextRound(roundValues, currentState.roundNumber, orders, customerJobs)

        val articleDynamics = currentState.articleDynamics
        for (dynamic in articleDynamics) {
            if (dynamic.pallet_count_processes[2] == 0) {
                val textDE = "Der Artikel ${dynamic.article.articleNumber} ist nicht mehr im Lager vorhanden."
                val textEN = "The Article ${dynamic.article.articleNumber} is out of stock."
                val message = MessageFactory.createMessage(textDE, textEN, currentState.roundNumber)
                currentState.messages.add(0, message)
            }
        }
    }
}