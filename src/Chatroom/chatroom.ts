import { NativeModules } from 'react-native';
import { IParamsLoginChatroom } from 'react-native-netease-im/src/Chatroom/chatroom.type';
const { RNNeteaseIm } = NativeModules;

class NimChatroom {
    login(params: IParamsLoginChatroom) {
        return RNNeteaseIm.loginChatroom(params)
    }
}

export default new NimChatroom()