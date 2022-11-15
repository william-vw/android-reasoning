package wvw.mobile.rules.eyebrow;

public class ReasonCmd {

    private String code;
    private ReasonerListener listener;

    public ReasonCmd(String code, ReasonerListener listener) {
        this.code = code;
        this.listener = listener;
    }

    public String getCode() {
        return code;
    }

    public ReasonerListener getListener() {
        return listener;
    }
}