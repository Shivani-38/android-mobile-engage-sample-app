package com.emarsys.mobileengage.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.MobileEngageException;
import com.emarsys.mobileengage.MobileEngageStatusListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MobileEngageFragment extends Fragment implements MobileEngageStatusListener {
    private static final String TAG = "MobileEngageFragment";

    private Button appLogingAnonymous;
    private Button appLogin;
    private Button appLogout;
    private Button customEvent;
    private Button messageOpen;

    private EditText applicationId;
    private EditText applicationSecret;
    private EditText eventName;
    private EditText eventAttributes;
    private EditText messageId;

    private TextView statusLabel;

    private Switch doNotDisturb;

    private String requestId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mobile_engage, container, false);

        MobileEngage.setStatusListener(this);

        statusLabel = (TextView) root.findViewById(R.id.mobileEngageStatusLabel);


        appLogingAnonymous = (Button) root.findViewById(R.id.appLoginAnonymous);
        appLogin = (Button) root.findViewById(R.id.appLogin);
        appLogout = (Button) root.findViewById(R.id.appLogout);
        customEvent = (Button) root.findViewById(R.id.customEvent);
        messageOpen = (Button) root.findViewById(R.id.messageOpen);

        applicationId = (EditText) root.findViewById(R.id.contactFieldId);
        applicationSecret = (EditText) root.findViewById(R.id.contactFieldValue);
        eventName = (EditText) root.findViewById(R.id.eventName);
        eventAttributes = (EditText) root.findViewById(R.id.eventAttributes);
        messageId = (EditText) root.findViewById(R.id.messageId);

        doNotDisturb = root.findViewById(R.id.doNotDisturb);

        appLogingAnonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusLabel.setText("Anonymous login: ");
                MobileEngage.appLogin();
            }
        });

        appLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactFieldId = applicationId.getText().toString();
                if (!contactFieldId.isEmpty()) {
                    int id = Integer.parseInt(contactFieldId);
                    String secret = applicationSecret.getText().toString();
                    requestId = MobileEngage.appLogin(id, secret);
                    statusLabel.setText("Login: ");
                }
            }
        });

        appLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobileEngage.appLogout();
                statusLabel.setText("Logout: ");
            }
        });

        customEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = eventName.getText().toString();
                String attributesString = eventAttributes.getText().toString();

                Map<String, String> attributes = null;
                if (!attributesString.isEmpty()) {
                    try {
                        attributes = new HashMap<>();
                        JSONObject json = new JSONObject(attributesString);
                        Iterator<String> keys = json.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            attributes.put(key, json.getString(key));
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, "Event attributes edittext content is not a valid JSON!");
                    }
                }

                MobileEngage.trackCustomEvent(name, attributes);
                statusLabel.setText("Custom event: ");
            }
        });

        messageOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                String id = messageId.getText().toString();
                Bundle payload = new Bundle();
                payload.putString("key1", "value1");
                payload.putString("u", String.format("{\"sid\": \"%s\"}", id));
                intent.putExtra("payload", payload);
                statusLabel.setText("Message open: ");
                MobileEngage.trackMessageOpen(intent);
            }
        });

        doNotDisturb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MobileEngage.InApp.setPaused(isChecked);
            }
        });

        return root;
    }

    @Override
    public void onStatusLog(String id, String message) {
        Log.i(TAG, message);
        statusLabel.append("OK");
        if (id.equals(requestId)) {
            ((MainActivity) getActivity()).updateBadgeCount();
        }
    }

    @Override
    public void onError(String id, Exception e) {
        Log.e(TAG, e.getMessage(), e);
        StringBuilder sb = new StringBuilder();
        if (e instanceof MobileEngageException) {
            MobileEngageException mee = (MobileEngageException) e;
            sb.append(mee.getStatusCode());
            sb.append(" - ");
        }
        sb.append(e.getMessage());
        statusLabel.append(sb.toString());
    }
}