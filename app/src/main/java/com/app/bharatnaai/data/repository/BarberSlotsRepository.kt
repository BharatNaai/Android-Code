package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.model.BarberSlotsAvailableResponse
import com.app.bharatnaai.data.model.BarberSlotsAvailableResponseRaw
import com.app.bharatnaai.data.model.BookingSlot
import com.app.bharatnaai.data.model.Slot
import com.app.bharatnaai.data.model.SlotBookingRequest
import com.app.bharatnaai.data.network.ApiService
import com.app.bharatnaai.utils.CommonMethod

class BarberSlotsRepository(
    private val context: Context,
    private val apiService: ApiService
) {
    private val commonMethod = CommonMethod()

    suspend fun getAvailableSlots(barberId: Int, date: String, serviceType: String): BarberSlotsAvailableResponse? {
        if (!commonMethod.isInternetAvailable(context)) {
            throw Exception("No internet connection")
        }
        val response = apiService.getAvailableSlots(barberId, date, serviceType)
        if (!response.isSuccessful) return null

        val raw: BarberSlotsAvailableResponseRaw = response.body() ?: return null
        val mapped = mutableListOf<Slot>()
        raw.slots.forEach { item ->
            if (serviceType.equals("COMBO", ignoreCase = true)) {
                val haircut = item.getAsJsonObject("haircutSlot")
                val beard = item.getAsJsonObject("beardSlot")
                val status = item.get("status")?.asString ?: "AVAILABLE"
                if (haircut != null && beard != null) {
                    val hcId = haircut.get("slotId").asInt
                    val bdId = beard.get("slotId").asInt
                    val dateStr = haircut.get("slotDate").asString
                    val start = haircut.get("startTime").asString
                    val end = beard.get("endTime").asString
                    mapped.add(
                        Slot(
                            id = hcId,
                            serviceType = "COMBO",
                            slotDate = dateStr,
                            startTime = start,
                            endTime = end,
                            status = status,
                            secondaryId = bdId
                        )
                    )
                }
            } else {
                val id = item.get("slotId").asInt
                val stype = item.get("serviceType").asString
                val dateStr = item.get("slotDate").asString
                val start = item.get("startTime").asString
                val end = item.get("endTime").asString
                val status = item.get("status").asString
                mapped.add(
                    Slot(
                        id = id,
                        serviceType = stype,
                        slotDate = dateStr,
                        startTime = start,
                        endTime = end,
                        status = status
                    )
                )
            }
        }

        return BarberSlotsAvailableResponse(
            totalSlots = raw.totalSlots,
            slots = mapped,
            success = raw.success,
            barberId = raw.barberId,
            serviceType = raw.serviceType,
            date = raw.date
        )
    }

    suspend fun bookSlot(customerId: Long, slotIds: List<Int>): BookingSlot? {
        if (!commonMethod.isInternetAvailable(context)) {
            throw Exception("No internet connection")
        }
        val response = apiService.bookSlot(
            SlotBookingRequest(
                customerId = customerId,
                slotIds = slotIds
            )
        )
        return if (response.isSuccessful) response.body() else null
    }
}
