import React, { createContext, useState, useEffect, useMemo, useContext} from 'react';
import Config from 'react-native-config';

import { getBasicInfo, getFaceInfo, isBasicInfoResponse, isErrorResponse, isFaceInfoResponse } from '../util/auth';
import { AuthContext } from './auth-context';

const UUID = '0';

interface BasicInfo {
  ageDegree: string, 
  ageGroup: string, 
  gender: string, 
  heightGroup: string, 
  nickname: string, 
  region: string,
}

interface FaceInfo {
  generatedS3url: string,
  originS3url: string,
}

interface UserContextType {
  basicinfo: BasicInfo,
  faceinfo: FaceInfo,
  status: string,
  setStatus: (status: string) => void,
}

const defaultBasicInfo = {ageDegree: '', ageGroup: '', gender: '', heightGroup:'', nickname: '', region: ''};
const defaultFaceInfo = {generatedS3url: '', originS3url: ''};

export const UserContext = createContext<UserContextType>({
  basicinfo: defaultBasicInfo,
  faceinfo: defaultFaceInfo,
  status: '',
  setStatus: (status: string) => {},
});

interface ChatProviderProps {
  children: React.ReactNode;
}

const UserContextProvider: React.FC<ChatProviderProps> = ({ children }) => {
  const authCtx = useContext(AuthContext);

  const [status, setStatus] = useState('');
  const [basicinfo, setBasicinfo] = useState<BasicInfo>(defaultBasicInfo);
  const [faceinfo, setFaceinfo] = useState<FaceInfo>(defaultFaceInfo);
  
  // 기본정보 로딩 후 userState.basicinfo 업데이트
  const setBasicInfoState = async () => {
    console.log('setBasicInfoState');
    if (authCtx.accessToken) {
      // 기본정보 get 시도
      console.log('기본정보 로딩 중');
      const basicInfoResponse = await getBasicInfo(authCtx.accessToken);
      console.log('기본정보 로딩 끝');

      // 기본정보 응답 확인
      if (isBasicInfoResponse(basicInfoResponse)) {
        // 기본정보 얻음
        if (basicInfoResponse.ageDegree && 
            basicInfoResponse.ageGroup &&
            basicInfoResponse.gender &&
            basicInfoResponse.heightGroup && 
            basicInfoResponse.nickname &&
            basicInfoResponse.region) {
            console.log('기본정보 있음');
            setStatus('BASIC_INFO_EXIST');
            const newBasicInfo = {
              ageDegree: basicInfoResponse.ageDegree,
              ageGroup: basicInfoResponse.ageGroup,
              gender: basicInfoResponse.gender,
              heightGroup: basicInfoResponse.heightGroup,
              nickname: basicInfoResponse.nickname,
              region: basicInfoResponse.region
            }
            setBasicinfo(newBasicInfo);
        } else {
          console.log('기본정보 없음');
          setStatus('BASIC_INFO_NOT_EXIST');
        }
      }
      if (isErrorResponse(basicInfoResponse)) {
        setStatus('BASIC_INFO_ERROR');
        authCtx.handleErrorResponse(basicInfoResponse);
      }
    }
  }

  // 마스크 이미지 로딩 후 userState.faceinfo 업데이트
  const setFaceInfoState = async () => {
    if (authCtx.accessToken) {
      console.log('마스크 이미지 로딩 중');
      const faceInfoResponse = await getFaceInfo(authCtx.accessToken);
      console.log('마스크 이미지 로딩 끝');

      if (isFaceInfoResponse(faceInfoResponse)) {
        if (faceInfoResponse.generatedS3url !== 'https://facefriend-s3-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png') {
          console.log('마스크 이미지 있음');
          setStatus('FACE_INFO_EXIST');
        }
        else {
          console.log('마스크 이미지 없음');
          setStatus('FACE_INFO_NOT_EXIST');
        }
      } else {
        console.log('마스크 이미지 없음');
        setStatus('FACE_INFO_NOT_EXIST');
      }
      if (isErrorResponse(faceInfoResponse)) {
        setStatus('FACE_INFO_ERROR');
        authCtx.handleErrorResponse(faceInfoResponse);
      }
    }
  }
  
  useEffect(() => {
    if (authCtx.status === 'INITIALIZED') {
      setBasicInfoState();
    }
  }, [authCtx.status])

  useEffect(() => {
    if (status === 'BASIC_INFO_EXIST') {
      setFaceInfoState();
    }
  }, [authCtx.status, status]);

  const value = useMemo(() => ({
    basicinfo,
    faceinfo,
    status,
    setStatus,
  }), [basicinfo, faceinfo, status]);

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
};

export default UserContextProvider;
