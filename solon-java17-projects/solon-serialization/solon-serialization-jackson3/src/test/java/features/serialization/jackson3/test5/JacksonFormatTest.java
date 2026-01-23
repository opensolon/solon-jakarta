package features.serialization.jackson3.test5;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.serialization.jackson3.Jackson3EntityConverter;
import org.noear.solon.test.SolonTest;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author coderWu
 */
@Import(profiles = "classpath:jackson_format_test.yml")
@SolonTest
public class JacksonFormatTest {
    private static final String FORMATTED_DATE = "2024-07-25";

    private static final String FORMATTED_TIME = FORMATTED_DATE + " 12:34:56";

    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Inject
    Jackson3EntityConverter entityConverter;

    @Test
    public void customDatePatternTest() throws Throwable {
        TimeModel timeModel = new TimeModel();
        timeModel.setDate(TIME_FORMATTER.parse(FORMATTED_TIME));
        timeModel.setDateWithoutFormat(TIME_FORMATTER.parse(FORMATTED_TIME));

        ContextEmpty ctx = new ContextEmpty();
        entityConverter.write(timeModel, ctx);
        String jsonString = ctx.attr("output");
        System.out.println(jsonString);

        assert "{\"date\":\"2024-07-25\",\"dateWithoutFormat\":\"2024-07-25 12:34:56\"}".equals(jsonString);

        JsonMapper objectMapper = new JsonMapper();
        JsonNode jsonObject = objectMapper.readTree(jsonString);

        assertEquals(FORMATTED_DATE, jsonObject.get("date").asText());
        assertEquals(FORMATTED_TIME, jsonObject.get("dateWithoutFormat").asText());
    }

    @Data
    private static class TimeModel {
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date date;

        private Date dateWithoutFormat;
    }
}
