package com.flw.dolores.entities

import com.google.gson.annotations.SerializedName

enum class Process(val value: Int) {
    @SerializedName("-1")
    ERROR(-1),

    @SerializedName("0")
    UNLOADING(0),

    @SerializedName("1")
    COLLECTION(1),

    @SerializedName("2")
    STORAGE(2),

    @SerializedName("3")
    CONTROL(3),

    @SerializedName("4")
    LOADING(4),

    @SerializedName("5")
    DONE(5);

    fun toInt(): Int = this.value


    /**
     * Checks if the current process uses conveyors
     * @returns usage of conveyors in process
     */
    fun isProcessWithConveyors(): Boolean = this.value in listOf(0, 2, 4)
    fun copy(): Process = fromInt(this.value)

    override fun toString(): String {
        return "$value"
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}