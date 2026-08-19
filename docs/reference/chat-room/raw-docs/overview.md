# 聊天室功能概述

The chat room is a looser and more open form than a group, similar to a square. There is no strict access mechanism, users can enter and exit freely, and generally speaking, there is no very fixed member organization structure. The typical application scenario is the chat room in the entertainment live broadcast and education live broadcast that NetEase Yunxin is very familiar with. Chat room is a paid expansion capability, which needs to be purchased under the condition of purchasing the basic functions of IM.

## Overview of functions

The chat room is equivalent to a square. As long as someone sees the door to the square, anyone can come in and leave at any time. And the group is like a company. The company is a more private organization. You must be a member of this organization to enter. You can actively apply or be invited to join as a member.

**NetEase Yunxin IM chat room**adopts a multi-layer architecture design, which can realize a real large-scale chat room with no upper limit on the number of participants, and it can meet the real-time requirements of message arrival.

## Functional difference

The differences between chat rooms and groups are as follows:

- The number of people supports different
  - There is no limit on the number of people supported by the chat room.
  - The group capacity of advanced groups and super-large groups is limited. Please refer to [value-added services](https://yunxin.163.com/price/im)for details.

- Permission functions are different
  - The permission management of the chat room is relatively simple, and you can enter and exit freely by default (you can set a blacklist to not allow entry).
  - Groups (advanced groups) can set rich invitation modes, invitation modes, verification modes, etc. Please refer to the [group function overview](https://doc.yunxin.163.com/messaging2/guide/DQzODg4ODE?platform=client)for details.

- The characteristics of the scene are different
  - The chat room is temporary in nature, and most of the members are tourists. After the tourist exits the chat room or is disconnected abnormally, it has nothing to do with the chat room and will no longer receive relevant information about the chat room.
  - The group is fixed in nature. After disconnecting, you will not leave the group. The next time you log in, you will receive a message during the offline period. If you disconnect abnormally, the message push will be triggered.

- Examples of common scenarios
  - **Board game**: The room is temporary, and players can enter and exit freely. They should use the chat room (that is, it is not a group with a small number of people, but it depends on the characteristics of the scene).
  - **Entertainment live broadcast**: There are a large number of viewers, and you can enter and exit freely. You should use the chat room.
  - **Enterprise office**: department/team permissions need to be set up, and historical messages need to be maintained, and groups (advanced groups) should be used.

## List of functions

| **Function**                                                                                                                                 | **Function description**                                                                                                                                                                      |
| -------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Chat room capacity                                                                                                                           | Support unlimited number of chats                                                                                                                                                             |
| Create a new chat room                                                                                                                       | Create a new chat room, which currently only supports creation from the server.                                                                                                               |
| Check the chat room information                                                                                                              | Including the creator, the number of people online, name, announcement, live broadcast address, extension field, whether to send update notification time, notification event extension field |
| Update the chat room information                                                                                                             | Including the creator, the number of people online, name, announcement, live broadcast address, extension field, whether to send update notification time, notification event extension field |
| Modify the chat room on/off status                                                                                                           | Modify the on/off status of the chat room. At present, it only supports modification from the server.                                                                                         |
| Modify the chat room automatic shutdown policy                                                                                               | It is divided into automatic shutdown when not turned on, automatic shutdown after a fixed time, and automatic shutdown after a fixed time after idle.                                        |
| At present, only modification from the server is supported.                                                                                  |
| Chat room message type                                                                                                                       | Support text, pictures, voice, video, file, geographical location, notification message, prompt message, customization                                                                        |
| Chat room message history                                                                                                                    | You can query the chat room history of the last 10 days, and you can set whether to save the history in the cloud when sending messages.                                                      |
| Chat room role                                                                                                                               | Chat room roles are divided into two categories: fixed members and non-fixed members.                                                                                                         |
| Fixed members include creators, administrators and ordinary members, and non-fixed members include ordinary tourists and anonymous tourists. |
| Chat room blacklist                                                                                                                          | After being blocked, you will no longer be able to enter the chat room.                                                                                                                       |
| Chat room ban                                                                                                                                | Banned users can be in the chat room, but they can't send messages.                                                                                                                           |
| Temporary ban in the chat room                                                                                                               | The chat room supports setting a temporary ban duration. When the ban takes a long time, the ban will be automatically canceled.                                                              |
| All members in the chat room are banned.                                                                                                     | Set the overall ban status of the chat room, and only the creator and administrator can speak.                                                                                                |
| Kick out of the chat room                                                                                                                    | Only the administrator can kick, if the target is that the administrator can only the creator kick.                                                                                           |
| Modify your chat room member information                                                                                                     | At present, only the update of chat room nicknames, avatars and extension fields is supported.                                                                                                |
| Enter multiple chat rooms at the same time                                                                                                   | Support the same account to enter multiple chat rooms at the same time, which will establish multiple connections.                                                                            |
| Multiple terminals enter a chat room at the same time                                                                                        | Support the same account to enter the same chat room at the same time on multiple terminals.                                                                                                  |
| Chat room robot                                                                                                                              | Add and delete robots in batches in the chat room, up to 100 accounts at a time.                                                                                                              |
| Top N Indicator Inquiry                                                                                                                      | Check the Top N data of the number of people entering the chat room, the number of active people and the amount of messages by hour/day                                                       |
| Chat room queue                                                                                                                              | For the use of live microphone scene                                                                                                                                                          |

## Member role

Chat room roles are divided into two categories: fixed members and non-fixed members.

- Fixed members are resident members of the chat room. Whether the user is currently online/in the chat room, they can be obtained as chat room members. At present, fixed members include: creators, administrators, ordinary members
- Non-fixed members include ordinary tourists and anonymous tourists. After leaving the chat room or offline, the tourists will no longer appear in the chat room user list.

| **Role**           | **Character description**                                                                                                                                                                                                                |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Creator            | Have the highest authority, including setting up administrators, blacklisting, banning, kicking people, and setting up ordinary members.                                                                                                 |
| Administrator      | Administrators have the permissions to block, ban, kick, and set up ordinary members.                                                                                                                                                    |
| Ordinary members   | Ordinary members are mainly different from tourists. Even if they leave the chat room and are not online at present, they can still be obtained as chat room members.                                                                    |
| Ordinary tourists  | Ordinary visitors are users who enter the chat room in a logged-in state and are not set as fixed members.                                                                                                                               |
| Anonymous tourists | Anonymous tourists can enter the chat room without logging in. Compared with ordinary tourists, anonymous tourists do not support sending messages, being banned, being blacklisted, being set as an administrator/ordinary member, etc. |

## Label function

The chat room tag function can flexibly support sending chat room messages to some members of the chat room, and also support receiving only specific messages.

It is mainly used in the following scenarios:

### **Scene 1: Super Small Class**

The combination of large-scale multi-person classrooms and small-class interaction mode. When a teacher teaches, students can be divided into several small classes for management and teaching interaction in the small class, which can take into account the teaching cost advantage of live large classes and the interactive teaching effect of small classes.

![image-netease](https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdefault%2F%E5%B9%BB%E7%81%AF%E7%89%871.JPG)

The picture above is a typical super small class scene:

- All class members are in the same chat room.
- Teachers can send messages to all classes and receive discussion messages from all classes.
- Students can only receive discussion messages in the class, and the messages sent can only be seen in the class.
- The role of teaching assistant can be configured by yourself, and can manage both one class and multiple classes.

### **Scene 2: Large-scale live chat room**

In the large-scale live chat room scenario, considering the phenomenon of bullet screen brushing and arguing, users can be tagged and grouped, which can achieve the effect of the following scenes:

- Multiple artists and stars live at the same time. Fans of artists and stars can interact internally, but fans between different stars do not interact.
- Sports competitions, e-sports, etc., the audience on both sides of the competition can choose the team they support, and only interact with the fans of the team.
- Formulate tag strategies suitable for their own application according to the characteristics of users. In the same chat room, users of the same age can communicate with each other.

## Message flow control

In order to ensure the user experience (such as avoiding server overloading), there is a traffic control mechanism in the chat room:

- **For ordinary messages**: chat room users can receive up to 20 messages per second, and the excess part will be randomly discarded due to flow control.
- **For high-priority messages**: chat room users receive up to 10 messages per second, and more than one may be lost. In order to avoid the loss of important messages (usually server-side messages), important messages can be set to high-priority messages, so as to ensure that messages within the upper limit of high-priority message flow control (10 messages per second) are not lost. How to **achieve high-priority messages? Please**refer to the [message sending configuration options](https://doc.yunxin.163.com/messaging2/guide/DQzNjE0MDU?platform=client#%E6%B6%88%E6%81%AF%E5%8F%91%E9%80%81%E9%85%8D%E7%BD%AE%E9%80%89%E9%A1%B9).
