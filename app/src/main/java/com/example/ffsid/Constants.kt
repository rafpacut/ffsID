package com.example.ffsid

class Constants{

    companion object {

        // Message types sent from the BluetoothChatService Handler
        val MESSAGE_STATE_CHANGE = 1
        val MESSAGE_READ = 2
        val MESSAGE_WRITE = 3
        val MESSAGE_DEVICE_NAME = 4
        val MESSAGE_TOAST = 5
        val MESSAGE_RECEIVED = 6
        var MESSAGE_TYPE_SENT = 0
        var MESSAGE_TYPE_RECEIVED = 1
        var MESSAGE_TYPE_PROTOCOL_APPROVED = 220
        var MESSAGE_TYPE_PROTOCOL_REJECTED = 221

        var SEND_ARRAY_INIT = 100
        var WAIT_FOR_ARRAY = 101
        var SEND_ARRAY = 102
//        var RECEIVED_ARRAY = 103

        var STATE_DEFAULT = 8
        var MESSAGE_WRITE_INT = 7

        // Key names received from the BluetoothChatService Handler
        val DEVICE_NAME = "device_name"
        val TOAST = "toast"
        val RECEIVED = "received"
        val ARRAY_SEPARATOR = ";"
        val PACKET_SEPARATOR = "|"

        var USER_ROLE_NONE = 10
        var USER_ROLE_PROVER = 11
        var USER_ROLE_VERIFIER = 12


        var PROTOCOL_INIT = 200
        var PROTOCOL_END = 300

        var PROVER_AWAIT_INTRODUCTION_CONFIRMATION = 201
        var PROVER_AWAIT_CHALLENGE = 204
        var PROVER_REG_INFO = 205
        var PROVER_GEN_X = 207
        var PROVER_CALC_Y = 209
        var PROVER_AWAIT_VERIFICATION = 211

        var VERIFIER_AWAIT_INTRO = 202
        var VERIFIER_CHALLENGE = 203
        var VERIFIER_AWAIT_CONFIRM = 206
        var VERIFIER_AWAIT_X = 208
        var VERIFIER_AWAIT_Y = 210

        var VERIFICATION_SUCCESS = 250
        var VERIFICATION_FAIL =251

    }

}