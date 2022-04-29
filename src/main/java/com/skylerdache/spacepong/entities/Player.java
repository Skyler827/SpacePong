package com.skylerdache.spacepong.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skylerdache.spacepong.game_elements.PlayerControlState;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="PLAYER_TYPE")
@Getter
@Setter
public abstract class Player {
    @Id
    @GeneratedValue
    private long id;

    private String username;
    private short paddleColorRed;
    private short paddleColorGreen;
    private short paddleColorBlue;
    @OneToMany(mappedBy = "player1")
    @JsonIgnore
    private List<GameEntity> p1Games;
    @OneToMany(mappedBy = "player2")
    @JsonIgnore
    private List<GameEntity> p2Games;

    @Transient
    @Getter
    @Setter
    PlayerControlState controlState;

    private static @NotNull String getHexSegment(int i) {
        if (i >= 16) {
            return Integer.toHexString(i);
        } else {
            return "0" + Integer.toHexString(i);
        }
    }
    public @NotNull String getHexColor() {
        return "#"+ getHexSegment(paddleColorRed)
                +getHexSegment(paddleColorGreen)
                +getHexSegment(paddleColorBlue);
    }

    public void setPaddleColor(String htmlHexColor) {
        paddleColorRed = Short.parseShort(htmlHexColor.substring(1,3), 16);
        paddleColorGreen = Short.parseShort(htmlHexColor.substring(3,5), 16);
        paddleColorBlue = Short.parseShort(htmlHexColor.substring(5,7), 16);
    }
    public Map<String, Object> getHashMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id",id);
        result.put("username", username);
        result.put("color",getHexColor());
        return result;
    }
    public String getJson() {
        return "{"+
            "\"id\":\""+id+"\","+
            "\"username\":\""+username+"\","+
            "\"color\":\""+getHexColor()+"\""+
        "}";
    }
}
