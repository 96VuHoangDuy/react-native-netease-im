<a id="list"></a>

Get group list
------------
API path: `admin/group/list`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
search|String|No|Fuzzy query is performed by group number, group name, ZYZJ account, and group owner's name，could set lookup type by `search_type`
search_type|Integer|No|The default value is 0<br>&emsp;&emsp;`0`-`fuzzy lookup by all data`.<br>&emsp;&emsp;`1`-`fuzzy lookup by group number`<br>&emsp;&emsp;`2`-`fuzzy lookup by group name`<br>&emsp;&emsp;`3`-`fuzzy lookup by group owner's ZYZJ account`<br>&emsp;&emsp;`4`-`fuzzy lookup by group owner's name`
page|Integer|No|Page number value,the default value is1
linage|Integer|No|pagesize,the default value is 0,represents using the system preset page size

<br>

<font color=Orange>Response</font>

```json
{
    "list": [ //list data
        {
            "id": 5615623, //Group id，corresponding to the teamId of the CommsEase group
            "name": "战记讨论组", //Group name
            "master": { //Group owner's account information
                "account": "85652361", //Group owner's ZYZJ account
                "accid": "ce9562156236", //Group owner's accid of the CommsEase
                "nickname": "小黑子", //Group owner's nickname
                "avatar": "", //Avatar image data, may be a url or the base64 data of the image.
                "avatar_raw": "", //Avatar raw image data, may be a url or the base64 data of the image.
            },
            "members": [ //Group members list
                {
                    "account": "85652361", //Member's ZYZJ account
                    "accid": "ce9562156236", //Member's accid of the CommsEase
                    "nickname": "小黑子", //Member's nickname
                    "avatar": "", //Member's avatar image data, may be a url or the base64 data of the image.
                    "avatar_raw": "", //Member's avatar raw image data, may be a url or the base64 data of the image.
                },
                //...
            ]
        }
    ],
    "pager": {
        "page": 1, //Current page 
        "linage": 20, //The size of the page value currently in use
        "all": 52, //The total number of records that meet the requirements
        "all_page": 3, //Total pages
        "overflow": false, //Page numbers are out of range or not,if the page when the request is submitted is outside the scope of all_page, so that it is true.
}
```



<br><br><br><br><br>



<a id="disband"></a>

Forcefully disband a group
------------
API path: `admin/group/disband`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
id|String|<font color=red>Yes</font>|The group id to be disbanded

<br>

<font color=Orange>Response</font>

&emsp;&emsp;none



<br><br><br><br><br>



