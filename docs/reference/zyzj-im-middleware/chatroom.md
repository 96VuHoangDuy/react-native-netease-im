<a id="admin_list"></a>

Get chat room list(for management)
-------------------------
API path: `admin/chatroom/list`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
none

<br>

<font color=Orange>Response</font>

```json
[
    {
        "id": "61564561", //Chatroom ID，corresponding to the roomid of the CommsEase
        "name": "海内存知己", //Chatroom name
        "announcement": "...", //Chatroom announcement
        "is_closed": false, //if close of not 
    },
    //...
]
```



<br><br><br><br><br>



<a id="list"></a>

Get the list of the chat room which is opened
-------------------------
API path: `chatroom/list`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
none

<br>

<font color=Orange>Response</font>

```json
[
    {
        "id": "61564561", //Chatroom ID，corresponding to the roomid of the CommsEase
        "name": "海内存知己", //Chatroom name
        "announcement": "...", //Chatroom announcement
    },
    //...
]
```



<br><br><br><br><br>



<a id="get_addr"></a>

Get the chat room addr list
-------------------------
API path： `chatroom/addr`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
id|String|<font color="#FF0000">Yes</font>|The chat room id, corresponding to the `roomid` of the CommsEase
account|String|<font color="#FF0000">Yes</font>|The ZYZJ account which will be used to log in to the chat room.
client_type|String|No|The type of client, there are three values: `web`、`common`、`wechat`. The default is `common`

<br>

<font color=Orange>Response</font>

```json
[
    "chatlink-sg.netease.im:7001", //addr of chat room
    //... other addresses ...
]
```



<br><br><br><br><br>



<a id="create"></a>

Create a chat room
-------------------------
API path: `admin/chatroom/create`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
name|String|<font color=red>Yes</font>|Chatroom name

<br>

<font color=Orange>Response</font>

```json
{
    "id": "61564561", //Chatroom ID，corresponding to the roomid of the CommsEase
    "name": "海内存知己", //Chatroom name
    "announcement": "...", //Chatroom announcement
    "is_closed": false, //Close or not 
}
```



<br><br><br><br><br>



<a id="save"></a>

Update a chat room
-------------------------
API path: `admin/chatroom/save`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
id|String|<font color=red>Yes</font>|Chatroom ID，corresponding to the roomid of the CommsEase
name|String|No|Chatroom name
announcement|String|No|Chatroom announcement

<br>

<font color=Orange>Response</font>

&emsp;&emsp;none



<br><br><br><br><br>


<a id="close"></a>

Close a chat room
-------------------------
API path: `admin/chatroom/close`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
id|String|<font color=red>Yes</font>|Chatroom ID to closing the chatroom，corresponding to the roomid of the CommsEase

<br>

<font color=Orange>Response</font>

&emsp;&emsp;none



<br><br><br><br><br>





<a id="set_admin"></a>

Set or Cancel the administrator of chat room
-------------------------
API路径： `admin/chatroom/set_admin`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
room_id|String|<font color=red>Yes</font>|Chatroom ID，corresponding to the roomid of the CommsEase
account|String|<font color=red>Yes</font>|The ZYZJ account to be operated
is_admin|boolean|<font color=red>Yes</font>|set or cancel as administrator，`true` is set  ，`false` is cancel

<br>

<font color=Orange>Response</font>

&emsp;&emsp;none



<br><br><br><br><br>

<a id="mute"></a>

Mute a chat room
-------------------------
API path: `admin/chatroom/mute`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
id|String|<font color=red>Yes</font>|Chatroom ID to muting the chatroom，corresponding to the roomid of the CommsEase
is_mute|boolean|<font color=red>Yes</font>|true：Set the chat room to an overall mute state (only creators and administrators can speak)，false：Cancel set the chat room to an overall mute state

<br>

<font color=Orange>Response</font>

&emsp;&emsp;none



<br><br><br><br><br>

<a id="broadcast"></a>

Send broadcast message to all chat room
-------------------------
API path: `admin/chatroom/broadcast`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
message|string|<font color=red>Yes</font>|text message

<br>

<font color=Orange>Response</font>

&emsp;&emsp;none



<br><br><br><br><br>



