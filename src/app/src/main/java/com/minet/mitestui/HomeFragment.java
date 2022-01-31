package com.minet.mitestui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private View homeFragment;
    private Button btnLogin;
    private LoginDialogFragment loginDialogFragment;
    private ProgressBar loadinLogin;

    private RelativeLayout loginScreen;
    private RelativeLayout technicianDetails;
    private RelativeLayout noConnection;

    TextView txtImage;
    TextView txtTechName;
    TextView txtCell;
    TextView txtEmail;

    private TechnicianModel model;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeFragment = inflater.inflate(R.layout.fragment_home, container, false);

        btnLogin = homeFragment.findViewById(R.id.btn_login);
        loadinLogin = homeFragment.findViewById(R.id.loading_login);

        loginScreen = homeFragment.findViewById(R.id.rl_login_section);
        technicianDetails = homeFragment.findViewById(R.id.rl_technician_details);
        noConnection = homeFragment.findViewById(R.id.rl_no_connection);

        txtImage = homeFragment.findViewById(R.id.txt_circle_name);
        txtTechName = homeFragment.findViewById(R.id.txt_logged_username);
        txtCell = homeFragment.findViewById(R.id.txt_cell_value);
        txtEmail = homeFragment.findViewById(R.id.txt_email_value);

        btnLogin.setOnClickListener(loginClicked);

        if (Utils.hasActiveInternetConnection() == ConnectivityState.CONNECTED){
            noConnection.setVisibility(View.GONE);

            if (model == null) loginScreen.setVisibility(View.VISIBLE);
            else {
                displayTechnicianDetails();
                technicianDetails.setVisibility(View.VISIBLE);
            }

        } else {

            if (model == null) noConnection.setVisibility(View.VISIBLE);
            else {
                displayTechnicianDetails();
                technicianDetails.setVisibility(View.VISIBLE);
            }

            loginScreen.setVisibility(View.GONE);
        }

        return homeFragment;
    }

    View.OnClickListener loginClicked = v -> {
        loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.show(getChildFragmentManager(), LoginDialogFragment.TAG);
    };

    public void setLoggingIn(boolean isLoggingIn) {
        // Setup Loading animations for logging in
        if (isLoggingIn)
            loadinLogin.setVisibility(View.VISIBLE);
        else
            loadinLogin.setVisibility(View.INVISIBLE);
    }

    public void setTechnician(TechnicianModel technician) {
        if (technician == null){
            loginScreen.setVisibility(View.VISIBLE);
            technicianDetails.setVisibility(View.GONE);
        } else {
            this.model = technician;

            displayTechnicianDetails();
        }
    }

    public void displayTechnicianDetails(){
        txtTechName.setText(model.getName() + " " + model.getSurname());
        try {
            String chars = String.valueOf(model.getName().charAt(0)) + model.getSurname().charAt(0);
            txtImage.setText(chars);
        } catch (Exception e){
            e.printStackTrace();
        }
        txtCell.setText(model.getCell());
        txtEmail.setText(model.getEmail());

        loginScreen.setVisibility(View.GONE);
        technicianDetails.setVisibility(View.VISIBLE);
    }
}
