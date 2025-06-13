package me.romanow.lep500.menu;

public abstract class MenuItemAction {
    public final String title;
    public final int color;
    public abstract void onSelect();
    public MenuItemAction(String title) {
        this.title = title;color=0;
        }
    public MenuItemAction(int color0, String title) {
        this.title = title;color = color0;
        }
}
