package com.brolei.aikb.infrastructure.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brolei.aikb.common.config.AiKbProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** {@link FixedCharTextSplitter} 的单元测试. */
class FixedCharTextSplitterTest {

  private FixedCharTextSplitter splitter;

  @BeforeEach
  void setUp() {
    AiKbProperties props = new AiKbProperties();
    props.getTextSplitter().setChunkSize(800);
    props.getTextSplitter().setOverlap(120);
    splitter = new FixedCharTextSplitter(props);
  }

  @Test
  void nullOrBlankShouldReturnEmpty() {
    assertTrue(splitter.split(null).isEmpty());
    assertTrue(splitter.split("   ").isEmpty());
  }

  @Test
  void shortTextShouldReturnSingleChunk() {
    List<String> chunks = splitter.split("Hello World");
    assertEquals(1, chunks.size());
    assertEquals("Hello World", chunks.get(0));
  }

  @Test
  void longTextShouldProduceMultipleChunks() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      sb.append("This is line ").append(i).append(" with some extra text to fill up space.\n");
    }
    List<String> chunks = splitter.split(sb.toString());
    assertTrue(chunks.size() > 1, "Long text should produce multiple chunks");
  }

  @Test
  void chunksShouldNotExceedChunkSize() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      sb.append("A".repeat(100)).append("\n");
    }
    List<String> chunks = splitter.split(sb.toString());
    for (String chunk : chunks) {
      assertTrue(chunk.length() <= 800, "Each chunk should be <= chunkSize");
    }
  }

  @Test
  void emptyChunksShouldBeFiltered() {
    String text = "A".repeat(10) + "\n\n\n" + "B".repeat(10);
    List<String> chunks = splitter.split(text);
    for (String chunk : chunks) {
      assertTrue(!chunk.isEmpty(), "No chunk should be empty");
    }
  }
}
