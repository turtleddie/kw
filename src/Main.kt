import javax.bluetooth.DiscoveryAgent
import javax.bluetooth.LocalDevice
import javax.bluetooth.RemoteDevice
import javax.bluetooth.DiscoveryListener
import javax.bluetooth.DeviceClass
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class BluetoothScan : DiscoveryListener {

    private val discoveredDevices = mutableSetOf<RemoteDevice>()
    private val client = OkHttpClient()

    override fun deviceDiscovered(btDevice: RemoteDevice, cod: DeviceClass) {
        println("Device discovered: ${btDevice.bluetoothAddress}")
        discoveredDevices.add(btDevice)
    }

    override fun servicesDiscovered(transID: Int, servRecord: Array<out javax.bluetooth.ServiceRecord>?) {
        // Not used in this example
    }

    override fun serviceSearchCompleted(transID: Int, respCode: Int) {
        // Not used in this example
    }

    override fun inquiryCompleted(discType: Int) {
        println("Device inquiry completed!")
        println("Discovered devices:")
        discoveredDevices.forEach {
            println(it.bluetoothAddress)
            sendAttendanceData(it.bluetoothAddress)
        }
    }

    fun startScan() {
        val localDevice = LocalDevice.getLocalDevice()
        val agent = localDevice.discoveryAgent

        println("Starting device inquiry...")
        agent.startInquiry(DiscoveryAgent.GIAC, this)
    }

    private fun sendAttendanceData(beaconData: String) {
        val json = JSONObject()
        json.put("user_id", "12345")
        json.put("beacon_data", beaconData)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://yourserver.com/scan")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Failed to send attendance data: ${response.message}")
            } else {
                println("Successfully sent attendance data for $beaconData")
            }
        }
    }
}

fun main() {
    val scanner = BluetoothScan()
    scanner.startScan()

    // 스캔이 완료될 때까지 프로그램을 종료하지 않도록 대기
    Thread.sleep(20000)
    println("Scanning complete.")
}
