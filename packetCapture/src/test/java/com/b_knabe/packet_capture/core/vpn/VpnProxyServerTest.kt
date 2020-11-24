package com.b_knabe.packet_capture.core.vpn

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VpnProxyServerTest {
    //TRUE
    @Test
    fun `get MTU size`() {
        val result = VpnProxyServer.getMtu()
        assertThat(result).isEqualTo(4096)
    }

    @Test
    fun `get not null MTU size`() {
        val result = VpnProxyServer.getMtu()
        assertThat(result).isNotNull()
    }

    @Test
    fun `check address`() {
        val result = VpnProxyServer.getAddress()
        assertThat(result).isEqualTo(VpnServiceImpl.ADDRESS)
    }

    @Test
    fun `check start time`() {
        val result = VpnProxyServer.getAddress()
        assertThat(result).isEqualTo(VpnProxyServer.getAddress())
    }

    //False
    @Test
    fun `get false MTU size`() {
        val result = VpnProxyServer.getMtu()
        assertThat(result).isNotEqualTo(1500)
    }

    @Test
    fun `check false address`() {
        val result = VpnProxyServer.getAddress()
        assertThat(result).isNotEqualTo(VpnServiceImpl.GOOGLE_DNS_FIRST)
    }

    @Test
    fun `check false start time`() {
        val result = VpnProxyServer.getStartTime()
        assertThat(result).isNotEqualTo(VpnProxyServer.getAddress())
    }
}

