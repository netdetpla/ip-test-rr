package org.ndp.ip_test_rr.bean

data class BatchInsertIP(
    val id: Long,
    val ip: String,
    val elapsedTime: Int,
    val geoID: Int
)