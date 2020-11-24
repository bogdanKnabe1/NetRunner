package com.b_knabe.packet_capture.core.vpn

import org.junit.Test
import com.google.common.truth.Truth.assertThat

//Check initialization
class VpnProxyProtectTest {

    var vpnServiceTest: IntArray? = null

    //TRUE
    @Test
    fun protectSocket() {
        vpnServiceTest = IntArray(1)
        assertThat(vpnServiceTest != null).isTrue()
    }

    @Test
    fun testProtectDatagram() {
        vpnServiceTest = IntArray(1)
        assertThat(vpnServiceTest != null).isTrue()
    }

    //FALSE
    @Test
    fun protectSocketNull() {
        assertThat(vpnServiceTest != null).isFalse()
    }

    @Test
    fun testProtectDatagramNull() {
        assertThat(vpnServiceTest != null).isFalse()
    }
}