public class WmlAnchorItem extends LinkItem {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_GO = 1;
    public static final int ACTION_PREV = 2;
    public static final int ACTION_REFRESH = 3;

    int action;
    String target;

    public WmlAnchorItem(String text, int action, String target) {
        super(text);
        this.action = action;
        this.target = target;
    }
}