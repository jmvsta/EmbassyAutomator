package com.jmvsta.embassyautomator

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class TelegramNotify(@Value("\${apiToken}") private val apiToken: String) {

    private val logger = LoggerFactory.getLogger(TelegramNotify::class.java)
    private val chatList: MutableSet<Long> = mutableSetOf(34502385)

    val bot = bot {
        token = apiToken
        dispatch {
            command("start") {
                val result = bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id), text = "Bot started")

                result.fold(
                    {
                        chatList.add(update.message!!.chat.id)
                        logger.info("Success")
                    },
                    {
                        logger.error("Error starting bot")
                    }
                )
            }
        }
    }

    @PostConstruct
    fun init() {
        bot.startPolling()
    }

    fun sendNotification(text: String) {
        chatList.forEach { chatId ->
            run {
                bot.sendMessage(ChatId.fromId(chatId), text = text).fold(
                    {
                        logger.info("Successfully sent notification: {}", text)
                    },
                    {
                        logger.error("Error sending notification: {}", it.get())
                    }
                )
            }
        }
    }

}
