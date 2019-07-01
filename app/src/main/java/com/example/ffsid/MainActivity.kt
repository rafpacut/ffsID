package com.example.ffsid

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import java.lang.StringBuilder
import Prover
import Verifier
import java.io.IOException
import kotlin.random.Random


class MainActivity : AppCompatActivity(), DevicesRecyclerViewAdapter.ItemClickListener, ChatFragment.CommunicationListener {
    private val REQUEST_ENABLE_BT = 123

    private val TAG = "FFSID MAIN ACTIVITY"
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewPaired: RecyclerView
    private val mDeviceList = arrayListOf<DeviceData>()
    private lateinit var devicesAdapter: DevicesRecyclerViewAdapter
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val PERMISSION_REQUEST_LOCATION = 123
    private val PERMISSION_REQUEST_LOCATION_KEY = "PERMISSION_REQUEST_LOCATION"
    private var alreadyAskedForPermission = false

    private lateinit var headerLabel: TextView
    private lateinit var headerLabelPaired: TextView
    private lateinit var headerLabelContainer: LinearLayout
    private lateinit var status: TextView
    private lateinit var connectionDot: ImageView

    private var connected: Boolean = false
    private lateinit var mConnectedDeviceName: String
    
    private var mBluetoothChatService: BluetoothChatService? = null
    private lateinit var chatFragment: ChatFragment

    private lateinit var arrayToSendInString: String
    private var transmissionState: Int = Constants.STATE_DEFAULT


    class ProtocolHandler(userRole: Int, mainActivity: MainActivity) {
        private var mActivity: MainActivity
        private var userRole: Int = Constants.USER_ROLE_NONE
        private var protocolState: Int = Constants.PROTOCOL_END
        private val securityParam = 5
        private var name: String = ""
//        private var prover = Prover(securityParam)
//        private var verifier = Verifier(securityParam)
//        private lateinit var receivedList: List<Int>

        private var roundsNumber : Int = 0
        private var roundCounter : Int = 0

        init {
            this.mActivity = mainActivity
            this.userRole = userRole
            if(userRole.equals(Constants.USER_ROLE_VERIFIER)) {

                roundsNumber = Random.nextInt(1,10) // Generate numbers of rounds for the protocol
                mActivity.sendInt(roundsNumber)
//                nextState("")
                this.protocolState = Constants.VERIFIER_AWAIT_INTRO
            } else if(userRole.equals(Constants.USER_ROLE_PROVER)) {
                mActivity.sendInt(Constants.PROTOCOL_INIT)
                this.protocolState = Constants.PROTOCOL_INIT
            }

        }


        fun nextState(msg: String) {
            when (userRole) {
                Constants.USER_ROLE_VERIFIER -> {
                    nextVerifierState(msg)
                }

                Constants.USER_ROLE_PROVER -> {
                    nextProverState(msg)
                }
            }
        }

        private fun nextProverState(stageMessage: String) {
            when(protocolState) {
                Constants.PROTOCOL_INIT -> {
                    Log.i("NEXT_STATE","STATE_AFTER_PROTOCOL_INIT")
                    roundsNumber = stageMessage.toInt()
//                    val (introduction, signature) = prover.getIntroduction()
//                    val publicKeyString = listToString(introduction.publicKey)


                    name = "hello world"
                    val publicKey = List<Long>(5) {Random.nextLong(1000,101010101)}
                    val publicKeyString = listLongToString(publicKey)
                    var signature = "abcdef".toByteArray()

                    val stringBuilder = StringBuilder()
                    stringBuilder.append(name).append(Constants.PACKET_SEPARATOR)
                    stringBuilder.append(publicKeyString).append(Constants.PACKET_SEPARATOR)
                    stringBuilder.append(signature.toString())

                    mActivity.sendMessage(stringBuilder.toString())
                    protocolState = Constants.PROVER_AWAIT_INTRODUCTION_CONFIRMATION
                }

                Constants.PROVER_AWAIT_INTRODUCTION_CONFIRMATION -> {
                    mActivity.sendMessage("AWAIT_CHALLENGE")
                    protocolState = Constants.PROVER_AWAIT_CHALLENGE
                }

                Constants.PROVER_AWAIT_CHALLENGE -> {
                    //fetch challenge
                    //stageMessage to List<Int>
                    val recChallenge = stringToListInt(stageMessage)
                    Log.i("RECEIVED_CHALLENGE",recChallenge.get(0).toString())
                    //prover.receivedChallenge = recChallenge
                    mActivity.sendMessage("RECEIVED")
                    protocolState = Constants.PROVER_GEN_X
                }

                Constants.PROVER_GEN_X -> {
                    //gen x and send to verifier
                    // val x = prover.genX()
                    val x = Random.nextLong(0,100000)
                    mActivity.sendMessage(x.toString())
                    protocolState = Constants.PROVER_CALC_Y
                }

                Constants.PROVER_CALC_Y -> {
                    //calcY and send to verifier
                    val y = Random.nextLong(0,100000)
                    //val y = prover.calcY()
                    mActivity.sendMessage(y.toString())
                    protocolState = Constants.PROVER_AWAIT_VERIFICATION
                }

                Constants.PROVER_AWAIT_VERIFICATION -> {
                    //get the verification results
                    val verifyResult = stageMessage.split("_").get(0)

                    if(verifyResult.equals("APPROVED")) {
                        Log.i("PROVER_VERIFICATION","APPROVED")
                    } else {
                        Log.i("PROVER_VERIFICATION","REJECTED")
                    }

                    if(roundCounter <= roundsNumber) {
                        roundCounter++
                        mActivity.sendMessage("NEXT_ROUND_${roundCounter}")
                        protocolState = Constants.PROVER_AWAIT_CHALLENGE
                    } else {
                        val milliSecondsTime = System.currentTimeMillis()
                        mActivity.chatFragment.communicate(Message("NAME: ${name}",milliSecondsTime,Constants.MESSAGE_TYPE_SENT))
                        protocolState = Constants.PROTOCOL_END
                    }
                }
            }
        }

        private fun nextVerifierState(stageMessage: String) {
            when(protocolState) {
                Constants.PROTOCOL_INIT -> {
                    Log.i("NEXT_STATE","STATE_AFTER_PROTOCOL_INIT")

                    protocolState = Constants.VERIFIER_AWAIT_INTRO
                }

                Constants.VERIFIER_AWAIT_INTRO -> {
                    Log.i("NEXT_STATE","GET_INTRO")
                    //message to name : String, publicKey : List<Long>
                    //introductionSignature : ByteArray
                    val unpackedMessage = stageMessage.split(Constants.PACKET_SEPARATOR)
                    name = unpackedMessage.get(0)
                    val publicKey = stringToListLong(unpackedMessage.get(1))
                    val introductionSignature = unpackedMessage.get(2).toByteArray()

                    Log.i("VERIFIER_NAME",name)
                    Log.i("VERIFIER_PUBLIC_KEY",publicKey.size.toString())
                    Log.i("PROVER_SIGNATURE",introductionSignature.toString())

//                    verifier.fetchIntroduction(Introduction(name, publicKey), introductionSignature)
                    mActivity.sendMessage("RECEIVED")
                    protocolState = Constants.VERIFIER_CHALLENGE
                }

                Constants.VERIFIER_CHALLENGE -> {
                    //verifier gen challlenge
                    val challenge = List<Int>(5){Random.nextInt(1,100)}
//                    val challenge = verifier.genChallenge()
                    var stringChallenge = listIntToString(challenge)
                    mActivity.sendMessage(stringChallenge)
                    protocolState = Constants.VERIFIER_AWAIT_CONFIRM
                }

                Constants.VERIFIER_AWAIT_CONFIRM -> {
                    mActivity.sendMessage("AWAIT_FOR_X")
                    protocolState = Constants.VERIFIER_AWAIT_X
                }

                Constants.VERIFIER_AWAIT_X -> {
                    //verifier fetch x

                    val x = stageMessage.toLong()
//                    verifier.receivedX = X
                    mActivity.sendMessage("RECEIVED")
                    protocolState = Constants.VERIFIER_AWAIT_Y
                }

                Constants.VERIFIER_AWAIT_Y -> {
                    //verifier fetch y and verify

                    val y = stageMessage.toLong()

                    if(roundCounter%2 == 0) {
                        mActivity.sendMessage("APPROVED_${roundCounter}")
                    } else {
                        mActivity.sendMessage("REJECTED_${roundCounter}")
                    }

                    if(roundCounter <= roundsNumber) {
                        roundCounter++
                        protocolState = Constants.VERIFIER_CHALLENGE
                    } else {
                        val milliSecondsTime = System.currentTimeMillis()
                        mActivity.chatFragment.communicate(Message("NAME: ${name}",milliSecondsTime,Constants.MESSAGE_TYPE_SENT))
                        protocolState = Constants.PROTOCOL_END
                    }
//                    verifier.receivedY = Y
//                    if(!verifier.verify())
//                        mActivity.sendMessage("REJECTED")
                    //else if (ctr == roundsNumber)
                    //sendMessage("Accepted")

                }
            }
        }

        fun stringToListLong(recString: String): List<Long> {
            var splittedString = recString.split(Constants.ARRAY_SEPARATOR)
            return List<Long>(splittedString.size) {i -> splittedString[i].toLong()}
        }

        fun stringToListInt(recString: String): List<Int> {
            var splittedString = recString.split(Constants.ARRAY_SEPARATOR)
            return List<Int>(splittedString.size) {i -> splittedString[i].toInt()}
        }

        fun listIntToString(list: List<Int>): String {
            val stringBuilder = StringBuilder()
            var element: Any
            for(i in 0..list.size-2) {
                element = list.get(i)
                stringBuilder.append(element).append(Constants.ARRAY_SEPARATOR)
            }

            element = list.get(list.size-1)
            stringBuilder.append(element)

            return stringBuilder.toString()
        }

        fun listLongToString(list: List<Long>): String {
            val stringBuilder = StringBuilder()
            var element: Any
            for(i in 0..list.size-2) {
                element = list.get(i)
                stringBuilder.append(element).append(Constants.ARRAY_SEPARATOR)
            }

            element = list.get(list.size-1)
            stringBuilder.append(element)

            return stringBuilder.toString()
        }

    }


    private var protocolHandler: ProtocolHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerViewPaired = findViewById(R.id.recyclerViewPaired)
        headerLabel = findViewById(R.id.headerLabel)
        headerLabelPaired = findViewById(R.id.headerLabelPaired)
        headerLabelContainer = findViewById(R.id.headerLabelContainer)
        status = findViewById(R.id.status)
        connectionDot = findViewById(R.id.connectionDot)

        status.text = getString(R.string.bluetooth_not_enabled)

        headerLabelContainer.visibility = View.INVISIBLE

        if (savedInstanceState != null)
            alreadyAskedForPermission = savedInstanceState.getBoolean(PERMISSION_REQUEST_LOCATION_KEY,false)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerViewPaired.layoutManager = LinearLayoutManager(this)

        recyclerView.isNestedScrollingEnabled = false
        recyclerViewPaired.isNestedScrollingEnabled = false

        findViewById<Button>(R.id.search_devices).setOnClickListener {
            findDevices()
        }

        findViewById<Button>(R.id.make_visible).setOnClickListener {
            makeVisible()
        }

        devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mDeviceList)
        recyclerView.adapter = devicesAdapter
        devicesAdapter.setItemClickListener(this)

        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver,filter)

        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        mBluetoothChatService = BluetoothChatService(this,mHandler)

        if(mBluetoothAdapter == null) {
            showAlertAndExit()
        } else {
            if(mBluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                status.text = getString(R.string.not_connected)
            }

            val pairedDevices = mBluetoothAdapter?.bondedDevices
            val mPairedDeviceList = arrayListOf<DeviceData>()

            if(pairedDevices?.size ?: 0 > 0) {
                for (device in pairedDevices!!) {
                    val deviceName = device.name
                    val deviceAddress = device.address
                    mPairedDeviceList.add(DeviceData(deviceName,deviceAddress))
                }

                val devicesAdapter = DevicesRecyclerViewAdapter(context = this, mDeviceList = mPairedDeviceList)
                recyclerViewPaired.adapter = devicesAdapter
                devicesAdapter.setItemClickListener(this)
                headerLabelPaired.visibility = View.VISIBLE
            }
        }
    }

    private fun makeVisible() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300)
        startActivity(discoverableIntent)
    }

    private fun checkPermissions() {
        if(alreadyAskedForPermission) {
            return
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                var builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.need_loc_access))
                builder.setMessage(getString(R.string.please_grant_loc_access))
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    alreadyAskedForPermission = true
                    requestPermissions(arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), PERMISSION_REQUEST_LOCATION)
                }

                builder.show()
            } else {
                startDiscovery()
            }
        } else {
            startDiscovery()
            alreadyAskedForPermission = true
        }
    }

    private fun showAlertAndExit() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.not_compatible))
            .setMessage(getString(R.string.no_support))
            .setPositiveButton("Exit", { _, _ -> System.exit(0)})
            .show()
    }

    private fun findDevices() {
        checkPermissions()
    }

    private fun startDiscovery() {
        mDeviceList.clear()

        if(mBluetoothAdapter?.isDiscovering ?: false) {
            mBluetoothAdapter?.cancelDiscovery()
        }

        mBluetoothAdapter?.startDiscovery()
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if(BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device.name
                val deviceAddress = device.address

                val deviceData = DeviceData(deviceName, deviceAddress)
                mDeviceList.add(deviceData)

                val setList = HashSet<DeviceData>(mDeviceList)
                mDeviceList.clear()
                mDeviceList.addAll(setList)

                devicesAdapter.notifyDataSetChanged()
            }

            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                // finished action
                Log.i("BT_RECEIVER","DISCOVERY FINISHED")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        progressBar.visibility = View.INVISIBLE

        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            status.text = getString(R.string.not_connected)

            val pairedDevices = mBluetoothAdapter?.bondedDevices
            val mPairedDeviceList = arrayListOf<DeviceData>()

            mPairedDeviceList.clear()

            if(pairedDevices?.size ?: 0 > 0) {
                for (device in pairedDevices!!) {
                    val deviceName = device.name
                    val deviceAddress = device.address
                    mPairedDeviceList.add(DeviceData(deviceName,deviceAddress))
                }

                val devicesAdapter = DevicesRecyclerViewAdapter(context=this,mDeviceList = mPairedDeviceList)
                recyclerViewPaired.adapter = devicesAdapter
                devicesAdapter.setItemClickListener(this)
                headerLabelPaired.visibility = View.VISIBLE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PERMISSION_REQUEST_LOCATION_KEY,alreadyAskedForPermission)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                alreadyAskedForPermission = false
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery()
                } else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(getString(R.string.fun_limited))
                        builder.setMessage(getString(R.string.since_perm_not_granted))
                        builder.setPositiveButton(android.R.string.ok,null)
                        builder.show()
                    }
                }
            }

        }
    }

    override fun itemClicked(deviceData: DeviceData) {
        connectDevice(deviceData)
    }

    private fun connectDevice(deviceData: DeviceData) {
        mBluetoothAdapter?.cancelDiscovery()
        val deviceAddress = deviceData.deviceAddress
        val device = mBluetoothAdapter?.getRemoteDevice(deviceAddress)

        status.text = getString(R.string.connecting)
        connectionDot.setImageDrawable(getDrawable(R.mipmap.ic_circle_connecting))

        mBluetoothChatService?.connect(device,true)
    }

    override fun onResume() {
        super.onResume()
        if(mBluetoothChatService != null) {
            if(mBluetoothChatService?.getState() == BluetoothChatService.STATE_NONE) {
                mBluetoothChatService?.start()
            }
        }

        if(connected)
            showChatFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    fun initProtocol(userRole: Int) {
        if(protocolHandler == null) {
            protocolHandler = MainActivity.ProtocolHandler(userRole,this)
        }
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {

            when (msg.what) {

                Constants.MESSAGE_STATE_CHANGE -> {

                    when (msg.arg1) {

                        BluetoothChatService.STATE_CONNECTED -> {

                            status.text = getString(R.string.connected_to) + " "+ mConnectedDeviceName
//                            connectionDot.setImageDrawable(getDrawable(R.drawable.ic_circle_connected))
//                            Snackbar.make(findViewById(R.id.mainScreen),"Connected to " + mConnectedDeviceName,Snackbar.LENGTH_SHORT).show()
                            //mConversationArrayAdapter.clear()
                            connected = true
                        }

                        BluetoothChatService.STATE_CONNECTING -> {
                            status.text = getString(R.string.connecting)
                            connectionDot.setImageDrawable(getDrawable(R.mipmap.ic_circle_connecting))
                            connected = false
                        }

                        BluetoothChatService.STATE_LISTEN, BluetoothChatService.STATE_NONE -> {
                            status.text = getString(R.string.not_connected)
                            connectionDot.setImageDrawable(getDrawable(R.mipmap.ic_circle_red))
                            Snackbar.make(findViewById(R.id.mainScreen),getString(R.string.not_connected),Snackbar.LENGTH_SHORT).show()
                            connected = false
                        }
                    }
                }

                Constants.MESSAGE_WRITE -> {
                        val writeBuf = msg.obj as ByteArray
                        // construct a string from the buffer
                        val writeMessage = String(writeBuf)
                        //Toast.makeText(this@MainActivity,"Me: $writeMessage",Toast.LENGTH_SHORT).show()
                        //mConversationArrayAdapter.add("Me:  " + writeMessage)
                        val milliSecondsTime = System.currentTimeMillis()
                        chatFragment.communicate(Message(writeMessage, milliSecondsTime, Constants.MESSAGE_TYPE_SENT))

                }

                Constants.MESSAGE_WRITE_INT -> {
                    val milliSecondsTime = System.currentTimeMillis()
                    if(msg.obj == Constants.MESSAGE_RECEIVED) {
                        var message = Constants.RECEIVED
                        chatFragment.communicate(Message(message,milliSecondsTime,Constants.MESSAGE_TYPE_SENT))
                    } else {
                        chatFragment.communicate(
                            Message(
                                msg.obj.toString(),
                                milliSecondsTime,
                                Constants.MESSAGE_TYPE_SENT
                            )
                        )
                    }
                }

                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    var readMessageInt = -1
                    try {
                        readMessageInt = readMessage.toInt()
                    } catch (e: Exception) { }

                    val milliSecondsTime = System.currentTimeMillis()
                    if(protocolHandler != null) {

                        chatFragment.communicate(Message("Received message:\n$readMessage", milliSecondsTime, Constants.MESSAGE_TYPE_RECEIVED))
                        protocolHandler!!.nextState(readMessage)
                    } else if(readMessageInt == Constants.PROTOCOL_INIT) {
                        chatFragment.communicate(Message("Received message:\n$readMessage", milliSecondsTime, Constants.MESSAGE_TYPE_RECEIVED))
                        initProtocol(Constants.USER_ROLE_VERIFIER)
                    } else {

                        //Toast.makeText(this@MainActivity,"$mConnectedDeviceName : $readMessage",Toast.LENGTH_SHORT).show()
                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage)
                        chatFragment.communicate(
                            Message(
                                readMessage,
                                milliSecondsTime,
                                Constants.MESSAGE_TYPE_RECEIVED
                            )
                        )
                    }
                }

                Constants.MESSAGE_RECEIVED -> {
                    val milliSecondsTime = System.currentTimeMillis()
                    chatFragment.communicate(Message(Constants.RECEIVED,milliSecondsTime,Constants.MESSAGE_TYPE_RECEIVED))
                    protocolHandler?.nextState("")
                }

                Constants.PROTOCOL_INIT -> {
                    initProtocol(Constants.USER_ROLE_VERIFIER)
                    val milliSecondsTime = System.currentTimeMillis()
                    chatFragment.communicate(Message("Protocol has been initailized",milliSecondsTime,Constants.MESSAGE_TYPE_RECEIVED))

                }

                Constants.SEND_ARRAY_INIT -> {
                    val milliSecondsTime = System.currentTimeMillis()
                    chatFragment.communicate(Message("Init info about sending arraylist",milliSecondsTime,Constants.MESSAGE_TYPE_RECEIVED))
                }

                Constants.WAIT_FOR_ARRAY -> {
                    transmissionState = Constants.WAIT_FOR_ARRAY
                    val milliSecondsTime = System.currentTimeMillis()
                    chatFragment.communicate(Message("Waiting for an array",milliSecondsTime,Constants.MESSAGE_TYPE_RECEIVED))
                    sendInt(Constants.MESSAGE_RECEIVED)

                }

                Constants.MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                    status.text = getString(R.string.connected_to) + " " +mConnectedDeviceName
                    connectionDot.setImageDrawable(getDrawable(R.mipmap.ic_circle_connected))
                    Snackbar.make(findViewById(R.id.mainScreen),"Connected to " + mConnectedDeviceName,Snackbar.LENGTH_SHORT).show()
                    connected = true
                    showChatFragment()
                }

                Constants.MESSAGE_TOAST -> {
                    status.text = getString(R.string.not_connected)
                    connectionDot.setImageDrawable(getDrawable(R.mipmap.ic_circle_red))
                    Snackbar.make(findViewById(R.id.mainScreen),msg.data.getString(Constants.TOAST),Snackbar.LENGTH_SHORT).show()
                    connected = false
                }
            }
        }
    }

    private fun arrayListToString(arrayList: List<Int>): String {
        val stringBuilder = StringBuilder()
        var element: Int
        for(i in 0..arrayList.size-2) {
            element = arrayList.get(i)
           stringBuilder.append(element).append(Constants.ARRAY_SEPARATOR)
        }

        element = arrayList.get(arrayList.size-1)
        stringBuilder.append(element)

        return stringBuilder.toString()
    }

    private fun sendArrayList(outArrayList: List<Int>) {
        if(isStateConnected()) {
            arrayToSendInString = arrayListToString(outArrayList)
            sendMessage(arrayToSendInString)
//            transmissionState = Constants.SEND_ARRAY_INIT

        }
    }

    private fun sendByteArray(byteArray: ByteArray) {
        if(isStateConnected()) {
//            mBluetoothChatService?.write(byteArray)
            sendMessage(String(byteArray))
        }

    }

    private fun sendInt(intMessage: Int) {
        if(isStateConnected()) {
//            mBluetoothChatService?.writeInt(intMessage)
            sendMessage(intMessage.toString())
        }
    }

    private fun sendMessage(message: String) {

        if(isStateConnected() && message.isNotEmpty()) {
            val send = message.toByteArray()
            mBluetoothChatService?.write(send)
        }
    }

    private fun isStateConnected(): Boolean {
        if(mBluetoothChatService?.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return false
        }

        return true
    }

    private fun showChatFragment() {
        if(!isFinishing) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            chatFragment = ChatFragment()
            chatFragment.setCommunicationListener(this)
            fragmentTransaction.replace(R.id.mainScreen, chatFragment, "ChatFragment")
            fragmentTransaction.addToBackStack("ChatFragment")
            fragmentTransaction.commit()

        }
    }

    override fun onCommunication(message: String) {
        if(message.equals(Constants.PROTOCOL_INIT.toString())) {
            initProtocol(Constants.USER_ROLE_PROVER)
        } else {
            sendMessage(message)
        }

    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else
            supportFragmentManager.popBackStack()
    }
}

