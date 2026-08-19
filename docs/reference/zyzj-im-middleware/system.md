<a id="cmd_profile"></a>

Query the profile of the System command dispatcher
---------------------------

API path: `system/cmd/profile`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
无

<br>

<font color=Orange>Response</font>

<!-- System basic data -->
```json
{
    "account": "10000", //System command's ZYZJ account
    "accid": "ss10000", //System command's accid of the CommsEase
    "nickname": "系统", //System command's nickname
    "phone": "", //The constant is an empty string
    "sex": 0, //Gender, Always zero
    "avatar": "", //Always null string
    "avatar_raw": "", //Always null string
    "country_id": "", //Always null string
    "region_lv1_id": "", //Always null string
    "region_lv2_id": "", //Always null string
    "region_lv3_id": "", //Always null string
    "auth_mod": -1, //Always -1, cannot be added as a friend
}
```



<br><br><br><br><br>



<a id="send_custom_notice"></a>

Send a custom notication message
---------------------------

API路径： `system/notice/custom/send`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String|No|The account of the user who will receive the message. If this parameter is specified, it indicates a targeted message; otherwise, it is a broadcast message.
data|Object|<font color=red>Yes</font>|This is the data in the message body, which will be placed intact in the 'data' property of the command message.

_Note: Custom notification messages are typically sent by the App server, sent to App as a custom message (a command message) by the system command dispatcher, and then independently parsed and used by App._



<br>

<font color=Orange>Response</font>

&emsp;&emsp;none



<br><br><br><br><br>