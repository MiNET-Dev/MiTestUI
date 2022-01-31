package com.minet.mitestui;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


public class LoginDialogFragment extends DialogFragment {

    public static String TAG = "LoginDialogFragment";

    // UI ELEMENTS
    private EditText txtUsername;
    private EditText txtPin;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface LoginDialogListener {
        public void onLoginDialogPositiveClick(DialogFragment dialog, String username, String pin);
        public void onLoginDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    LoginDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (LoginDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View customView = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_signin, null);

        txtUsername = customView.findViewById(R.id.txt_username);
        txtPin = customView.findViewById(R.id.txt_pin);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(customView)
                // Add action buttons
                .setPositiveButton("Signin", (dialog, id) -> {
                    // sign in the user ...
                    listener.onLoginDialogPositiveClick(LoginDialogFragment.this, txtUsername.getText().toString(), txtPin.getText().toString());
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // CANCEL
                    LoginDialogFragment.this.getDialog().cancel();
                    listener.onLoginDialogNegativeClick(LoginDialogFragment.this);
                });

        return builder.create();
    }
}
