import { SafeAreaProvider } from 'react-native-safe-area-context';
import AuthContextProvider from './store/auth-context';
import Login from './pages/Login.tsx';
import Signup from './pages/Signup.tsx';
import BottomNavigationPage from './pages/BottomNavigationPage.tsx';
import Home from './pages/Home.tsx';
import BasicInfoPage from './pages/BasicInfoPage.tsx';
import FaceInfoPage from './pages/FaceInfoPage.tsx';
import FaceFeaturePage from './pages/FaceFeaturePage.tsx';
import FindEmail from './pages/FindEmail.tsx';
import FindPw from './pages/FindPw.tsx';

import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import NicknamePage from './pages/NicknamePage.tsx';
import BasicInfoWithoutNickname from './pages/BasicInfoWithoutNickname.tsx';
import OtherUserSelfProduce from './pages/OtherUserSelfProduce.tsx';
import ChatRoomList from './components/chat/ChatRoomList.tsx';
import ChatContextProvider from './store/chat-context.tsx';
import ChatRoomContextProvider from './store/chat-room-context.tsx';
import UserContextProvider from './store/user-context.tsx';

const Stack = createStackNavigator();
function App() {
  return (
    <AuthContextProvider>
      <ChatContextProvider>
        <ChatRoomContextProvider>
          <UserContextProvider>
            <SafeAreaProvider>
              <NavigationContainer>
                <Stack.Navigator initialRouteName='Home' screenOptions={{headerShown: false}}>
                  <Stack.Screen name="Login" component={Login}/>
                  <Stack.Screen name="FindEmail" component={FindEmail}/>
                  <Stack.Screen name="FindPw" component={FindPw}/>
                  <Stack.Screen name="Signup" component={Signup}/>
                  <Stack.Screen name='Main' component={BottomNavigationPage}/>
                  <Stack.Screen name='BasicInfo' component={BasicInfoPage}/>
                  <Stack.Screen name='Home' component={Home}/>
                  <Stack.Screen name='FaceInfo' component={FaceInfoPage}/>
                  <Stack.Screen name='FaceFeature' component={FaceFeaturePage}/>
                  <Stack.Screen name='Nickname' component={NicknamePage}/>
                  <Stack.Screen name='BasicInfoWithoutNickname' component={BasicInfoWithoutNickname}/>
                  <Stack.Screen name='OtherSelfProduce' component={OtherUserSelfProduce}/>
                  {/* Bottom Navigation이 있는 페이지의 경우 SafeAreaView를 이용하면 ios에서 bottomNavigation이 제대로 안 보임 */}
                </Stack.Navigator>
              </NavigationContainer>
            </SafeAreaProvider> 
            </UserContextProvider>
        </ChatRoomContextProvider>
      </ChatContextProvider> 
    </AuthContextProvider>
  );
}

export default App;
