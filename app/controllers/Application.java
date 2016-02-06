package controllers;

import java.io.IOException;

import akka.actor.ActorRef;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import play.mvc.Controller;
import play.mvc.Result;

import static actors.SlackHandlerProtocol.Say;

@Singleton
public class Application extends Controller {
    @Inject @Named("slacker")
    ActorRef slackActor;

    public Result slackSay(String msg) {
        slackActor.tell(new Say(msg), null);
        return ok(msg);
    }

    public Result index() throws IOException {
        slackActor.tell(new Say("index"), null);
        return ok("foo");
    }
}