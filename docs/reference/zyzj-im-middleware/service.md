<a id="chatbot_profile"></a>

Query the profile of the Chatbot
---------------------------

API path: `service/chatbot/profile`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
none

<br>

<font color=Orange>Response</font>

<!-- Chatbot basic data -->
```json
{
    "account": "20000", //Custormer service chatbot's ZYZJ account 
    "accid": "ss20000", //Custormer service chatbot's accid of the CommsEase
    "nickname": "客服机器人", //Custormer service chatbot's nickname
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



<a id="service_profile"></a>

Query the profile of the currently bound manual customer service
---------------------------

API path: `service/bound_csr`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String|<font color=red>Yes</font>|The ZYZJ account which to query the profile of the currently bound manual customer service

<br>

<font color=Orange>Response[there is a manual customer service]</font>

<!-- manual customer service basic data -->
```json
{
    "csr": {
        "account": "20257", //Manual custormer service's ZYZJ account
        "accid": "ss20257", //Manual custormer service's accid of the CommsEase
        "nickname": "小丽", //Manual custormer service's nickname
        "phone": "", //The constant is an empty string
        "sex": 0, //Gender
        "avatar": "", //Manual custormer service's avatar image data, may be a url or the base64 data of the image.
        "avatar_raw": "", //Manual custormer service's avatar raw image data, may be a url or the base64 data of the image.
        "country_id": "", //Always null string
        "region_lv1_id": "", //Always null string
        "region_lv2_id": "", //Always null string
        "region_lv3_id": "", //Always null string
        "auth_mod": -1, //Always -1, cannot be added as a friend
    }
}
```

<font color=Orange>Response【there is no manual customer service】</font>

```json
{
    "csr": null
}
```



<br><br><br><br><br>



<a id="customer_list"></a>

Get the customer list of the manual customer service
---------------------------

API path: `service/csr/customer/list`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String|<font color=red>Yes</font>|The ZYZJ account which to query the customer list of the manual customer service

<br>

<font color=Orange>Response</font>

<!-- Customer basic data -->
```json
[
    {
        "account": "62399512", //Custormer's ZYZJ account
        "accid": "ce8565212362", //Custormer's accid of the CommsEase
        "nickname": "罗小黑", //Custormer's nickname
        "phone": "0123456789", //The bound phone number. If no phone number is bound, the value will be an empty string
        "sex": 0, //Gender
        "avatar": "", //Custormer's avatar image data, may be a url or the base64 data of the image.
        "avatar_raw": "", //Custormer's avatar raw image data, may be a url or the base64 data of the image.
        "country_id": "", //The country ID
        "region_lv1_id": "", //First level administrative district ID
        "region_lv2_id": "", //Second level administrative district ID
        "region_lv3_id": "", //Third level administrative district ID
        "auth_mod": 0, //Friend verification mode
    },
    //...
]
```



<br><br><br><br><br>



<a id="online_csr_list"></a>

Get the online manual customer service list
---------------------------

API path: `service/csr/onlines`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
customer_service_type|String|No|Support by customer service type search： 1-Phone charge recharge customer service staff，2-Air ticket customer service staff

<br>

<font color=Orange>Response</font>

<!-- Customer service basic data -->
```json
[
    {
        "account": "20257", //Manual custormer service's ZYZJ account
        "accid": "ss20257", //Manual custormer service's accid of the CommsEase
        "nickname": "小丽", //Manual custormer service's nickname
        "phone": "", //The constant is an empty string
        "sex": 0, //Gender
        "avatar": "", //Manual custormer service's avatar image data, may be a url or the base64 data of the image.
        "avatar_raw": "", //Manual custormer service's avatar raw image data, may be a url or the base64 data of the image.
        "country_id": "", //Always null string
        "region_lv1_id": "", //Always null string
        "region_lv2_id": "", //Always null string
        "region_lv3_id": "", //Always null string
        "auth_mod": -1, //Always -1, cannot be added as a friend
        "customer_service_type": 1, //Customer service type ： 1-Phone charge recharge customer service staff，2-Air ticket customer service staff
    },
    //...
]
```



<br><br><br><br><br>


<a id="list_csr"></a>

Get the current manual customer service list
---------------------------

API path: `admin/service/csr/list`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
无

<br>

<font color=Orange>Response</font>

<!-- Customer service basic data -->
```json
[
    {
        "account": "20257", //Manual custormer service's ZYZJ account
        "accid": "ss20257", //Manual custormer service's accid of the CommsEase
        "nickname": "小丽", //Manual custormer service's nickname
        "phone": "", //The constant is an empty string
        "admin_acc": "admin_xl", //The administrator account which are bond
        "sex": 0, //Gender
        "avatar": "", //Manual custormer service's avatar image data, may be a url or the base64 data of the image.
        "avatar_raw": "", //Manual custormer service's avatar raw image data, may be a url or the base64 data of the image.
        "country_id": "", //Always null string
        "region_lv1_id": "", //Always null string
        "region_lv2_id": "", //Always null string
        "region_lv3_id": "", //Always null string
        "auth_mod": -1, //Always -1, cannot be added as a friend
        "customer_service_type": 1, //Customer service type ： 1-Phone charge recharge customer service staff，2-Air ticket customer service staff
    },
    //...
]
```



<br><br><br><br><br>



<a id="create_csr"></a>

Create the manual customer service 
---------------------------

API path: `admin/service/csr/create`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
nickname|String|<font color=red>Yes</font>|Manual custormer service's nickname
admin_acc|String|<font color=red>Yes</font>|The administrator account which to bond
customer_service_type|String|No|Customer service type： 1-Phone charge recharge customer service staff，2-Air ticket customer service staff
avatar|String|No|The image url or base64 encoding string of avatar(head image).

<br>

<font color=Orange>Response</font>

<!-- Customer service basic data -->
```json
{
    "account": "20257", //Manual custormer service's ZYZJ account
    "accid": "ss20257", //Manual custormer service's accid of the CommsEase
    "nickname": "小丽", //Manual custormer service's nickname
    "phone": "", //The constant is an empty string
    "admin_acc": "admin_xl", //The administrator account which are bond
    "sex": 0, //Gender
    "avatar": "", //Manual custormer service's avatar image data, may be a url or the base64 data of the image.
    "avatar_raw": "", //Manual custormer service's avatar raw image data, may be a url or the base64 data of the image.
    "country_id": "", //Always null string
    "region_lv1_id": "", //Always null string
    "region_lv2_id": "", //Always null string
    "region_lv3_id": "", //Always null string
    "auth_mod": -1, //Always -1, cannot be added as a friend
    "customer_service_type": 1, //Customer service type ： 1-Phone charge recharge customer service staff，2-Air ticket customer service staff
}
```



<br><br><br><br><br>



<a id="save_csr"></a>

Save the manual customer service 
---------------------------

API path: `admin/service/csr/save`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String|<font color=red>Yes</font>|Manual custormer service's ZYZJ account
nickname|String|No|Manual custormer service's nickname
admin_acc|String|No|The administrator account which to bond. The empty string indicates to unbinding
password|String|<font color=orange>discarded</font>|password (Empty string will be ignore)
customer_service_type|String|No|Customer service type： 1-Phone charge recharge customer service staff，2-Air ticket customer service staff
avatar|String|No|The image url or base64 encoding string of avatar(head image).

<br>

<font color=Orange>Response</font>

<!-- Customer service basic data -->
none



<br><br><br><br><br>



<a id="set_csr_online_state"></a>

Set the manual customer service online state
---------------------------

API path: `service/csr/set_online`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
account|String|<font color=red>Yes</font>|Manual custormer service's ZYZJ account
ttl|String|<font color=red>Yes</font>|Online state duration, unit: `second`.Less than or equal to 0 indicates immediate failure, that is, it is set to offline.

<br>

<font color=Orange>Response</font>

<!-- Customer service basic data -->
none



<br><br><br><br><br>



<a id="csr_types"></a>

Get the type list of customer service
---------------------------

API路径： `admin/service/csr/types`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
None

<br>

<font color=Orange>Response</font>

```json
[
    {
        "id": 1, //the type code
        "name": "话费充值", //the type name
    },
    {
        "id": 2,
        "name": "机票",
    },
]
```



<br><br><br><br><br>