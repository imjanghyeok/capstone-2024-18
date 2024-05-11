package capstone.facefriend.chat.service.aop;

import capstone.facefriend.chat.domain.ChatMessage;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Transactional
public abstract class ChatAopAbstract {

    @After("execution(* capstone.facefriend.member.service.FaceInfoService.updateOrigin(..)) && args(origin, styleId, memberId)")
    public abstract void beforeUpdateOrigin(MultipartFile origin, int styleId, Long memberId);

    @Before("execution(* capstone.facefriend.chat.repository.ChatMessageRepository.save(..)) && args(chatMessage)")
    public abstract void beforeSaveChatMessage(ChatMessage chatMessage) throws IOException;
}
