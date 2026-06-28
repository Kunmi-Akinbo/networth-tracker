package com.networth.api.config

import org.quartz.Scheduler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.quartz.SchedulerFactoryBean

@Configuration
@EnableScheduling
class SchedulerConfig {

    @Bean
    fun schedulerFactoryBean(): SchedulerFactoryBean {
        val factory = SchedulerFactoryBean()
        factory.setSchedulerName("networth-scheduler")
        factory.setWaitForJobsToCompleteOnShutdown(true)
        factory.setOverwriteExistingJobs(true)
        factory.setAutoStartup(true)
        return factory
    }

    @Bean
    fun scheduler(factory: SchedulerFactoryBean): Scheduler {
        return factory.scheduler
    }
}
