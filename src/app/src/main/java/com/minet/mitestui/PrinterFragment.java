package com.minet.mitestui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import za.co.megaware.MinetService.ByteParceAble;

public class PrinterFragment extends Fragment {

    private final String _DOWNLOAD_URL = "http://www.minet.co.za/boot/b.bmp";
    private final String _DESTINATION_PATH = "/storage/emulated/0/Download/b.bmp";

    public Button btnPrint;

    public ImageView imgBMPView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_printer, container, false);

        btnPrint = view.findViewById(R.id.btn_print);

        btnPrint.setOnClickListener(printFunction);

        imgBMPView = view.findViewById(R.id.img_bmp);

        File imageFile = new File(_DESTINATION_PATH);

        if (!imageFile.exists()){
            try {
                btnPrint.setEnabled(false);
                btnPrint.setText("DOWNLOADING");
                imageFile.createNewFile();
                // DOWNLOADING B.BMP ON STARTUP
                Utils.downloadFile(_DOWNLOAD_URL, imageFile, btnPrint, "PRINT");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] bytes = new byte[(int) imageFile.length()];

        try {
            fis.read(bytes);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imgBMPView.setImageBitmap(decodedByte);
            imgBMPView.setMaxHeight(200);
            imgBMPView.setAdjustViewBounds(true);
            imgBMPView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return view;
    }

    View.OnClickListener printFunction = v -> {
        File file = new File("/storage/emulated/0/Download/b.bmp");

        if (file.exists()){
            byte[] printerpayload = Utils.ReadDataFromFile(file, 10, 4);

            if(printerpayload == null){
                Toast.makeText(getContext(), "Error reading file", Toast.LENGTH_LONG).show();
                return;
            }

            int m_uBitmapDataOffset = ((printerpayload[3] << 24) | (printerpayload[2] << 16) | (printerpayload[1] << 8) | printerpayload[0]);
            printerpayload = null;
            printerpayload = Utils.ReadDataFromFile(file, m_uBitmapDataOffset, (int) (file.length() - (m_uBitmapDataOffset)));

            if(printerpayload == null){
                Toast.makeText(getContext(), "Error reading file", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                ByteParceAble _data = new ByteParceAble();
                _data.set_byte(printerpayload);
                int id = new Random().nextInt(61);
                Log.d("PRINTER_PRINT", "MiDEVICE_PRINT-" + id);
                ServiceHelper.getInstance().print(_data, 100, true, id);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

}
