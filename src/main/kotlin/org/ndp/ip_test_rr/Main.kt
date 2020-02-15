package org.ndp.ip_test_rr

import org.ndp.ip_test_rr.bean.BatchInsertIP
import org.ndp.ip_test_rr.bean.Task
import org.ndp.ip_test_rr.utils.DatabaseHandler
import org.ndp.ip_test_rr.utils.Logger.logger
import org.ndp.ip_test_rr.utils.OtherTools
import org.ndp.ip_test_rr.utils.RedisHandler

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("start result recycling...")
        val results = RedisHandler.consumeResult(RedisHandler.generateNonce(5))
        RedisHandler.returnACK()
        val updateTasks = ArrayList<Task>()
        val insertIPs = ArrayList<BatchInsertIP>()
        for (r in results) {
            // task status update
            if (r.status == 1) {
                updateTasks.add(Task(r.taskID, 21000, r.desc))
                continue
            }
            updateTasks.add(Task(r.taskID, 20030, ""))
            // ip
            for (ip in r.result) {
                val ipInt = OtherTools.iNetString2Number(ip.ip)
                if (DatabaseHandler.findIP(ipInt)) continue
                insertIPs.add(
                    BatchInsertIP(
                        ipInt,
                        ip.ip,
                        ip.elapsedTime,
                        DatabaseHandler.findGeoID(ipInt)
                    )
                )
            }
        }
        DatabaseHandler.batchInsertIP(insertIPs)
        DatabaseHandler.batchUpdateTaskStatus(updateTasks)
    }
}