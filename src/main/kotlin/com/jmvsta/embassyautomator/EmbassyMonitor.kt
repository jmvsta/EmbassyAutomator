package com.jmvsta.embassyautomator

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class EmbassyMonitor(
    val telegramNotify: TelegramNotify,
    @Value("\${address1}") private val address1: String,
    @Value("\${address2}") private val address2: String,
    @Value("\${userEmail}") private val userEmail: String,
    @Value("\${userPassword}") private val userPassword: String,
    @Value("\${message}") private val message: String,
) {

    private val logger = LoggerFactory.getLogger(EmbassyMonitor::class.java)
    private val driver: WebDriver = ChromeDriver(ChromeOptions().addArguments("--headless=chrome"))

    @PostConstruct
    fun init() {
        driver.get("https://prenotami.esteri.it/Home")
        val email = driver.findElement(By.id("login-email"))
        email.sendKeys(userEmail)
        val password = driver.findElement(By.id("login-password"))
        password.sendKeys(userPassword)
        driver.findElement(By.id("login-form")).submit()
    }

    @Scheduled(fixedRate = 300000)
    fun monitor() {
        driver.get(address1)
        logger.info("PAGE1 ${driver.pageSource}")
        if (!driver.pageSource.contains(message)) {
            logger.info("Time to book on $address1")
            telegramNotify.sendNotification("Time to book on $address1")
        } else {
            logger.info("No bookings")
        }
        driver.get(address2)
        logger.info("PAGE2 ${driver.pageSource}")
        if (!driver.pageSource.contains(message)) {
            logger.info("Place some logic $address2")
            telegramNotify.sendNotification("Time to book on $address2")
        } else {
            logger.info("No bookings")
        }
    }

    @Scheduled(fixedRate = 3600000)
    fun healthNotifyTask() {
        telegramNotify.sendNotification("I'm alive, no news")
    }

    @PreDestroy
    fun shutdown() {
        driver.close()
    }

}
