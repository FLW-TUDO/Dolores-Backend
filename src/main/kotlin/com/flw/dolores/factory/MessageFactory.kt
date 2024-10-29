package com.flw.dolores.factory

import com.flw.dolores.entities.Message
import com.google.gson.Gson
import org.bson.types.ObjectId
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.BufferedReader


class MessageFactory {
    private val resource: Resource = ClassPathResource("entity_base/base_message.json")
    private val messageBaseFile: BufferedReader = BufferedReader(resource.inputStream.reader(Charsets.UTF_8))
    private val jsonString: String = messageBaseFile.readText()
    private val messageList: MutableList<Message> = Gson().fromJson(jsonString)

    fun loadDynamics(): MutableList<Message> {
        val messages: MutableList<Message> = mutableListOf()
        for (message: Message in messageList) {
            val newMessage: Message = Message(
                id = ObjectId.get(),
                messageDE = message.messageDE,
                messageEN = message.messageEN,
                roundNumber = message.roundNumber,
            )
            messages.add(newMessage)
        }
        return messages
    }

    companion object {
        fun createMessage(textDE: String, textEN: String, roundNumber: Int): Message {
            return Message(
                messageDE = textDE,
                messageEN = textEN,
                roundNumber = roundNumber
            )
        }
    }
}