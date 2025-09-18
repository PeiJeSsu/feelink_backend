package ntou.cse.feelink_backend.chatroom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Map;

@Document(collection = "daily_emotion_analysis")
public class DailyEmotionAnalysis {

    @Id
    private String id;

    @Field("chatroom_id")
    private String chatroomId;

    @Field("date_string")
    private String dateString;

    @Field("summary")
    private String summary;

    @Field("emotions")
    private Map<String, Float> emotions;

    @Field("demand")
    private String demand;

    @Field("score")
    private float score;

    @Field("magnitude")
    private float magnitude;

    public DailyEmotionAnalysis() {}


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChatroomId() { return chatroomId; }
    public void setChatroomId(String chatroomId) { this.chatroomId = chatroomId; }

    public String getDateString() { return dateString; }
    public void setDateString(String dateString) { this.dateString = dateString; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Map<String, Float> getEmotions() { return emotions; }
    public void setEmotions(Map<String, Float> emotions) { this.emotions = emotions; }

    public String getDemand() { return demand; }
    public void setDemand(String demand) { this.demand = demand; }

    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }

    public float getMagnitude() { return magnitude; }
    public void setMagnitude(float magnitude) { this.magnitude = magnitude; }
}
