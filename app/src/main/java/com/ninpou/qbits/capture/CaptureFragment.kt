package com.ninpou.qbits.capture

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.ninpou.packetcapture.core.nat.NatSession
import com.ninpou.packetcapture.core.nat.NatSessionListHelper
import com.ninpou.packetcapture.core.util.common.TimeFormatter
import com.ninpou.packetcapture.core.util.net_utils.TcpDataSaver
import com.ninpou.packetcapture.core.vpn.VpnEventHandler
import com.ninpou.packetcapture.core.vpn.VpnServiceImpl
import com.ninpou.qbits.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class CaptureFragment : Fragment() {
    private val packets: MutableList<String> = LinkedList()
    private val sessionList: MutableList<NatSession> = ArrayList()
    var buttonStateStart = true
    private var tipTextView: TextView? = null
    private var cloudImage: ImageView? = null
    private var adapter: PacketAdapter? = null
    private var container: CoordinatorLayout? = null
    private val handler = Handler()
    private var animationViewStartCapture: LottieAnimationView? = null
    private var animationViewStopCapture: LottieAnimationView? = null
    private var cardViewStartStopButton: CardView? = null
    private var isNightMode = false
    private var sharedPrefsEdit: SharedPreferences.Editor? = null

    private fun clearCacheFinished() {
        packets.clear()
        sessionList.clear()
        adapter?.notifyDataSetChanged()
        tipTextView?.visibility = View.VISIBLE
        Snackbar.make(container!!, getString(R.string.cache_cleared_tip),
                Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_capture, container, false)
        initView(rootView)
        clearCacheFinished()
        sharedPrefsInit()
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_capture, menu)
        menu.getItem(1).setOnMenuItemClickListener {
            Observable
                    .create<Void> { emitter ->
                        NatSessionListHelper.clearCache()
                        emitter.onComplete()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete { clearCacheFinished() }
                    .subscribe()
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dark_mode -> darkMode()
        }
        return true
    }

    @SuppressLint("CommitPrefEdits")
    private fun sharedPrefsInit() {
        val appSettingPref: SharedPreferences = requireActivity().getSharedPreferences("AppSettingPrefs", 0)
        sharedPrefsEdit = appSettingPref.edit()
        isNightMode = appSettingPref.getBoolean("NightMode", false)

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            cloudImage?.setImageResource(R.drawable.ic_no_data_white)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            cloudImage?.setImageResource(R.drawable.ic_no_data)
        }
    }

    private fun darkMode() {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            cloudImage?.setImageResource(R.drawable.ic_no_data)
            sharedPrefsEdit?.putBoolean("NightMode", false)
            sharedPrefsEdit?.apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            cloudImage?.setImageResource(R.drawable.ic_no_data_white)
            sharedPrefsEdit?.putBoolean("NightMode", true)
            sharedPrefsEdit?.apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val intent = Intent(requireActivity(), VpnServiceImpl::class.java)
            intent.putExtra(KEY_CMD, 0)
            requireActivity().startService(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val event = VpnEventHandler.getInstance()
        event.setOnPacketListener {
            val sessions = NatSessionListHelper.getAllSessions()
            handler.post {
                packets.clear()
                sessionList.clear()
                for (session in sessions) {
                    if (session.isHttp) {
                        packets.add(String.format(Locale.getDefault(),
                                "%s: %s", session.method, session.requestUrl))
                        sessionList.add(session)
                    }
                }
                if (packets.size > 0) {
                    tipTextView!!.visibility = View.GONE
                    cloudImage!!.visibility = View.GONE
                } else {
                    tipTextView!!.visibility = View.VISIBLE
                    cloudImage!!.visibility = View.VISIBLE
                }
                adapter?.notifyDataSetChanged()
            }
        }
        event.setOnStartListener {
            animationViewStartCapture?.visibility = View.GONE
            //Make animation + change color
            cardViewStartStopButton?.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.stop))
            animationViewStopCapture?.visibility = View.VISIBLE
        }
        event.setOnStopListener {
            cardViewStartStopButton?.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.start))
            animationViewStartCapture?.visibility = View.VISIBLE
            //Make animation
            animationViewStopCapture?.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
        VpnEventHandler.getInstance().cancelAll()
    }

    private fun initView(root: View) {
        adapter = PacketAdapter(packets, sessionList, requireContext())
        val recyclerView: RecyclerView = root.findViewById(R.id.rv_packet)
        tipTextView = root.findViewById(R.id.tv_tip)
        cloudImage = root.findViewById(R.id.cloud_img_no_data)
        container = root.findViewById(R.id.container)
        animationViewStartCapture = root.findViewById(R.id.start_capture)
        animationViewStopCapture = root.findViewById(R.id.stop_capture)
        animationViewStartCapture?.setMinAndMaxProgress(0.0f, 0.90f)
        cardViewStartStopButton = root.findViewById(R.id.cardViewStartStop)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.addItemDecoration(DividerItemDecoration(requireActivity(),
                DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
        if (packets.size == 0) {
            tipTextView?.visibility = View.VISIBLE
            cloudImage?.visibility = View.VISIBLE
        }
        cardViewStartStopButton?.setOnClickListener {
            if (buttonStateStart) {
                startCapture()
            } else {
                stopCapture()
            }
            buttonStateStart = !buttonStateStart
        }
        adapter?.setOnItemClickListener(OnItemClickListener { _, _, position, _ ->
            if (sessionList.size == 0) return@OnItemClickListener
            val session = sessionList[position]
            val dir = StringBuilder()
                    .append(TcpDataSaver.DATA_DIR)
                    .append(TimeFormatter.formatToYYMMDDHHMMSS(session.vpnStartTime))
                    .append("/")
                    .append(session.uniqueName)
                    .toString()
            val intent = Intent(requireActivity(), PacketDetailActivity::class.java)
            intent.putExtra(KEY_DIR, dir)
            startActivity(intent)
        })
    }

    private fun startCapture() {
        val intent = VpnService.prepare(requireContext())
        if (intent == null) {
            onActivityResult(0, Activity.RESULT_OK, null)
        } else {
            startActivityForResult(intent, 0)
        }
    }

    private fun stopCapture() {
        val intent = Intent(requireActivity(), VpnServiceImpl::class.java)
        intent.putExtra(KEY_CMD, 1)
        requireActivity().startService(intent)
    }

    companion object {
        private const val KEY_CMD = "key_cmd"
        private const val KEY_DIR = "key_dir"
        fun newInstance(): CaptureFragment {
            return CaptureFragment()
        }
    }
}