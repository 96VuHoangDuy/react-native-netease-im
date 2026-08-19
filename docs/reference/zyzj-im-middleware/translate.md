<a id="text_trans"></a>

Text translate
------------
API path： `translate/text`

<br>

<font color=LimeGreen>Request</font>

Key|Type|Required|Description
-:|-:|-:|:-
text|String|<font color=red>是</font>|The source text string
from|String|否|The language code of source text，default to `auto` which is automatic recognition. <a href="atta_trans_language_code.md">Here</a> is a description of the language code
to|string|<font color=red>是</font>|The language code of translate target. <a href="atta_trans_language_code.md">Here</a> is a description of the language code

<br>

<font color=Orange>响应数据</font>

```json
{
    "text": "...", //Translate result
    "from": "en", //The language code of source text
    "to": "zh-CHS", //The language code of translate target
}
```


<br><br><br><br><br>