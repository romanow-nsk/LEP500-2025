package me.romanow.lep500;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import java.util.ArrayList;

import me.romanow.lep500.service.AppData;


public class MultiListBoxDialog {
        boolean mark []=null;
        AlertDialog myDlg=null;
        MultiListBoxListener ls=null;
        boolean second=false;
        Activity parent=null;
        ArrayList<TextView> viewList = new ArrayList<>();
    public MultiListBoxDialog(Activity activity, String title, ArrayList<String> src, MultiListBoxListener ff){
        try {
            parent=activity;
            mark = new boolean[src.size()];
            for(int i=0;i<mark.length;i++)
                mark[i]=false;
            myDlg=new AlertDialog.Builder(activity).create();
            myDlg.setCancelable(true);
            myDlg.setTitle(null);
            LinearLayout lrr=(LinearLayout)activity.getLayoutInflater().inflate(R.layout.multi_listbox, null);
            LinearLayout trmain=(LinearLayout)lrr.findViewById(R.id.multi_listbox_panel);
            trmain.setPadding(5, 5, 5, 5); 
            if (title!=null){
                 TextView hd=(TextView)lrr.findViewById(R.id.multi_listbox_header);
                 hd.setText(title);
                 hd.setPadding(25, 25, 5, 5);
                 hd.setOnClickListener(new OnClickListener(){
                        public void onClick(final View arg0) {
                            int count=0;
                            for(boolean bb : mark)
                                if (bb)
                                    count++;
                            myDlg.cancel();
                            ls.onSelect(mark);
                            }});
                 hd.setOnLongClickListener(new View.OnLongClickListener() {
                     @Override
                     public boolean onLongClick(View v) {
                         for (int i=0;i<src.size();i++) {
                            mark[i]=!mark[i];
                             int bg=(mark[i] ? R.drawable.background_head_selectb : R.drawable.background_head);
                             viewList.get(i).setBackgroundResource(bg);
                            }
                         return true;
                     }
                 });
                }
             myDlg.setOnCancelListener(new OnCancelListener(){
                public void onCancel(DialogInterface arg0) {
                }
             });
            ls=ff;
            for (int i=0;i<src.size();i++){
            	final int ii=i;
                LinearLayout xx;
                xx=(LinearLayout)activity.getLayoutInflater().inflate(R.layout.listbox_item, null);
                xx.setPadding(5, 5, 5, 5);
                final TextView tt=(TextView)xx.findViewById(R.id.dialog_listbox_name);
                viewList.add(tt);
                tt.setText(src.get(i));
                int bg=(mark[i] ? R.drawable.background_head_select : R.drawable.background_head);
                tt.setBackgroundResource(bg);
                tt.setTextColor(AppData.ColorCommon);
                tt.setOnClickListener(new OnClickListener(){
                    public void onClick(final View arg0) {
                        mark[ii] = !mark[ii];
                        int bg=(mark[ii] ? R.drawable.background_head_selectb : R.drawable.background_head);
                        tt.setBackgroundResource(bg);
               		}});
                trmain.addView(xx);
                }
             myDlg.setView(lrr);
             myDlg.show();
             } catch(Throwable ee){  }                
        }    
}
 