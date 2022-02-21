package com.minet.mitestui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import za.co.megaware.MinetService.EventTag;
import za.co.megaware.MinetService.EventType;

public class EventFragment extends RelativeLayout {

    RelativeLayout mainCard;
    ImageView icon;
    TextView cardHeader;
    TextView cardDescription;
    TextView cardTime;

    EventType eventType;
    EventTag eventTag;
    String message;
    String dateTime;

    public EventFragment(Context context, EventType eventType, EventTag eventTag, String message, String dateTime) {
        super(context);

        this.eventTag = eventTag;
        this.eventType = eventType;
        this.message = message;
        this.dateTime = dateTime;

        initControl(context);
    }

    private void initControl(Context context){
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.midevice_event_fragment, this);

        mainCard = findViewById(R.id.event_card);
        icon = findViewById(R.id.icon);
        cardHeader = findViewById(R.id.event_header);
        cardDescription = findViewById(R.id.event_description);
        cardTime = findViewById(R.id.card_time);

        switch (eventType){
            case INFO:
                mainCard.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.event_border_blue));
                icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_even_info));
                break;
            case SUCCESS:
                mainCard.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.event_border_green));
                icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_event_check));
                break;
            case ERROR:
                mainCard.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.event_border_red));
                icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_event_error));
                break;
        }

        cardHeader.setText(eventTag.toString());
        cardDescription.setText(message);
        cardTime.setText(dateTime);
    }

}
