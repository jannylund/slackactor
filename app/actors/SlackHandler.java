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
import play.Logger;
import scala.concurrent.duration.Duration;

import static akka.pattern.Patterns.ask;


public class SlackHandler extends UntypedActor {

    @Inject
    Configuration configuration;

    private SlackSession slackSession;
    private SlackChannel slackChannel;
    private Cancellable tick;

    @Override
    public void preStart() throws IOException {
        Logger.debug("Starting up slacker");
        Configuration slackConfig = configuration.getConfig("slack");

        slackSession = SlackSessionFactory.createWebSocketSlackSession(slackConfig.getString("auth-token"));
        // TODO: handle exception here and set duration for retry.
        slackSession.connect();
        slackChannel = slackSession.findChannelByName(slackConfig.getString("channel"));

        getSelf().tell(new SlackHandlerProtocol.Say("actor starting up"), getSelf());

        final Long tickerInterval = slackConfig.getLong("ticker", 1000L);
        tick = getContext().system().scheduler().schedule(
                Duration.create(tickerInterval, TimeUnit.MILLISECONDS),
                Duration.create(tickerInterval, TimeUnit.MILLISECONDS),
                getSelf(), new SlackHandlerProtocol.Say("tick"), getContext().dispatcher(), null);
    }

    @Override
    public void postStop() throws IOException {
        tick.cancel();
        ask(getSelf(), new SlackHandlerProtocol.Say("actor going down"), 1000);
        slackSession.disconnect();
        Logger.debug("shutting down slacker");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SlackHandlerProtocol.Say) {
            slackSession.sendMessage(slackChannel, ((SlackHandlerProtocol.Say) message).msg, null);
        } else {
            unhandled(message);
        }
    }
}