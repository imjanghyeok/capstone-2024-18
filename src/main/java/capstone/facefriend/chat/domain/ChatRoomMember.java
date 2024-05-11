package capstone.facefriend.chat.domain;

import capstone.facefriend.common.domain.BaseEntity;
import capstone.facefriend.member.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Slf4j
@DynamicInsert
public class ChatRoomMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ROOM_ID")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "SENDER_ID")
    private Member sender;

    @ManyToOne
    @JoinColumn(name = "RECEIVER_ID")
    private Member receiver;

    @Column
    private boolean isSenderExist;

    @Column
    private boolean isReceiverExist;

    @Column
    private boolean isSenderPublic;

    @Column
    private boolean isReceiverPublic;
}