package com.ninpou.qbits.capture;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.snackbar.Snackbar;
import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionHelper;
import com.ninpou.packetcapture.core.util.common.TimeFormatter;
import com.ninpou.packetcapture.core.util.net.TcpDataSaver;
import com.ninpou.packetcapture.core.vpn.VpnEvent;
import com.ninpou.packetcapture.core.vpn.VpnServiceImpl;
import com.ninpou.qbits.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class CaptureFragment extends Fragment {
    private static final String KEY_CMD = "key_cmd";
    private static final String KEY_DIR = "key_dir";

    private final List<String> packets = new LinkedList<>();
    final private List<NatSession> sessionList = new ArrayList<>();
    boolean buttonStateStart = true;
    private TextView tipTextView;
    private ImageView cloudImage;
    private PacketAdapter adapter;
    private CoordinatorLayout container;
    private Handler handler = new Handler();
    private LottieAnimationView animationViewStartCapture;
    private LottieAnimationView animationViewStopCapture;
    private CardView cardViewStartStopButton;


    public static CaptureFragment newInstance() {
        return new CaptureFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_capture, menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Observable
                        .create(new ObservableOnSubscribe<Void>() {
                            @Override
                            public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
                                NatSessionHelper.clearCache();
                                emitter.onComplete();
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                clearCacheFinished();
                            }
                        })
                        .subscribe();
                return false;
            }
        });
    }


    private void clearCacheFinished() {
        packets.clear();
        sessionList.clear();
        adapter.notifyDataSetChanged();
        tipTextView.setVisibility(View.VISIBLE);
        Snackbar.make(container, getString(R.string.cache_cleared_tip),
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_capture, container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(requireActivity(), VpnServiceImpl.class);
            intent.putExtra(KEY_CMD, 0);
            requireActivity().startService(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        VpnEvent event = VpnEvent.getInstance();
        event.setOnPacketListener(new VpnEvent.OnPacketListener() {
            @Override
            public void onReceive() {
                final Collection<NatSession> sessions = NatSessionHelper.getAllSessions();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        packets.clear();
                        sessionList.clear();
                        for (NatSession session : sessions) {
                            if (session.isHttp) {
                                packets.add(String.format(Locale.getDefault(),
                                        "%s: %s", session.method, session.requestUrl));
                                sessionList.add(session);
                            }
                        }
                        if (packets.size() > 0) {
                            tipTextView.setVisibility(View.GONE);
                            cloudImage.setVisibility(View.GONE);
                        } else {
                            tipTextView.setVisibility(View.VISIBLE);
                            cloudImage.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        event.setOnStartListener(new VpnEvent.OnStartListener() {
            @Override
            public void onStart() {
                animationViewStartCapture.setVisibility(View.GONE);
                //Make animation + change color
                cardViewStartStopButton.setCardBackgroundColor(getResources().getColor(R.color.stop));
                animationViewStopCapture.setVisibility(View.VISIBLE);

            }
        });
        event.setOnStopListener(new VpnEvent.OnStopListener() {
            @Override
            public void onStop() {
                cardViewStartStopButton.setCardBackgroundColor(getResources().getColor(R.color.start));
                animationViewStartCapture.setVisibility(View.VISIBLE);
                //Make animation
                animationViewStopCapture.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        VpnEvent.getInstance().cancelAll();
    }

    private void initView(View root) {
        adapter = new PacketAdapter(packets, sessionList, requireContext());
        RecyclerView recyclerView = root.findViewById(R.id.rv_packet);
        tipTextView = root.findViewById(R.id.tv_tip);
        cloudImage = root.findViewById(R.id.cloud_img_no_data);
        container = root.findViewById(R.id.container);
        animationViewStartCapture = root.findViewById(R.id.start_capture);
        animationViewStopCapture = root.findViewById(R.id.stop_capture);
        animationViewStartCapture.setMinAndMaxProgress(0.0f, 0.90f);
        cardViewStartStopButton = root.findViewById(R.id.cardViewStartStop);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        if (packets.size() == 0) {
            tipTextView.setVisibility(View.VISIBLE);
            cloudImage.setVisibility(View.VISIBLE);
        }

        cardViewStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonStateStart) {
                    startCapture();
                } else {
                    stopCapture();
                }
                buttonStateStart = !buttonStateStart;
            }
        });
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (sessionList.size() == 0) return;
                NatSession session = sessionList.get(position);
                String dir = new StringBuilder()
                        .append(TcpDataSaver.DATA_DIR)
                        .append(TimeFormatter.formatToYYMMDDHHMMSS(session.vpnStartTime))
                        .append("/")
                        .append(session.getUniqueName())
                        .toString();
                Intent intent = new Intent(requireActivity(), PacketDetailActivity.class);
                intent.putExtra(KEY_DIR, dir);
                startActivity(intent);
            }
        });
    }

    private void startCapture() {
        Intent intent = VpnService.prepare(requireContext());
        if (intent == null) {
            onActivityResult(0, Activity.RESULT_OK, null);
        } else {
            startActivityForResult(intent, 0);
        }
    }

    private void stopCapture() {
        Intent intent = new Intent(requireActivity(), VpnServiceImpl.class);
        intent.putExtra(KEY_CMD, 1);
        requireActivity().startService(intent);
    }
}
