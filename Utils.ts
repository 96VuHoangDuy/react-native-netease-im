import { NativeModules, Platform } from "react-native";
const { RNNeteaseIm, PinYin } = NativeModules;

class NimUtils {
  // getCacheSize() {
  //   return RNNeteaseIm.getCacheSize();
  // }
  /**
   * 清除数据缓存
   */
  getSessionCacheSize(sessionId: string) {
    return RNNeteaseIm.getSessionCacheSize(sessionId);
  }

  /**
   * 获取缓存大小
   */
  cleanSessionCache(sessionId: string) {
    return RNNeteaseIm.cleanSessionCache(sessionId);
  }

  getListSessionsCacheSize(sessionIds: Array<string>) {
    return RNNeteaseIm.getListSessionsCacheSize(sessionIds);
  }

  /**
   * 获取缓存大小
   */
  cleanListSessionsCache(sessionIds: Array<string>) {
    return RNNeteaseIm.cleanListSessionsCache(sessionIds);
  }

  /**
   * 播放录音
   * @returns {*}
   */
  play(filepath: string,isExternalSpeaker?: boolean) {
    return RNNeteaseIm.play(filepath, isExternalSpeaker);
  }

  /**
   * 播放本地资源音乐
   * name：iOS：文件名字，Android：文件路径
   * type：音乐类型，如：'mp3'
   * @returns {*}
   */
  playLocal(name: string, type: string) {
    if (Platform.OS === "ios") {
      return RNNeteaseIm.playLocal(name, type);
    }
    return RNNeteaseIm.playLocal("assets:///" + name + "." + type, type);
  }

  getIsPlayingRecord(callback: (isPlaying: boolean) => void) {
    return RNNeteaseIm.getIsPlayingRecord(callback);
  }

  /**
   * 停止播放录音
   * @returns {*}
   */
  stopPlay() {
    return RNNeteaseIm.stopPlay();
  }

  sortPinYin(o: any, key: string) {
    return PinYin.sortPinYin(o, key);
  }

  /**
   * Android only
   * @returns {*}
   */
  fetchNetInfo() {
    return RNNeteaseIm.fetchNetInfo();
  }

  switchAudioOutputDevice(isExternalSpeaker: boolean){
    return RNNeteaseIm.switchAudioOutputDevice(isExternalSpeaker)
  }

  getDeviceLanguage(){
    return RNNeteaseIm.getDeviceLanguage()
  }
}

export default new NimUtils();
