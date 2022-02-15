package com.minet.mitestui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private View profileFragment;
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
        profileFragment = inflater.inflate(R.layout.fragment_user_display, container, false);

        btnLogin = profileFragment.findViewById(R.id.btn_login);
        loadinLogin = profileFragment.findViewById(R.id.loading_login);

        loginScreen = profileFragment.findViewById(R.id.rl_login_section);
        technicianDetails = profileFragment.findViewById(R.id.rl_technician_details);
        noConnection = profileFragment.findViewById(R.id.rl_no_connection);

        txtImage = profileFragment.findViewById(R.id.txt_circle_name);
        txtTechName = profileFragment.findViewById(R.id.txt_logged_username);
        txtCell = profileFragment.findViewById(R.id.txt_cell_value);
        txtEmail = profileFragment.findViewById(R.id.txt_email_value);

        technicianDetails.setVisibility(View.VISIBLE);

        displayTechnicianDetails();

        return profileFragment;
    }

    public void setTechnician(TechnicianModel technician) {
        if (technician == null){
            loginScreen.setVisibility(View.VISIBLE);
            technicianDetails.setVisibility(View.GONE);
        } else {
            this.model = technician;
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
