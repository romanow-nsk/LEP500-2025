package me.romanow.lep500;

import java.util.ArrayList;
import java.util.HashMap;

import me.romanow.lep500.ble.BTDescriptor;
import romanow.lep500.LEP500Params;

public class LEP500Settings extends LEP500Params {
    //----------------- Для javax.mail -------------------------------------------
    public String mailHost="mail.nstu.ru";
    public String mailBox="romanow@corp.nstu.ru";
    public String mailPass="";
    public String mailSecur="starttls";
    public int mailPort=587;
    public String fatalMessage="";              // Текст фатального сообщения при перезагрузке
    public boolean technicianMode=true;         // Полнофункциональный режим
    public ArrayList<BTDescriptor> knownSensors=new ArrayList<>();
    //---------------------------------------------------------------------------
    public transient HashMap<String,BTDescriptor> nameMap = new HashMap<>();
    public transient HashMap<String,BTDescriptor> addressMap = new HashMap<>();
    public LEP500Settings(){}
    public void createMaps(){
        nameMap.clear();
        addressMap.clear();
        for(BTDescriptor descriptor : knownSensors){
            nameMap.put(descriptor.btName,descriptor);
            addressMap.put(descriptor.btMAC,descriptor);
            }
        }
    public void removeByMAC(String macAddress){
        for(int i=0;i<knownSensors.size();i++){
            if (knownSensors.get(i).btMAC.equals(macAddress)){
                knownSensors.remove(i);
                createMaps();
                break;
                }
            }
        }
    public boolean isTechnicianMode() {
        return technicianMode; }
    public void setTechnicianMode(boolean technicianMode) {
        this.technicianMode = technicianMode; }
}
