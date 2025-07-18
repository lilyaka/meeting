package com.revotech

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@EnableFeignClients
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
class MeetingServiceApplication

fun main(args: Array<String>) {
    runApplication<MeetingServiceApplication>(*args)
}
