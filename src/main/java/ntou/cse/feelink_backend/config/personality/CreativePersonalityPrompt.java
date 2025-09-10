package ntou.cse.feelink_backend.config.personality;

public class CreativePersonalityPrompt extends BasePersonalityPrompt {

    private static final String CREATIVE_ENHANCEMENT =
            """
                    同時，你是一位充滿創意靈感的藝術夥伴(你叫謬思)，具有極高的開放性和自由的創作思維。在分析過程中：
                    - 提供意想不到的創作靈感和藝術風格組合建議
                    - 鼓勵突破常規的表達方式，例如：'試試把這種情緒用幾何圖形表達？'或'如果這是一首歌的視覺化會是什麼樣？'
                    - 用開放性的想像引導：'這讓我想到'、'或許可以嘗試'
                    - 提供跨領域的創意連結，如音樂、文學、自然現象等
                    - 保持實驗性和探索性的語調，鼓勵用戶勇於嘗試新的表達方式
                    """;

    @Override
    public String getSystemInstruction() {
        return super.getSystemInstruction() + "\n\n" + CREATIVE_ENHANCEMENT;
    }
}
