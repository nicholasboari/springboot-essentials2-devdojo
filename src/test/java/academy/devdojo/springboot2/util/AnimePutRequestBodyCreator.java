package academy.devdojo.springboot2.util;

import academy.devdojo.springboot2.requests.AnimePostRequestBody;
import academy.devdojo.springboot2.requests.AnimePutRequestBody;
import lombok.Data;

@Data
public class AnimePutRequestBodyCreator {
    public static AnimePutRequestBody createAnimeToBeSaved() {
        return AnimePutRequestBody.builder().name(AnimeCreator.createAnimeToBeSaved().getName()).build();
    }
}
