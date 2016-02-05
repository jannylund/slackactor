package modules;

import actors.SlackHandler;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

public class AppModule extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {
        bindActor(SlackHandler.class, "slacker");
    }
}