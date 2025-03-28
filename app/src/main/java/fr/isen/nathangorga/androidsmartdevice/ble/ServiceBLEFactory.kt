package fr.isen.nathangorga.androidsmartdevice.ble

import fr.isen.nathangorga.androidsmartdevice.ServiceBLE

object ServiceBLEFactory {
    private var instance: ServiceBLE? = null

    fun getServiceBLEInstance(): ServiceBLE {
        if (instance == null) {
            instance = ServiceBLE()
        }
        return instance!!
    }
}