package com.b_knabe.packet_capture.core.vpn

import com.google.common.truth.Truth.assertThat
import org.junit.Test

//check VPN service Implementation fields init
class VpnServiceImplFieldsTest {

    //True
    @Test
    fun `check MTU field`() {
        val mtuExpected = 4096
        val mtuOutput: Int = VpnServiceImpl.MTU
        assertThat(mtuExpected).isEqualTo(mtuOutput)
    }

    @Test
    fun `check SESSION field`() {
        val mtuExpected = "NetRunner"
        val mtuOutput = VpnServiceImpl.SESSION
        assertThat(mtuExpected).isEqualTo(mtuOutput)
    }

    @Test
    fun `check ADDRESS field`() {
        val mtuExpected = "10.0.0.10"
        val mtuOutput = VpnServiceImpl.ADDRESS
        assertThat(mtuExpected).isEqualTo(mtuOutput)
    }
}