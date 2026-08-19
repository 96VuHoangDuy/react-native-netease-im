Preface
=============

<a id="comm_prot"></a>

Communication methods
----------------

<a id="post_type"></a>

### 1. Data submission method
The content type of the submission is `text/plain`, The data of the body is a plaintext string that has been encrypted and base64 encoded. For more information about encryption, please refer to <a href="#encrypt">here</a>.

The orignal data of the `body` that before encryption is a JSON string, it is encoded from raw data through JSON. This JSON string is <a href="#encrypt">encrypted</a> to obtain ciphertext, which is then base64 encoded to obtain the `body` data to be submitted.

The raw data to be submitted is a `key-value` Plain Object. If an API does not require any parameters, <font color=red>it must also submit an empty object</font>, otherwise the server will consider as illegal data and refuse the request.

<a id="resp_type"></a>

<br>

### 2. Format of response data
If the business processing is successfully, the data format of the server responds is same to <a href="#post_type">&lt;Data submission method&gt;</a>, with the same content type `text/plain`, following the same data format and encryption/decryption method.

The situation of HTTP communication errors is not considered here, the server will respond two different data format based on the success or failure of business processing:

- business processing is successfully
- Some error occurred during business processing

When the business processing has been successful, different APIs will respond to different data format. Different APIs will respond to different data formats, which will be provided with their structure and description in the `Response` section of each API manual. The response data is encrypted and the `Content-Type` is `text/plain`.

When the business processing is failed, the API responds with an unencrypted unified data format:

```js
{
    error: 1, //The error code
    message: "Error information", // The description of error
    data: undefined, //Optional, in most cases this attribute will not be present
}
```
Key|Type|Required|Description
-:|-:|-|:-
error|Integer|<font color=red>Yes</font>|The error code, different errors will have different value. <a href="atta_error_code.md">Here</a> is a description of the error code
message|String|<font color=red>Yes</font>|The text description of error
data|any|No|Attached data, in most cases this attribute will not be present, reserved for expansion only. If an API provides additional data when responding to error, the API manual will have a section called `Error data` to explain this additional data.<br><font color=red>Note</font>：The property is **_Optional_**, it must be noted that it cannot be assumed that this attribute must exist.

<font color=red>Particular attention</font>

When the business processing is failed，The response `Content-Type` is `application/json`, and the `body` is the raw JSON data that is unencrypted!

<br>

### 3. Http Header
Except for the unified `Error data` that the server responds to when an error occurs in business processing, any request and response must or optionally have the following header parameters in the `Header`:

key|Type|Require|Description
-:|-:|-|:-
Content-Type|String|<font color=red>Yes</font>|text/plain
Nonce|String|<font color=red>Yes</font>|A random string, <font color=red>the length must be 32 characters</font>, this random string will also be used for `encryption/decryption` and `signature`.
Sign|String|<font color=red>Yes</font>|Signature of the data. Please refer to <a href="#sign">here</a> for details.
Language|String|No|Used to indicate the language that the server should use when responding to a request. Default to `zh-cn`.If the parameter is not specified or an unsupported parameter value is submitted, the default value `zh-cn` is used. Currently, only `zh-cn` is supported.

<br>

### 4. Server authentication method

Overall, the server uses three authentication methods for each request simultaneously:

- IP whitelist
- Rules of encryption and decryption for data
- Signature

If the requesting IP is not in the whitelist, the `body` data format is incorrect, or the data cannot be successfully decrypted, these situations will be considered illegal requests, and the system will respond with error code `0xFFFFFFFF`。

<br>

<a id="enc_and_dec"></a>

Encryption and decryption methods
-------------
The target data for encryption and decryption is the business parameter data to be transmitted, which needs to be encrypted into ciphertext and placed in the `Http Body` for transmission. After receiving the data, the receiver needs to extract the ciphertext from the `Http Body` and decrypt it to obtain the original unencrypted raw data.

### 1. Business data

Business data is the parameter data that needs to be submitted during API requests. Different APIs require different business data to be submitted, but the raw data of the business data must be a plain object. If an API does not require any business data, the submitted data when requesting this API will be an empty object.

<br>

<a id="encrypt"></a>

### 2. Encryption

The encryption process of data consists of 5 steps:

- JSON encoding the business raw object data to obtain `JSON string`
- Generate a random string with a length of 32 characters, namely `Nonce`
- Concatenate `Nonce` and the first 16 characters of the `key`, then perform md5 digest calculation on the concatenated string, and intercept the first 16 characters of the MD5 result to obtain the `iv` that used for encryption
- Encrypt the `JSON string` with `AES-256-CBC`(_pkcs7padding_) using `key` and `iv` to obtain `encrypted data`
- Perform base64 encoding on `encrypted data` to obtain `ciphertext`, which is the `HTTP body data` to be transmitted<br>_Note: Some programming language libraries support direct output of final ciphertext encoded with base64, in which case this step can be omitted._

Example:

```js
import CryptoJS from "crypto-js";

const key = "XokEWMvTf0mXtBSljag2JTDWQRCDfQ8N"; //The key
const data = { //The business raw object data
    original: "65803938",
    target: "36076564"
};
let dataStr = JSON.stringify(data); //Step1: JSON encoding the business raw object data
let nonce = "P671xXzhRhCgQEN4tbq2IgbcW78vnxm4"; //Step2：Generate a random string named Nonce. For simplicity and ease of data verification, fixed values are used here, but it should be noted that in actual code, it should be a real randomly generated 32 character string
let iv = CryptoJS.MD5(
        CryptoJS.enc.Utf8.parse(nonce + key.substring(0,16))
    ).toString(CryptoJS.enc.Hex).substring(0, 16); //Step3: Concatenate the Nonce and the first 16 characters of the Key, and perform md5 digest calculation on the concatenated string to obtain the `iv`

let encrypter = CryptoJS.AES.encrypt( //Step4：Encryption
    CryptoJS.enc.Utf8.parse(dataStr),
    CryptoJS.enc.Utf8.parse(key),
    {
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7,
        iv: CryptoJS.enc.Utf8.parse(iv)
    }
);
let bodyStr = encrypter.toString(); //Directly obtain the encrypted base64 encoding result, so there is an implicit 'step5' here
console.log(bodyStr); //Output：qGWOySqTn98gWmTFW4yjEul1NtUQ9S5IyEg4q8B5InDLa0wo4G/vowZgjwsCfmg7
```

<br>

<a id="decrypt"></a>

### 2. Decryption
&emsp;&emsp;Decryption is the reverse process of encryption, the steps is：

- Concatenate `Nonce` and the first 16 characters of the `key`, then perform md5 digest calculation on the concatenated string, and intercept the first 16 characters of the MD5 result to obtain the `iv` that used for encryption
- Perform base64 decoding on the `Http body data` to obtain `encrypted data`.<br> _Note: Some programming language libraries support direct input of base64 encoded ciphertext for decryption, in which case this step can be omitted._
- Decrypt the `encrypted data` with `AES-256-CBC`(_pkcs7padding_) using `key` and `iv` to obtain the original plaintext `JSON string`.
- Perform JSON parse on `JSON string` to obtain the `business raw object data`.

Example:

```js
const key = "XokEWMvTf0mXtBSljag2JTDWQRCDfQ8N"; //The Key
const bodyStr = "qGWOySqTn98gWmTFW4yjEul1NtUQ9S5IyEg4q8B5InDLa0wo4G/vowZgjwsCfmg7"; //The data to be decrypted which read from HTTP body
const nonce = "P671xXzhRhCgQEN4tbq2IgbcW78vnxm4"; //The random string named Nonce which read from Http Header

let iv = CryptoJS.MD5(
        CryptoJS.enc.Utf8.parse(nonce + key.substring(0,16))
    ).toString(CryptoJS.enc.Hex).substring(0, 16); //Step1: Obtain iv from the Nonce and the Key
let decrypt = CryptoJS.AES.decrypt(
    bodyStr,
    CryptoJS.enc.Utf8.parse(key),
    {
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7,
        iv: CryptoJS.enc.Utf8.parse(iv)
    }
);//Step3: Decryption. Please note, this library directly uses base64 encoded ciphertext for decryption, so the "Step2" is implied here.
let dataStr = decrypt.toString(CryptoJS.enc.Utf8); //Convert the decryption result into a string.
let data = null;
try{
    data = JSON.parse(dataStr); //Step4: JSON parse string to object
}catch(e){
    data = null;
}
console.log(data); //Output object: {original: '65803938', target: '36076564'}
```


<br>

<a id="sign"></a>

Signature
----------------

The target data of the signature is the `JSON string` before encryption, the algorithm used to the signature is `HMAC-SHA1`. The purpose of signature is twofold:

- Verify the legality and completeness of business data
- One of the API communication security verification methods

### Signature steps

- The `Key` and the `Nonce` take the first 16 characters each and connect them together to obtain a string of 32 characters in length, named `Signature key`.
- Use `Signature key` to perform `HMAC-SHA1` summary calculation on `JSON string` and obtain `summary results`.
- Perform `base64` encoding on `summary results` to obtain the finally `Sign`.


Example:

```js
import CryptoJS from "crypto-js";

const key = "XokEWMvTf0mXtBSljag2JTDWQRCDfQ8N"; //The Key
const data = { //The business raw object data
    original: "65803938",
    target: "36076564"
};
let dataStr = JSON.stringify(data); //Step1: JSON encoding the business raw object data to obtain JSON string
let nonce = "P671xXzhRhCgQEN4tbq2IgbcW78vnxm4"; //Step2: Generate a random string named Nonce. For simplicity and ease of data verification, fixed values are used here, but it should be noted that in actual code, it should be a real randomly generated 32 character string
let signKey = key.substring(0,16) + nonce.substring(0,16); //Step3: The `Key` and the `Nonce` take the first 16 characters each and connect them together to obtain Signature key
let signWords = CryptoJS.HmacSHA1( //Step4: Perform hamc-sha1 summary calculation on JSON string
        CryptoJS.enc.Utf8.parse(dataStr),
        CryptoJS.enc.Utf8.parse(signKey)
    );
let sign = CryptoJS.enc.Base64.stringify(signWords); //Step5: Perform base64 encoding on summary results
console.log(sign); //Output: fBrrnfkYWt/GrJ7h0osOBXV/NSA=
```

<br>

<a id="comm_demo"></a>

Complete example of communication protocol
-------------------------

```js
import CryptoJS from "crypto-js";

const key = "XokEWMvTf0mXtBSljag2JTDWQRCDfQ8N"; //The Key
const data = { //The business raw object data
    original: "65803938",
    target: "36076564"
};
let dataStr = JSON.stringify(data); //JSON encoding the business raw object data to obtain JSON string
let nonce = "P671xXzhRhCgQEN4tbq2IgbcW78vnxm4"; //Generate the Nonce, a random string.

//签名部分
let signKey = key.substring(0,16) + nonce.substring(0,16); //The Key and the Nonce take the first 16 characters each and connect them together to obtain Signature key
let signWords = CryptoJS.HmacSHA1( //Perform hamc-sha1 summary calculation on JSON string
        CryptoJS.enc.Utf8.parse(dataStr),
        CryptoJS.enc.Utf8.parse(signKey)
    );
let sign = CryptoJS.enc.Base64.stringify(signWords); //Perform base64 encoding on summary results to obtain the Sign

//Encrypt the body data 
let iv = CryptoJS.MD5(
        CryptoJS.enc.Utf8.parse(nonce + key.substring(0,16))
    ).toString(CryptoJS.enc.Hex).substring(0, 16); //Concatenate the Nonce and the first 16 characters of the Key, and perform md5 digest calculation on the concatenated string to obtain the `iv`

let encrypter = CryptoJS.AES.encrypt( //Encryption
    CryptoJS.enc.Utf8.parse(dataStr),
    CryptoJS.enc.Utf8.parse(key),
    {
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7,
        iv: CryptoJS.enc.Utf8.parse(iv)
    }
);
let bodyStr = encrypter.toString(); //Conver the encrypted result to string to obtain the finally body data

//Send the request. For clarity and simplicity of the code, asynchronous functions are used here.
(async() => {
    let response = await fetch("https://zyzj.server.host/api/path", {
        "headers": {
            "content-type": "text/plain;charset=UTF-8",
            "Nonce": nonce,
            "Sign": sign,
            "Language": "zh-CN",
            "cache-control": "no-cache",
            "pragma": "no-cache"
        },
        "body": bodyStr,
        "method": "POST",
        "mode": "cors",
    });
    if(!response || !response.ok)
        return; //response is error, no further processing required. Note that the actual code may require error handling.
    let nonce = response.headers.get("Nonce");
    let sign = response.headers.get("Sign");
    if(!nonce || nonce.length !== 32 || !sign)
        return; //There are no the Nonce and the Sign in the response header, no further processing required.
    let bodyStr = await response.text();
    if(!bodyStr || bodyStr === "")
        return; //There is no body data,no further processing required.
    
    //Decrypting received response body data to obtain raw JSON string
    let iv = CryptoJS.MD5(
        CryptoJS.enc.Utf8.parse(nonce + key.substring(0,16))
    ).toString(CryptoJS.enc.Hex).substring(0, 16); //Use the Nonce  of the response and the Key to obtain the iv which use for decrypt.
    let decrypt = CryptoJS.AES.decrypt( //Decryption
        bodyStr,
        CryptoJS.enc.Utf8.parse(key),
        {
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7,
            iv: CryptoJS.enc.Utf8.parse(iv)
        }
    );
    let dataStr = decrypt.toString(CryptoJS.enc.Utf8); //Convert the decryption result into string, which is the JSON string of the response.

    //Verify the Sign of the response
    let signKey = key.substring(0,16) + nonce.substring(0,16);
    let signWords = CryptoJS.HmacSHA1(
            CryptoJS.enc.Utf8.parse(dataStr),
            CryptoJS.enc.Utf8.parse(signKey)
        );
    if(CryptoJS.enc.Base64.stringify(signWords) !== sign)
        return; //Sign error, no further processing required.
    
    //Parse the JSON string to raw object
    let data = null;
    try{
        data = JSON.parse(dataStr);
    }catch(e){
        return; //Parse error, no further processing required.
    }
    console.log(data); //Output the object data.

})();
```