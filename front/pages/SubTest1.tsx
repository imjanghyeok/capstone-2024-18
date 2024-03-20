import { View, Text, Button } from 'react-native';
import { useNavigate } from "react-router-native";


const SubTest1 = () => {
  const navigate = useNavigate();
  
  return (
    <View style={{margin: 100, alignSelf: 'center'}}>
      <Text style={{fontSize: 20}}>SubTest1창</Text>
      <Button title={"Test2"} onPress={() => {navigate("/test2")}}/>
    </View>
  );
};

export default SubTest1;