package notai.client.ai;

import notai.client.ai.request.LlmTaskRequest;
import notai.client.ai.response.TaskResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@SpringBootTest
@Tag("exclude-test") // 테스트 필요할때 주석
class AiClientIntegrationTest {

    @Autowired
    private AiClient aiClient;

    @Test
    void LLM_태스크_제출_통합_테스트() {
        // Given
        LlmTaskRequest request = LlmTaskRequest.of("OCR 텍스트", "STT 텍스트", "키보드 노트");

        // When
        TaskResponse response = aiClient.submitLlmTask(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.taskId());
        assertEquals("llm", response.taskType());
    }

    @Test
    void STT_태스크_제출_통합_테스트() {
        // Given
        byte[] audioBytes = "test audio content".getBytes();
        ByteArrayResource byteArrayResource = new ByteArrayResource(audioBytes);

        // When
        TaskResponse response = aiClient.submitSttTask(byteArrayResource);

        // Then
        assertNotNull(response);
        assertNotNull(response.taskId());
        assertEquals("stt", response.taskType());
    }
}
