import { useContext, useEffect, useState } from "react";
import { View } from "react-native"
import AutoHeightImage from 'react-native-auto-height-image';
import { useNavigate } from "react-router-native";

import { AuthContext } from '../store/auth-context.tsx';
import { getBasicInfo, isBasicInfoResponse, isValidResponse } from "../util/auth";

const Home = () => {
  const authCtx = useContext(AuthContext);
  const navigate = useNavigate();
  const [reloadCounter, setReloadCounter] = useState(0);
  
  const handleAuth = async () => {
    // 앱 실행시 엑세스토큰이 있는지 체크
    if (authCtx.accessToken) {
      console.log("엑세스토큰 있음");

      // 기본정보 체크
      const response = await getBasicInfo(authCtx.accessToken);

      // 기본정보가 없으면 기본정보 입력 페이지로 이동
      if (isBasicInfoResponse(response)) {
        if (response.nickname === "" || 
        response.ageDegree === "DEFAULT" || 
        response.ageGroup === "DEFAULT" || 
        response.gender === "DEFAULT" ||
        response.heightGroup === "DEFAULT" ||
        response.region === "DEFAULT") {
          console.log("기본정보 없음");
          navigate("/basic-info");
        } else {
          // 기본정보 있음
          console.log("기본정보 있음");
          navigate("/main");
        } 
      }
      // 통신 실패
      else {
        console.log("통신 실패");
        setReloadCounter(reloadCounter + 1);
      }
    }
    // 엑세스토큰이 없지만 리프레시 토큰이 있는 경우
    else if (authCtx.refreshToken) {
      // 엑세스토큰 재발급 시도
      console.log("재발급 시도");
      const response = await authCtx.reissue();
      // 재발급 성공시 새로고침
      if (isValidResponse(response)) {
        console.log("재발급 성공");
        setReloadCounter(reloadCounter + 1);
      }
      // 재발급 실패 시 로그인 페이지로 이동
      else {
        console.log("재발급 실패");
        navigate("/login");
      }
    }
    // 리프레시 토큰이 없는 경우 로그인 페이지로 이동
    else {
      console.log("리프레시 토큰 없음");
      navigate("/login");
    }
  }

  useEffect(() => {
    handleAuth();
    console.log(reloadCounter);
  }, [authCtx.accessToken, reloadCounter])

  return (
    <View>
      <AutoHeightImage
        width={100}
        style={{alignSelf:"center", marginHorizontal: 80}}
        source={require('../assets/images/logo_origin.png')}
      />
    </View>
  )
}

export default Home;