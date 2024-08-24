package org.system.design.sse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class EmojiController {

    private static final String[] EMOJIS = {
            "😀", "😂", "😍", "😎", "🤔", "😢", "😠", "🥳", "😱", "😴",
            "😅", "😉", "😊", "😇", "🥰", "😏", "😐", "😑", "😶", "🙄",
            "🤗", "🤔", "🤐", "🤨", "🤯", "😤", "😡", "😠", "😔", "😕",
            "🙃", "🤑", "😲", "😵", "😳", "🥺", "😷", "🤒", "🤕", "🤑",
            "🤠", "😈", "👿", "👻", "💀", "☠️", "👽", "🤖", "🎃", "😺",
            "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾", "🙈", "🙉",
            "🙊", "🐵", "🐒", "🦍", "🦧", "🦉", "🦋", "🐝", "🐞", "🦗",
            "🐜", "🐝", "🦄", "🦋", "🐌", "🐛", "🐜", "🐝", "🐞", "🐸",
            "🦕", "🐧", "🐦", "🐤", "🐣", "🐥", "🦢", "🦉", "🦗", "🐝"
    };
    ;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();

    @GetMapping(value = "/emojis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEmojis() {
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    // Send one random emoji
                    String emoji = EMOJIS[random.nextInt(EMOJIS.length)];
                    emitter.send(SseEmitter.event().data(emoji));

                    // Sleep for 1 second
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
