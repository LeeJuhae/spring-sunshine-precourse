package study;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RestController
public class JokeController {
    private final ChatClient chatClient;
    private final Functions functions; // Functions 필드 추가

    public JokeController(ChatClient.Builder builder, Functions functions) {
        this.chatClient = builder.build();
        this.functions = functions; // 주입된 functions 사용
    }

    @GetMapping("/joke")
    public ChatResponse joke(
            @RequestParam(defaultValue = "ijuhae") String name,
            @RequestParam(defaultValue = "pirate") String voice) {
        var userMessage = new UserMessage("""
            Tell me about three famous pirates from the Golden Age of Piracy and what they did.
            Write at least one sentence for each pirate.
            """);
        var systemPromptTemplate = new SystemPromptTemplate("""
            You are a helpful AI assistant.
            You are an AI assistant that helps people find information.
            Your name is {name}.
            You should reply to the user's request using your name and in the style of a {voice}.
            """
        );
        var systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));
        var prompt = new Prompt(List.of(userMessage, systemMessage));
        return chatClient.prompt(prompt).call().chatResponse();
    }

    @GetMapping("/actors")
    public ActorFilms actors(@RequestParam(defaultValue = "Tom Cruise") String actor) {
        var converter = new BeanOutputConverter<>(ActorFilms.class);
        var format = converter.getFormat();
        var userMessage = """
        Generate the filmography of 5 movies for {actor}.
        {format}
        """;
        var promptTemplate = new PromptTemplate(userMessage);
        var prompt = promptTemplate.create(Map.of("actor", actor, "format", format));
        var text = chatClient.prompt(prompt).call().content();
        return converter.convert(text);
    }


    @GetMapping("/addDays")
    public String addDays(@RequestParam(defaultValue = "0") int days) {
        var template = new PromptTemplate("Tell me the date after {days} days from today");
        var prompt = template.render(Map.of("days", days));
        return chatClient.prompt()
            .user(prompt)
            .tools(this.functions)
            .call()
            .content();
    }
}