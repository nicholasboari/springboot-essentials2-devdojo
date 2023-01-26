package academy.devdojo.springboot2.util;

import academy.devdojo.springboot2.requests.AnimePostRequestBody;
import lombok.Data;

@Data
public class AnimePostRequestBodyCreator {
    public static AnimePostRequestBody createAnimeToBeSaved() {
        return AnimePostRequestBody.builder().name(AnimeCreator.createAnimeToBeSaved().getName()).build();
    }
}
