package com.fornadagora.notification

data class PushNotification(
        val data: NotificationData,
        val to: String
)