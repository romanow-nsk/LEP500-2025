package me.romanow.lep500;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.romanow.lep500.service.AppData;
import romanow.lep500.I_EventListener;

public abstract class SettingsMenuBase {
    protected AlertDialog myDlg=null;
    protected boolean wasChanged=false;
    protected MainActivity base;
    public SettingsMenuBase(MainActivity base0){
        base = base0;
        dialogMain();
        }
    public void cancel(){
        myDlg.cancel();
        }
    protected LinearLayout createItem(String name, String value, final I_EventListener lsn){
        return createItem(name,value,false,false,lsn);
        }
    protected LinearLayout createItem(String name, String value, boolean shortSize, boolean textType, final I_EventListener lsn){
        LinearLayout xx=(LinearLayout)base.getLayoutInflater().inflate(
                shortSize ? R.layout.settings_item_short : R.layout.settings_item, null);
        xx.setPadding(5, 5, 5, 5);
        final EditText tt=(EditText) xx.findViewById(R.id.dialog_settings_value);
        if (!textType)
            value = value.replace(",",".");
        tt.setText(""+value);
        TextView img=(TextView)xx.findViewById(R.id.dialog_settings_name);
        img.setText(name);
        img.setTextColor(AppData.ColorCommon);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lsn.onEvent(tt.getText().toString());
                }
            });
        img.setClickable(true);
        tt.setTextColor(AppData.ColorCommon);
        tt.setInputType(textType ? InputType.TYPE_CLASS_TEXT : (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        //tt.setInputType(textType ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER );
        tt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId==6 || actionId==5){
                    lsn.onEvent(v.getText().toString());
                    }
                return false;
            }
        });
        return xx;
        }
    protected LinearLayout createListBox(String name, final ArrayList<String> values, int idx, final I_ListBoxListener lsn){
        LinearLayout xx=(LinearLayout)base.getLayoutInflater().inflate(R.layout.settings_item_list, null);
        xx.setPadding(5, 5, 5, 5);
        final TextView tt=(TextView) xx.findViewById(R.id.dialog_settings_value);
        tt.setText(""+values.get(idx));
        tt.setTextColor(AppData.ColorCommon);
        TextView img=(TextView)xx.findViewById(R.id.dialog_settings_name);
        img.setText(name);
        img.setTextColor(AppData.ColorCommon);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ListBoxDialog(base,values,"Функ.окна", new I_ListBoxListener(){
                    @Override
                    public void onSelect(int index) {
                        lsn.onSelect(index);
                        tt.setText(""+values.get(index));
                        }
                    @Override
                    public void onLongSelect(int index) {}
                    @Override
                    public void onCancel() {}
                }).create();
            }
        });
        img.setClickable(true);
        return xx;
        }
    //-----------------------------------------------------------------------------------------
    public abstract void settingsSave();
    public abstract void createDialog(LinearLayout trmain);
    //------------------------------------------------------------------------------------------
    protected void settingsChanged(){
        settingsSave();
        wasChanged=true;
        base.popupInfo("Настройки изменены");
        }
    public void dialogMain(){
        try {
            myDlg=new AlertDialog.Builder(base).create();
            myDlg.setCancelable(true);
            myDlg.setTitle(null);
            RelativeLayout lrr=(RelativeLayout)base.getLayoutInflater().inflate(R.layout.settings, null);
            LinearLayout trmain=(LinearLayout)lrr.findViewById(R.id.dialog_settings_panel);
            trmain.setPadding(5, 5, 5, 5);
            TextView hd=(TextView)lrr.findViewById(R.id.dialog_settings_header);
            hd.setOnClickListener(new View.OnClickListener(){
                public void onClick(final View arg0) {
                    myDlg.cancel();
                }});
            myDlg.setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface arg0) {
                    myDlg.cancel();
                    }
                });
            createDialog(trmain);
            myDlg.setView(lrr);
            myDlg.show();
        } catch(Exception ee){
            int a=1;
            }
        catch(Error ee){
            int u=0;
        }
    }
}

