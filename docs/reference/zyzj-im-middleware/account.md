<a id="reg"></a>

Register account
------------
API path: `registry`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
nickname|String|<font color=red>Yes</font>|Nickname
phone|String|No|The phone number to be bound. If this data is specified, it must ensure the validity of the phone number and that it has not been bound to other accounts.
sex|Integer|No|Gender, default to 0.<br>&emsp;&emsp;0 not set<br>&emsp;&emsp;1 male<br>&emsp;&emsp;2 female
avatar|String|No|Avatar image data, may be a url or the base64 data of the image. <br>_Note: If the value is incorrect, an empty string is used by default._
country_id|String|No|The ID of user's country, The system is only responsible for saving values, and how to use it is up to the APP to decide. <br>_The following three levels of administrative district IDs are also the same._
region_lv1_id|String|No|First level administrative district ID
region_lv2_id|String|No|Second level administrative district ID
region_lv3_id|String|No|Third level administrative district ID
auth_mod|Integer|No|Friend verification mode, default to 1.<br>&emsp;&emsp;0 No verification required<br>&emsp;&emsp;1 Require friend verification (Currently not in use)
allow_tmp_session|Integer|No|Is accept temporary sessions，<br>&emsp;&emsp;0 - yes<br>&emsp;&emsp;Not 0 - No
<br>

<font color=Orange>Response</font>

<!-- user basic data -->
```json
{
    "account": "62399512", //The ZYZJ account
    "accid": "ce8565212362", //The accid of the CommsEase
    "nickname": "罗小黑", //Nickname
    "phone": "0123456789", //The bound phone number. If no phone number is bound, the value will be an empty string
    "sex": 0, //Gender
    "avatar": "", //Avatar image data, may be a url or the base64 data of the image.
    "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
    "country_id": "", //The country ID
    "region_lv1_id": "", //First level administrative district ID
    "region_lv2_id": "", //Second level administrative district ID
    "region_lv3_id": "", //Third level administrative district ID
    "auth_mod": 0, //Friend verification mode
    "allow_tmp_session": true|false, //Is accept temporary sessions
}
```


<br><br><br><br><br>



<a id="login"></a>

Login
----------

API path: `login`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String|<font color=red>Yes</font>|The ZYZJ account to be logined

<br>

<font color=Orange>Response</font>

```json
{
    "account": "62399512", //The ZYZJ account
    "accid": "ce8565212362", //The accid of CommsEase
    "token": "leFzS2joyWFezksbb34jX996TK47VOjlnwFdpUwz", //The token used to login to CommsEase
    "app_key": "YgU5iITNxI9moEUyf74BIrTKYHNNIR4d", //The AppKey which used for NIM SDK
}
```



<br><br><br><br><br>



<a id="get_acc_info"></a>

Query the profile by the ZYZJ account
---------------------------

API path: `account/profile`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String\|Array&lt;String&gt;|<font color=red>Yes</font>|The ZYZJ account which to query profile

<br>

<font color=Orange>Response[account is a string]</font>

<!-- user basic data -->
```json
{
    "account": "62399512", //The ZYZJ account
    "accid": "ce8565212362", //The accid of the CommsEase
    "nickname": "罗小黑", //Nickname
    "phone": "0123456789", //The bound phone number. If no phone number is bound, the value will be an empty string
    "sex": 0, //Gender
    "avatar": "", //Avatar image data, may be a url or the base64 data of the image.
    "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
    "country_id": "", //The country ID
    "region_lv1_id": "", //First level administrative district ID
    "region_lv2_id": "", //Second level administrative district ID
    "region_lv3_id": "", //Third level administrative district ID
    "auth_mod": 0, //Friend verification mode
    "allow_tmp_session": true|false, //Is accept temporary sessions
}
```

<font color=Orange>Response[account is a string array]</font>

<!-- user basic data -->
```json
[
    {
        "account": "62399512", //The ZYZJ account
        "accid": "ce8565212362", //The accid of the CommsEase
        "nickname": "罗小黑", //Nickname
        "phone": "0123456789", //The bound phone number. If no phone number is bound, the value will be an empty string
        "sex": 0, //Gender
        "avatar": "", //Avatar image data, may be a url or the base64 data of the image.
        "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
        "country_id": "", //The country ID
        "region_lv1_id": "", //First level administrative district ID
        "region_lv2_id": "", //Second level administrative district ID
        "region_lv3_id": "", //Third level administrative district ID
        "auth_mod": 0, //Friend verification mode
        "allow_tmp_session": true|false, //Is accept temporary sessions
    },
    //...
]
```



<br><br><br><br><br>



<a id="get_acc_info_by_accid"></a>

Query the profile by the CommsEase accid
---------------------------

API path: `account/profile/accid`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
accid|String\|Array&lt;String&gt;|<font color=red>Yes</font>|The accid which to query profile

<br>

<font color=Orange>Response[accid is a string]</font>

<!-- user basic data -->
```json
{
    "account": "62399512", //The ZYZJ account
    "accid": "ce8565212362", //The accid of the CommsEase
    "nickname": "罗小黑", //Nickname
    "phone": "0123456789", //The bound phone number. If no phone number is bound, the value will be an empty string
    "sex": 0, //Gender
    "avatar": "", //Avatar image data, may be a url or the base64 data of the image.
    "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
    "country_id": "", //The country ID
    "region_lv1_id": "", //First level administrative district ID
    "region_lv2_id": "", //Second level administrative district ID
    "region_lv3_id": "", //Third level administrative district ID
    "auth_mod": 0, //Friend verification mode
    "allow_tmp_session": true|false, //Is accept temporary sessions
}
```

<font color=Orange>Response[accid is a string array]</font>

<!-- user basic data -->
```json
[
    {
        "account": "62399512", //The ZYZJ account
        "accid": "ce8565212362", //The accid of the CommsEase
        "nickname": "罗小黑", //Nickname
        "phone": "0123456789", //The bound phone number. If no phone number is bound, the value will be an empty string
        "sex": 0, //Gender
        "avatar": "", //Avatar image data, may be a url or the base64 data of the image.
        "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
        "country_id": "", //The country ID
        "region_lv1_id": "", //First level administrative district ID
        "region_lv2_id": "", //Second level administrative district ID
        "region_lv3_id": "", //Third level administrative district ID
        "auth_mod": 0, //Friend verification mode
        "allow_tmp_session": true|false, //Is accept temporary sessions
    },
    //...
]
```



<br><br><br><br><br>



<a id="save"></a>

Save profile
------------
API path: `account/save`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String|<font color=red>Yes</font>|The ZYZJ account which to save the profile data.
nickname|String|No|Nickname
phone|String|No|The bound phone number which needs to be modified. If this data is specified, it must ensure the validity of the phone number and that it has not been bound to other accounts.
sex|Integer|No|Gender.<br>&emsp;&emsp;0 no set<br>&emsp;&emsp;1 male<br>&emsp;&emsp;2 female
avatar|String|No|Avatar image data, may be a url or the base64 data of the image. <br>_Note: If the value is incorrect, it will be ignored._
country_id|String|No|The ID of user's country, The system is only responsible for saving values, and how to use it is up to the APP to decide. <br>_The following three levels of administrative district IDs are also the same._
region_lv1_id|String|No|First level administrative district ID
region_lv2_id|String|No|Second level administrative district ID
region_lv3_id|String|No|Third level administrative district ID
auth_mod|Integer|No|Friend verification mode.<br>&emsp;&emsp;0 No verification required<br>&emsp;&emsp;1 Require friend verification (Currently not in use)
allow_tmp_session|Integer|No|Is accept temporary sessions，<br>&emsp;&emsp;0 - yes<br>&emsp;&emsp;Not 0 - No

<br>

<font color=Orange>Response</font>

<!-- user basic data -->
```json
{
    "account": "62399512", //The ZYZJ account
    "accid": "ce8565212362", //The accid of the CommsEase
    "nickname": "罗小黑", //Nickname
    "phone": "0123456789", //The bound phone number. If no phone number is bound, the value will be an empty string
    "sex": 0, //Gender
    "avatar": "", //Avatar image data, may be a url or the base64 data of the image.
    "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
    "country_id": "", //The country ID
    "region_lv1_id": "", //First level administrative district ID
    "region_lv2_id": "", //Second level administrative district ID
    "region_lv3_id": "", //Third level administrative district ID
    "auth_mod": 0, //Friend verification mode
    "allow_tmp_session": true|false, //Is accept temporary sessions
}
```


