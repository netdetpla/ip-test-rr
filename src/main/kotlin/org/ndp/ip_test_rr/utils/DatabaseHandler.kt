package org.ndp.ip_test_rr.utils

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.batchUpdate
import me.liuwj.ktorm.dsl.*
import org.ndp.ip_test_rr.bean.BatchInsertIP
import org.ndp.ip_test_rr.bean.Task
import org.ndp.ip_test_rr.utils.Logger.logger
import org.ndp.ip_test_rr.table.Task as TableTask
import org.ndp.ip_test_rr.table.IP as TableIP

object DatabaseHandler {
    private val dbUrl = Settings.setting["dbUrl"] as String
    private val dbDriver = Settings.setting["dbDriver"] as String
    private val dbUser = Settings.setting["dbUser"] as String
    private val dbPassword = Settings.setting["dbPassword"] as String
    private val database: Database


    init {
        database = Database.Companion.connect(
            dbUrl,
            dbDriver,
            dbUser,
            dbPassword
        )
    }

    fun findIP(ipInt: Long): Boolean {
        val id = TableIP.select(TableIP.id)
            .where { TableIP.id eq ipInt.toInt() }
            .map { it[TableIP.id]!! }
        return id.isNotEmpty()
    }

    fun findGeoID(ipInt: Long): Int {
        val geoID = database.useConnection { conn ->
            val sql = "select `geoname_id` from `GeoLite2-City-Blocks-IPv4` " +
                    "use index (`geolite2-city-blocks-ipv4_long_ip_start_index`," +
                    "`geolite2-city-blocks-ipv4_long_ip_end_index`) " +
                    "where `long_ip_start` <= ? and `long_ip_end` >= ?"

            conn.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, ipInt)
                stmt.setLong(2, ipInt)

                stmt.executeQuery().iterable().map { it.getInt(1) }
            }
        }
        return if (geoID.isEmpty()) {
            -1
        } else {
            geoID[0]
        }
    }

    fun batchUpdateTaskStatus(updateTasks: List<Task>) {
        logger.debug("task size: ${updateTasks.size}")
        TableTask.batchUpdate {
            for (task in updateTasks) {
                item {
                    it.taskStatus to task.status
                    it.desc to task.desc
                    where {
                        TableTask.id eq task.id
                    }
                }
            }
        }
    }

    fun batchInsertIP(insertIPs: List<BatchInsertIP>) {
        logger.debug("ip size: ${insertIPs.size}")
        TableIP.batchInsert {
            for (i in insertIPs) {
                item {
                    it.id to i.id
                    it.lnglatID to i.geoID
                    it.ip to i.ip
                    it.elapsedTime to i.elapsedTime
                    it.ipTestFlag to 1
                }
            }
        }
    }
}