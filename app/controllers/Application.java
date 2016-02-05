package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import actors.SlackHandlerProtocol;
import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import play.*;
import play.mvc.*;

import views.html.*;

@Singleton
public class Application extends Controller {
    @Inject @Named("slacker")
    ActorRef slackActor;

    public Result index() throws IOException {
        slackActor.tell(new SlackHandlerProtocol.Say("index was requested"), null);
        return ok("foo");
    }
}