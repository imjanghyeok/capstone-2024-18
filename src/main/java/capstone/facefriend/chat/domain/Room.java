package capstone.facefriend.chat.domain;

import capstone.facefriend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Slf4j
@DynamicInsert
public class Room extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String name;

        @Enumerated(EnumType.STRING)
        @Column
        private Status status = Status.set;

        @Column
        private boolean isPublic;

        public enum Status {
                set("Set"),
                open("Open"),
                close("Close"),
                delete("Delete");
                private final String value;

                Status(String value) {
                        this.value = value;
                }

                public String getValue() {
                        return value;
                }
        }


}
