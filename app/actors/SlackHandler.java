package actors;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import com.google.inject.Inject;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import play.Configuration;
import scala.concurrent.duration.Duration;

import static actors.SlackHandlerProtocol.Say;

public class SlackHandler extends UntypedActor {

    private final String authToken;
    private final String channel;
    private final Long tickerInterval;

    @Inject
    public SlackHandler(Configuration configuration) {
        authToken = configuration.getString("slack.auth-token");
        channel = configuration.getString("slack.channel");
        tickerInterval = configuration.getLong("slack.ticker", 1000L);
    }

    private SlackSession slackSession;
    private SlackChannel slackChannel;
    private Cancellable tick;

    @Override
    public void preStart() throws IOException {
        slackSession = SlackSessionFactory.createWebSocketSlackSession(authToken);
        // TODO: handle exception here and set duration for retry.
        slackSession.connect();
        slackChannel = slackSession.findChannelByName(channel);
        tick = getContext().system().scheduler().schedule(
                Duration.create(tickerInterval, TimeUnit.MILLISECONDS),
                Duration.create(tickerInterval, TimeUnit.MILLISECONDS),
                getSelf(), new Say("tick"), getContext().dispatcher(), null);
    }

    @Override
    public void postStop() throws IOException {
        tick.cancel();
        slackSession.disconnect();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SlackHandlerProtocol.Say) {
            slackSession.sendMessage(slackChannel, ((Say) message).msg, null);
        } else {
            unhandled(message);
        }
    }
}