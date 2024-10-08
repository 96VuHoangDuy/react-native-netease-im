import { NativeModules } from 'react-native';
import {
  ChatroomMessageOrderBy,
  IParamsLoginChatroom,
  NIMChatroomInfo,
  NIMChatroomMember,
} from 'react-native-netease-im/src/Chatroom/chatroom.type';
const { RNNeteaseIm } = NativeModules;

class NimChatroom {
  login(params: IParamsLoginChatroom): Promise<NIMChatroomInfo> {
    return RNNeteaseIm.loginChatroom(params);
  }

  logout(roomId: string) {
    return RNNeteaseIm.logoutChatroom(roomId);
  }

  fetchChatroomInfo(roomId: string): Promise<NIMChatroomInfo> {
    return RNNeteaseIm.fetchChatroomInfo(roomId);
  }

  fetchChatroomMember(
    roomId: string,
    userId: string
  ): Promise<NIMChatroomMember> {
    return RNNeteaseIm.fetchChatroomMember(roomId, userId);
  }

  fetchChatroomMembers(roomId: string): Promise<NIMChatroomMember[]> {
    return RNNeteaseIm.fetchChatroomMembers(roomId);
  }

  fetchMessageHistory(roomId: string, limit: number , currentMessageId?: string, orderBy?: ChatroomMessageOrderBy) {
    return RNNeteaseIm.fetchMessageHistory(roomId, limit, currentMessageId, orderBy)
  }
}

export default new NimChatroom();
