package capstone.facefriend.chat.infrastructure.repository;

import capstone.facefriend.chat.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomIdOrderByCreatedAt(Long roomId, Pageable pageable);

    Message save(Message message);

}
