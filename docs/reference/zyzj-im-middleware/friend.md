<a id="profile"></a>

Query the other's profile
------------
API path: `friend/profile`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>Yes</font>|The ZYZJ account that initiated the query
target|String|<font color=red>Yes</font>|The ZYZJ account that geted other people's profile

<br>

<font color=Orange>Response[friendly]</font>

<!-- Basic user data + Friend remarks data -->
```json
{
    "account": "62399512", //The ZYZJ account
    "accid": "ce8565212362", //The accid of the CommsEase
    "nickname": "罗小黑", //nickname
    "sex": 0, //Gender
    "avatar": "", //Avatar image data of the owner, may be a url or the base64 data of the image.
    "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
    "country_id": "", //The ID of user's country
    "region_lv1_id": "", //First level administrative district ID
    "region_lv2_id": "", //Second level administrative district ID
    "region_lv3_id": "", //Third level administrative district ID
    "auth_mod": 0, //Friend verification mode
    "note_data": { //Friend note data
        "nickname": "小黑子", //Note nickname
        "is_top": true, //Top or not
        "is_mute": false, //Mute or not（Message do not disturb）
        "is_faved": true, //Star or not
    },
}
```

<font color=Orange>Response[stranger]</font>

<!-- Basic user data + No friend remarks data -->
```json
{
    "account": "62399512", //The ZYZJ account
    "accid": "ce8565212362", //The accid of the CommsEase
    "nickname": "罗小黑", //nickname
    "sex": 0, //Gender
    "avatar": "", //Avatar image data of the owner, may be a url or the base64 data of the image.
    "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
    "country_id": "", //The ID of user's country
    "region_lv1_id": "", //First level administrative district ID
    "region_lv2_id": "", //Second level administrative district ID
    "region_lv3_id": "", //Third level administrative district ID
    "auth_mod": 0, //Friend verification mode
    "note_data": null, //Friend note data, Strangers did not note the data
}
```



<br><br><br><br><br>




<a id="batch_profile"></a>

Batch query the other's profile
------------
API path： `friend/batch/profile`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>是</font>|The ZYZJ account that initiated the query
targets|Array&lt;String&gt;|<font color=red>是</font>|The ZYZJ account list that geted other people's profile

<br>

<font color=Orange>Response</font>

```json
[
    {//friendly
        "account": "62399512", //The ZYZJ account
        "accid": "ce8565212362", //The accid of the CommsEase
        "nickname": "罗小黑", //nickname
        "sex": 0, //Gender
        "avatar": "", //Avatar image data of the owner, may be a url or the base64 data of the image.
        "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
        "country_id": "", //The ID of user's country
        "region_lv1_id": "", //First level administrative district ID
        "region_lv2_id": "", //Second level administrative district ID
        "region_lv3_id": "", //Third level administrative district ID
        "auth_mod": 0, //Friend verification mode
        "note_data": { //Friend note data
            "nickname": "小黑子", //Note nickname
            "is_top": true, //Top or not
            "is_mute": false, //Mute or not（Message do not disturb）
            "is_faved": true, //Star or not
        },
    },
    {//stranger
        "account": "62399512", //The ZYZJ account
        "accid": "ce8565212362", //The accid of the CommsEase
        "nickname": "罗小黑", //nickname
        "sex": 0, //Gender
        "avatar": "", //Avatar image data of the owner, may be a url or the base64 data of the image.
        "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
        "country_id": "", //The ID of user's country
        "region_lv1_id": "", //First level administrative district ID
        "region_lv2_id": "", //Second level administrative district ID
        "region_lv3_id": "", //Third level administrative district ID
        "auth_mod": 0, //Friend verification mode
        "note_data": null, //Friend note data, Strangers did not note the data
    },
    //... other account data ...
]

```

<br><br><br><br><br>




<a id="batch_profile_by_accid"></a>

Batch query the other's profile by accid
------------
API路径： `friend/batch/profile/accid`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>是</font>|The ZYZJ account that initiated the query
accids|Array&lt;String&gt;|<font color=red>是</font>|The accid list that geted other people's profile

<br>

<font color=Orange>Response</font>

_Same to <a href="#batch_profile">Batch query the other's profile</a>_

<br><br><br><br><br>



<a id="add"></a>

Add friend
------------
API path: `friend/add`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>Yes</font>|The ZYZJ account that initiated the add friend
target|String|<font color=red>Yes</font>|The ZYZJ account that added friend
apply_remark|String|No|Friend verification information, if the target ZYZJ account does not require friend verification that the data is ignored.

<br>

<font color=Orange>Response</font>

```json
{
    "wait": true, //Whether to wait for push messages
}
```



<br><br><br><br><br>



<a id="del"></a>

Remove friend
------------
API path: `friend/remove`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>Yes</font>|The ZYZJ account that initiated the remove friend
target|String|<font color=red>Yes</font>|The ZYZJ account that removed friend

<br>

<font color=Orange>Response</font>

```json
{
    "wait": true, //Whether to wait for push messages
}
```



<br><br><br><br><br>



<a id="black"></a>

Add someone to blacklist
------------
API path: `friend/black_list/add`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>Yes</font>|The ZYZJ account that initiated the pull to blacklist
target|String|<font color=red>Yes</font>|The ZYZJ account that pulled to blacklist

<br>

<font color=Orange>Response</font>

```json
{
    "wait": false, //Whether to wait for push messages
}
```



<br><br><br><br><br>



<a id="rm_black"></a>

Remove someone from blacklist
------------
API path: `friend/black_list/remove`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>Yes</font>|The ZYZJ account that initiated the request
target|String|<font color=red>Yes</font>|The ZYZJ account that is removed from blacklist

<br>

<font color=Orange>Response</font>

```json
{
    "wait": false, //Whether to wait for push messages
}
```



<br><br><br><br><br>



<a id="list"></a>

Get the friend list
------------
API path: `friend/list`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String|<font color=red>Yes</font>|The ZYZJ account that initiated the query
type|Integer|No|The data type geted, default is 3, that Get all the data. <br>&emsp;&emsp; 1 Friends list only  <br>&emsp;&emsp; 2 Blacklist only <br>&emsp;&emsp; 3 Friends list and blacklist list only  <br>&emsp;&emsp; 4 Group chat list only <br>&emsp;&emsp; 7 Friends, blacklist, group list <br>&emsp;&emsp; 1025 Friends list only, And the elements contains account/accid only <br>&emsp;&emsp; 1026 Blacklist only, And the elements contains account/accid only <br>&emsp;&emsp; 1027 Friends list and blacklist list only，And the elements contains account/accid only.

_Notes：type is a bit mask value，set the bit 0 to 1 which is 0b001 in order to get the friends list，set the bit 1 to 1 which is 0b010 in order to get the blacklist list，set the bit 2 to 1 which is 0b100 in order to get the groups list. So, when the value is 3 which is 0b011, it indicates getting the friend list and blacklist, but there is not chat group list. set the bit 10 to 1 which is 0b1000000011, it indicates that the data of friend list and blacklist obtained contains only properties account and accid.
<br>

<font color=Orange>Response</font>

<!-- Basic user data + Friend remarks data -->
```json
{
    "friends": [ //The friends list
        {
            "account": "62399512", //The ZYZJ account
            "accid": "ce8565212362", //The accid of the CommsEase
            "nickname": "罗小黑", //Nickname
            "sex": 0, //Gender
            "avatar": "", //Avatar image data of the owner, may be a url or the base64 data of the image.
            "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
            "country_id": "", //The ID of user's country
            "region_lv1_id": "", //First level administrative district ID
            "region_lv2_id": "", //Second level administrative district ID
            "region_lv3_id": "", //Third level administrative district ID
            "auth_mod": 0, //Friend verification mode
            "note_data": { //Friend note data
              "nickname": "小黑子", //Note nickname
              "is_top": true, //Top or not
              "is_mute": false, //Mute or not（Message do not disturb）
              "is_faved": true, //Star or not
            },
        },
        //...
    ],
    "black_list": [ //The blacklist list
        {
            "account": "62399512", //The ZYZJ account
            "accid": "ce8565212362", //The accid of the CommsEase
            "nickname": "罗小黑", //Nickname
            "sex": 0, //Gender
            "avatar": "", //Avatar image data of the owner, may be a url or the base64 data of the image.
            "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
            "country_id": "", //The ID of user's country
            "region_lv1_id": "", //First level administrative district ID
            "region_lv2_id": "", //Second level administrative district ID
            "region_lv3_id": "", //Third level administrative district ID
            "auth_mod": 0, //Friend verification mode
        },
        //...
    ],
    "group_list": [ //The groups list
        {
            "id": 5621253, //Group id，corresponding to the teamId of the CommsEase group
            "name": "战记讨论组", //Group name
        }
    ]
}
```



<br><br><br><br><br>



<a id="save_note"></a>

Save notes for the friend
------------
API path: `friend/note/save`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
original|String|<font color=red>Yes</font>|The ZYZJ account that initiated the save friend notes request
target|String|<font color=red>Yes</font>|The ZYZJ account that saved friend notes
nickname|String|No|Note name
is_top|boolean|No|Top or not
is_mute|boolean|No|Mute or not（Message do not disturb）
is_faved|boolean|No|Star or not

<br>

<font color=Orange>Response</font>

&emsp;&emsp;none













