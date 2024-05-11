package capstone.facefriend.chat.service.aop;

import capstone.facefriend.chat.domain.ChatMessage;
import capstone.facefriend.chat.domain.ChatRoomMember;
import capstone.facefriend.chat.repository.ChatMessageRepository;
import capstone.facefriend.chat.repository.ChatRoomMemberRepository;
import capstone.facefriend.chat.repository.ChatRoomRepository;
import capstone.facefriend.member.domain.faceInfo.FaceInfoByLevel;
import capstone.facefriend.member.domain.faceInfo.FaceInfoByLevelRepository;
import capstone.facefriend.member.domain.faceInfo.FaceInfoRepository;
import capstone.facefriend.member.domain.member.Member;
import capstone.facefriend.member.domain.member.MemberRepository;
import capstone.facefriend.member.exception.member.MemberException;
import capstone.facefriend.member.multipartFile.ByteArrayMultipartFile;
import capstone.facefriend.member.service.FaceInfoService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static capstone.facefriend.member.exception.member.MemberExceptionType.NOT_FOUND;

@Slf4j
@Aspect
@Component
@Transactional
@RequiredArgsConstructor
public class ChatAop extends ChatAopAbstract {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final FaceInfoService faceInfoService;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public static final int LEVEL_ONE = 10; // test 목적으로 일단 작게 설정
    public static final int LEVEL_TWO = 20;
    public static final int LEVEL_THREE = 30;
    public static final int LEVEL_FOUR = 40;

    // origin이 업데이트된 이후마다 faceInfoByLevel 또한 업데이트된다.
    @Override
    @After("execution(* capstone.facefriend.member.service.FaceInfoService.updateOrigin(..)) && args(origin, styleId, memberId)")
    public void beforeUpdateOrigin(MultipartFile origin, int styleId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(NOT_FOUND)); // 영속

        String originS3url = member.getFaceInfo().getOriginS3url();

        FaceInfoByLevel faceInfoByLevel = member.getFaceInfoByLevel();
        faceInfoByLevel.setGeneratedByLevelS3url(originS3url); // dirty check
    }

    // chat 갯수가 임계점을 넘을 때마다 generate_face_by_level()을 호출한다.
    // 또한 가중치 이미지를 s3에 업로드하고 그 url을 db에 저장한다.
    @Override
    @Before("execution(* capstone.facefriend.chat.repository.ChatMessageRepository.save(..)) && args(chatMessage)")
    public void beforeSaveChatMessage(ChatMessage chatMessage) throws IOException {
        Long roomId = chatMessage.getChatRoom().getId();
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomId(roomId).orElseThrow(); // snap shot

        // 채팅방의 채팅 객체의 갯수 count
        Member sender = chatRoomMember.getSender();
        Integer chatMessageCount = chatMessageRepository.countChatMessagesByChatRoom(chatMessage.getChatRoom());

        /** param 1 : convert originS3url into byte[] **/
        String originS3url = sender.getFaceInfo().getOriginS3url();
        String originObjectName = originS3url.substring(originS3url.lastIndexOf("/") + 1);
        S3Object originS3Object = amazonS3.getObject(new GetObjectRequest(bucketName, originObjectName));

        S3ObjectInputStream originS3inputStream = originS3Object.getObjectContent();
        ByteArrayOutputStream originS3outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = originS3inputStream.read(buffer)) != -1) {
            originS3outputStream.write(buffer, 0, bytesRead);
        }
        originS3outputStream.close();
        byte[] originS3byteArray = originS3outputStream.toByteArray();

        // convert byte[] into MultipartFile
        ByteArrayMultipartFile originMultipartFile = new ByteArrayMultipartFile(originS3byteArray, originObjectName);

        /** param 2 : styleId **/
        int styleId = sender.getFaceInfo().getStyleId();

        /** param 3 : memberId **/
        Long memberId = sender.getId();


        String newGeneratedByLevelS3url = "";
        switch (chatMessageCount) {

            case LEVEL_ONE:
                newGeneratedByLevelS3url = faceInfoService.updateGeneratedByLevel(originMultipartFile, styleId, memberId, 1);

                break;
            case LEVEL_TWO:
                newGeneratedByLevelS3url = faceInfoService.updateGeneratedByLevel(originMultipartFile, styleId, memberId, 2);
                break;
            case LEVEL_THREE:
                newGeneratedByLevelS3url = faceInfoService.updateGeneratedByLevel(originMultipartFile, styleId, memberId, 3);
                break;
            case LEVEL_FOUR:
                newGeneratedByLevelS3url = faceInfoService.updateGeneratedByLevel(originMultipartFile, styleId, memberId, 4);
                break;
        }

        sender.getFaceInfoByLevel().setGeneratedByLevelS3url(newGeneratedByLevelS3url); // dirty check

    }
}
