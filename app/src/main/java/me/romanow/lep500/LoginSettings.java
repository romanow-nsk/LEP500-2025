package me.romanow.lep500;

public class LoginSettings {
    private String dataSetverIP="217.71.138.9";
    private int dataServerPort=4567;
    private String userPhone="913*******";
    private String userPass="";
    private long userId=0;
    private String sessionToken="";
    private String registrationCode="";        // Хэш-код регистрации приложения
    public String getDataSetverIP() {
        return dataSetverIP; }
    public void setDataSetverIP(String dataSetverIP) {
        this.dataSetverIP = dataSetverIP; }
    public int getDataServerPort() {
        return dataServerPort; }
    public void setDataServerPort(int dataServerPort) {
        this.dataServerPort = dataServerPort; }
    public String getUserPhone() {
        return userPhone; }
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone; }
    public String getUserPass() {
        return userPass; }
    public void setUserPass(String userPass) {
        this.userPass = userPass; }
    public String getSessionToken() {
        return sessionToken; }
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken; }
    public long getUserId() {
        return userId; }
    public void setUserId(long iserId) {
        this.userId = iserId; }
    public String getRegistrationCode() {
        return registrationCode; }
    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode; }
}
