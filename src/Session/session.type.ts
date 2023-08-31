export enum NimSessionType {
  None = "None",
  P2P = "P2P",
  Team = "Team",
  SUPER_TEAM = "SUPER_TEAM",
  System = "System",
  Ysf = "Ysf",
  ChatRoom = "ChatRoom",
  QChat = "QChat",
}

export enum QueryDirectionEnum {
  QUERY_OLD = "QUERY_OLD",
  QUERY_NEW = "QUERY_NEW",
}

export enum CustomAttachmentType {
  RedPacket = "redpacket",
  BankTransfer = "transfer",
  BankTransferSystem = "system",
  RedPacketOpen = "redpacketOpen",
  ProfileCard = "ProfileCard",
  Collection = "Collection",
  SystemImageText = "SystemImageText",
  LinkUrl = "url",
  AccountNotice = "account_notice",
  Card = "card",
}

export enum SendAttachmentType {
  ONE_TO_ONE = "0",
  ONE_TO_MANY_FIXED_AMOUNT = "1",
  ONE_TO_MANY_RANDOM_AMOUNT = "2",
}
