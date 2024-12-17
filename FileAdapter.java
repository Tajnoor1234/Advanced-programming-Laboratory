package com.example.tunesphere;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
public class FileAdapter extends ArrayAdapter<String> {
    public FileAdapter(Context context, ArrayList<String> files) {
        super(context, 0, files);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView fileNameTextView = convertView.findViewById(android.R.id.text1);
        fileNameTextView.setText(getItem(position));
        return convertView;
    }
}
