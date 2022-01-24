package com.minet.mitestui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.Visibility;

public class NFCDeviceInfoFragment extends TableLayout {

    private final String deviceNumber;
    private final String hwVersion;
    private final String swVersion;
    private final String uuid;
    private final String capabilities;
    private final int id;

    private boolean isTapped = false;
    private String data;

    // UI ELEMENTS
    View nfcDeviceInfoFragmentView;
    TableLayout tblNFCDeviceInfo;

    TextView txtDevice;
    TextView txtUUID;
    TextView txtSW;
    TextView txtHW;
    TextView txtCapabilities;
    TextView txtCardDataHeader;
    TextView txtCardDataValue;

    public NFCDeviceInfoFragment(Context context, String deviceNumber, String hwVersion, String swVersion, String uuid, String capabilities, int id){
        super(context);

        this.deviceNumber = deviceNumber;
        this.hwVersion = hwVersion;
        this.swVersion = swVersion;
        this.uuid = uuid;
        this.capabilities = capabilities;
        this.id = id;

        initControl(context);
    }

    private void initControl(Context context)
    {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.nfc_device_info_fragment, this);

        tblNFCDeviceInfo = findViewById(R.id.tbl_device_nfc_info);

        txtDevice = findViewById(R.id.txt_device);
        txtUUID = findViewById(R.id.txt_uuid);
        txtSW = findViewById(R.id.txt_sw_version);
        txtHW = findViewById(R.id.txt_hw_version);
        txtCapabilities = findViewById(R.id.txt_capabilities);
        txtCardDataHeader = findViewById(R.id.txt_card_data_header);
        txtCardDataValue = findViewById(R.id.txt_card_data_value);

        txtDevice.setText(deviceNumber);
        txtUUID.setText(uuid);
        txtSW.setText(swVersion);
        txtHW.setText(hwVersion);
        txtCapabilities.setText(capabilities);

        findViewById(R.id.tbl_device_nfc_info).setId(id);
    }

    public void setTapped(boolean tapped){
        this.isTapped = tapped;
    }

    public void setData(String data){
        this.data = data;
    }

    public void setNFCPresent(boolean isPresent, byte[] data){
        if (isPresent) {
            tblNFCDeviceInfo.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_green));
            this.data = Utils.byteArrayToHexString(data, 0, data.length);
            txtCardDataValue.setText(this.data);
        }
        else {
            tblNFCDeviceInfo.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border));
            this.data = null;
            txtCardDataValue.setText("");
        }
    }

    public void showCardData(boolean canShow){
        if (canShow){
            txtCardDataHeader.setVisibility(VISIBLE);
            txtCardDataValue.setVisibility(VISIBLE);
        } else {
            txtCardDataHeader.setVisibility(GONE);
            txtCardDataValue.setVisibility(GONE);
        }
    }

}
