<a id="create_tmp_session"></a>

Create the temporary session
------------
API path： `chat/session/tmp/create`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>Yes</font>|The ZYZJ account that want to create temp session
target|String|<font color=red>Yes</font>|The target account of temporary session
from_type|String|<font color=red>Yes</font>|from type：<br>`group` - Group<br>`room` - Chat room
from_id|String|<font color=red>Yes</font>|Pass the group id or the chat room id based on the value of `from_type`
<br>

<font color=Orange>Response</font>

```json
{
    "duration": 259200, //The effective duration of a temporary session, in seconds. If the accounts is friends, the value is 0.
}
```



<br><br><br><br><br>





<a id="close_tmp_session"></a>

Close the temporary session
------------
API path： `chat/session/tmp/close`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>Yes</font>|The ZYZJ account that want to close temp session
target|String|<font color=red>Yes</font>|The target account of temporary session
<br>

<font color=Orange>Response</font>

None



<br><br><br><br><br>
















