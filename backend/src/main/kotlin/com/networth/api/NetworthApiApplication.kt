package com.networth.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class NetworthApiApplication

fun main(args: Array<String>) {
    runApplication<NetworthApiApplication>(*args)
}
