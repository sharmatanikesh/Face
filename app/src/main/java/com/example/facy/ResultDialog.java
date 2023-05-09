package com.example.facy;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ResultDialog extends DialogFragment {

   Button btn;
   TextView textView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_resultdialog,container,false);
        String text  = "";

        btn = view.findViewById(R.id.ok_btn);
        textView = view.findViewById(R.id.dialog);

        Bundle bundle = getArguments();
        text = bundle.getString("RESULT_TEXT");
        textView.setText(text);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;

    }
}
