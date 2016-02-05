package actors;

public class SlackHandlerProtocol {
    public static class Say {
        public final String msg;

        public Say(String msg) {
            this.msg = msg;
        }
    }
}
