package me.romanow.lep500;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import romanow.lep500.I_EventListener;

public class OKDialog {
    private AlertDialog myDlg=null;
    private boolean wasChanged=false;
    private Activity base;
    I_EventListener lsn;

    private LinearLayout createItem(final String name){
        LinearLayout xx=(LinearLayout)base.getLayoutInflater().inflate(R.layout.ok_item, null);
        xx.setPadding(5, 5, 5, 5);
        TextView img=(TextView)xx.findViewById(R.id.ok_button);
        img.setText(name);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lsn.onEvent(name);
                myDlg.cancel();
                }
            });
        img.setClickable(true);
        return xx;
        }

    public OKDialog(Activity base0, String parName, final I_EventListener listener){
        lsn = listener;
        base = base0;
        try {
            myDlg=new AlertDialog.Builder(base).create();
            myDlg.setCancelable(true);
            myDlg.setTitle(null);
            myDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    lsn.onEvent(null);
                    myDlg.cancel();
                    }
                });
            LinearLayout layout = createItem(parName);
            myDlg.setView(layout);
            myDlg.show();
        } catch(Exception ee){
            int a=1;
            }
        catch(Error ee){
            int u=0;
        }
    }
}

