![ttystudio GIF](https://media.giphy.com/media/ftAyb0CG1FNAIZt4SO/giphy.gif) 

 # NetRunner android application for packet capture.
  > NetRunner is a packet capture application, which captures and displays any http connections but not https(maybe for future). The main point is creating a VPNService app and activating it, will force all traffic in the device to go through your newly created virtual interface which is managed by a userspace application, where we can receiving IP Packets by reading from the virtual interface. Some of source links was used to understood this point.
  
 - https://android.googlesource.com/platform/development/+/master/samples/ToyVpn/src/com/example/android/toyvpn/ToyVpnService.java
 - https://stackoverflow.com/questions/38679188/capture-network-traffic-programmatically-no-root

Once we have a VPN service running, our app will receive every network byte the device sends, and has the power to inject raw bytes back.

Things get interesting if rather than forwarding these bytes to a VPN provider, we examine them, and then simply put them straight back on the real network. In that case, we get to see every network byte, but we don't interfere with the network connection of the device, and we don't need an externally hosted VPN provider to do it.

And there are some interesting & constructive use cases this opens up though for developer tooling. For example:
 * Inspecting & rewriting mobile traffic for testing & debugging.
 * Building a firewall for Android that blocks outgoing app connections according to your custom rules.
 * Recording metrics on the traffic sent & received by your device.
 * Simulating connection issues by adding delays or randomly injecting packet resets.
 
# NetRunner is using:
 - Kotlin and Java as native language's
 - rxjava for some operations(these small chunks of code were made primarily for training and usability testing versus coroutines)
 - Material and lottie animations for ui
 - okttp3 and retrofit for simple networking
 - okio for fast I/O operations
  
