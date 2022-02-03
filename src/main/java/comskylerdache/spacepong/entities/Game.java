package comskylerdache.spacepong.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
@Data
public class Game {
    @Id
    @GeneratedValue
    long id;

    ZonedDateTime start;
    ZonedDateTime end;

}
